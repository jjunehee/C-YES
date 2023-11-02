package com.cyes.webserver.domain.stompSocket.service;

import com.cyes.webserver.domain.problem.dto.ProblemResponse;
import com.cyes.webserver.domain.problem.service.ProblemService;
import com.cyes.webserver.domain.quizproblem.repository.QuizProblemRepository;
import com.cyes.webserver.domain.rank.dto.GradingResult;
import com.cyes.webserver.domain.stompSocket.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final ChannelTopic channelTopic;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate StringRedisTemplate;
    private final QuizProblemRepository quizProblemRepository;
    private final ProblemService problemService;
    private final ObjectMapper objectMapper;

    //client에게 퀴즈쇼 시작 신호를 전송하는 메서드
    public List<ProblemResponse> startSession(Long quizId) {
        // 퀴즈 문제 pk 조회
        List<String> list = quizProblemRepository.findQuizProblems(quizId);
        // (문제, 정답) 리스트 조회
        List<ProblemResponse> problemAnswerList = problemService.findAllProblemByQuiz(list);
        // 클라이언트한테 시작 신호 보내기
        redisTemplate.convertAndSend(channelTopic.getTopic(), new SessionMessage(quizId, SessionMessage.MessageType.START));

        return problemAnswerList;
    }


    //client에게 풀어야 할 문제를 전송하는 메서드
    public void sendProblem(Long quizId, ProblemResponse problem) throws JsonProcessingException {
        ProblemMessage problemMessage = ProblemMessage.builder()
                .sessionId(quizId)
                .type(SessionMessage.MessageType.PROBLEM)
                .question(problem.getContentResponse().getQuestion())
                .build();

        // 클라이언트한테 문제 보내기
        redisTemplate.convertAndSend(channelTopic.getTopic(), problemMessage);

        /*
           Redis에 특정 문제를 보낸 시간을 저장한다.
         */

        // key : quiz_id_problemOrder ( 퀴즈번호_문제순서 )
        String key = quizId + "_" + problem.getProblemOrder();
        // value : LocalDateTime.now() ( 문제를 보낸 시간 )
        setStringDateRedis(key, LocalDateTime.now().toString(), Duration.ofMinutes(30));
    }

    //client는 다음 문제로 넘어가기 전에 정답을 확인할 수 있다.
    //client한테 문제의 정답을 보여주는 메서드
    public void sendAnswer(Long quizId, ProblemResponse problem) {
        AnswerMessage answerMessage = AnswerMessage.builder()
                .sessionId(quizId)
                .type(SessionMessage.MessageType.ANSWER)
                .answer(problem.getContentResponse().getAnswer())
                .build();

        // 클라이언트한테 답 보내기
        redisTemplate.convertAndSend(channelTopic.getTopic(), answerMessage);
    }

    //client에게 퀴즈가 종료되었음을 알리는 메서드
    public void sendEnd(Long quizId) {

        SessionMessage endMessage = new SessionMessage(quizId, SessionMessage.MessageType.END);

        redisTemplate.convertAndSend(channelTopic.getTopic(), endMessage);
    }

    //client에게 최종 순위를 보내주는 메서드
    public void sendResult(Long quizId, List<ProblemResponse> problemResponseList) throws JsonProcessingException {

        List<SubmitRedis> list = getSubmitDataRedis(quizId.toString());
        //채점을 완료해서 순서를 매긴 값의 리스트 이다.

        List<GradingResult> gradingResultList = getGradingResultList(list, problemResponseList);

        //채점 결과를 담고있는 메시지양
        SessionMessage resultMessage = ResultMessage.builder()
                .quizId(quizId)
                .gradingResultList(gradingResultList)
                .type(SessionMessage.MessageType.RESULT)
                .build();

        //Redis에 publish
        redisTemplate.convertAndSend(channelTopic.getTopic(), resultMessage);
    }

    public List<GradingResult> getGradingResultList(List<SubmitRedis> answerList, List<ProblemResponse> problemList) {
        //key : memgerId, value : 채점 결과
        Map<Long, GradingResult> resultMap = new HashMap<>();

        for (int i = 0; i < answerList.size(); i++) {
            SubmitRedis answer = answerList.get(i);
            //제출 답안
            String submit = answer.getSubmitContent();

            //memberId
            Long memberId = answer.getMemberId();


            //문제 번호
            Integer problemNumber = answer.getProblemOrder();

            //문제의 정답
            String problemAnswer = problemList.get(problemNumber - 1).getContentResponse().getAnswer();

            //제출 답안이랑 정답이랑 같으면
            if (submit.equals(problemAnswer)) {
                GradingResult result = resultMap.getOrDefault(memberId, GradingResult.builder().memberId(memberId).build());

                result.addCorrectCount();
                result.addDuringTime(answer.getDuringTime());

                resultMap.put(memberId, result);
            }

        }
        List<GradingResult> resultList = new ArrayList(resultMap.values());
        Collections.sort(resultList);
        System.out.println("resultList = " + resultList);

        return resultList;
    }




    public void handleEnter(SessionMessage message) {

    }

    /**
     * 클라이언트가 보낸 답안을 Redis에 기록하는 메소드.
     */
    public void handleSubmit(SubmitMessage message) throws JsonProcessingException {
        // key : 퀴즈번호_문제순서_멤버id
        String key = message.createKey();

        // value : SubmitDto
        SubmitRedis submitRedis = message.ToSubmitRedis(LocalDateTime.parse(getDataFromRedis(getRedisKey(message))),LocalDateTime.now());

        // redis에 제출 정보 저장
        setSubmitDateRedis(key, submitRedis, Duration.ofMinutes(30));
    }

    public void handleChat(ChatMessage message) {

    }


    public String getDataFromRedis(String key) throws JsonProcessingException {

        return StringRedisTemplate.opsForValue().get(key);
    }

    public List<SubmitRedis> getSubmitDataRedis(String key) {

        // 클라이언트가 제출한 정보를 keypattern(submit_퀴즈id)으로 검색
        String keyPattern = "submit_" + key + "*";

        // keypattern으로 검색이 된 key들을 select
        Set<String> values = StringRedisTemplate.keys(keyPattern);

        // Redis에서 가져온 제출 정보를 담을 List 선언
        List<SubmitRedis> submitRedisList = new ArrayList<>();

        // 역직렬화를 위한 objectMapper 선언.
        ObjectMapper objectMapper = new ObjectMapper();
        for (String keyStr : values) {
            try {
                // 제출 정보 key값으로 검색하여 직렬화하여 스트링으로 저장된 제출 정보를 가져온다.
                String valueStr = getDataFromRedis(keyStr);

                // 가져온 스트링 제출 정보를 SubmitRedis 객체 형태로 역직렬화한다.
                SubmitRedis submitRedis = objectMapper.readValue(valueStr, SubmitRedis.class);

                // 역직렬화한 SubmitRedis 객체를 List에 추가.
                submitRedisList.add(submitRedis);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return submitRedisList;
    }


    public void setStringDateRedis(String key, String value, Duration duration) {

        ValueOperations<String, String> stringListValueOperations = StringRedisTemplate.opsForValue();

        // Redis에 출제 시간 데이터 저장
        stringListValueOperations.set(key, value, duration);
    }

    public void setSubmitDateRedis(String key, SubmitRedis submitRedis, Duration duration) throws JsonProcessingException {


        ValueOperations<String, String> valueOperations = StringRedisTemplate.opsForValue();

        String valueStr = objectMapper.writeValueAsString(submitRedis);
        // Redis에 제출 정보 데이터 저장
        valueOperations.set(key, valueStr, duration);

    }

    public String getRedisKey(SubmitMessage message) {
        return message.getQuizId() + "_" + message.getProblemOrder();
    }

}
