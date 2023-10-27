package com.cyes.webserver.domain.stompSocket.service;

import com.cyes.webserver.domain.problem.dto.ProblemResponse;
import com.cyes.webserver.domain.problem.service.ProblemService;
import com.cyes.webserver.domain.quiz.service.QuizService;
import com.cyes.webserver.domain.quizproblem.repository.QuizProblemRepository;

import com.cyes.webserver.domain.stompSocket.dto.AnswerMessage;
import com.cyes.webserver.domain.stompSocket.dto.QuestionMessage;
import com.cyes.webserver.domain.stompSocket.dto.SessionMessage;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Instanceof;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final ChannelTopic channelTopic;
    private final RedisTemplate<String,Object> redisTemplate;
    private final QuizProblemRepository quizProblemRepository;
    private final ProblemService problemService;
    private final QuizService quizService;
    private final ObjectMapper objectMapper;

    //퀴즈가 종료될 때 0으로 초기화 해야함
    private int cnt;

    @Transactional
    public void sendMessage(SessionMessage message){

        String topic = channelTopic.getTopic();
//
//        String jsonData = String.valueOf(redisTemplate.opsForValue().get(message.getBody()));
//
//        SessionMessage sessionMessage = objectMapper.readValue(jsonData,SessionMessage.class);
//
//        if(sessionMessage.getType().equals(SessionMessage.MessageType.SUBMIT)) {
//
//        }

        redisTemplate.convertAndSend(topic, message);
    }

    public void sendToUsers(SessionMessage message) throws JsonProcessingException {

        String topic = channelTopic.getTopic();

        switch (message.getType()) {
            case START:
                // 진행하는 퀴즈에 대한 문제pk 리스트 조회
                List<String> list = quizProblemRepository.findQuizProblems(quizService.searchQuiz().getQuizId());

                // (문제, 정답) 리스트 조회
                List<ProblemResponse> problemAnswerList = problemService.findAllProblemByQuiz(list);

                // Redis에 problemAnswerList(문제,정답) 넣어놓는다. 만료기한은 30분
                setDateExpire("ProblemAnswer", problemAnswerList, Duration.ofMinutes(30));

                // 클라이언트한테 시작 신호 보내기
                redisTemplate.convertAndSend(topic, message);
                break;

            case QUESTION:
                // redis에서 문제 꺼내오기
                String question = getDateFromRedis("ProblemAnswer", "question", cnt);

                QuestionMessage questionMessage = QuestionMessage.builder()
                        .type(SessionMessage.MessageType.QUESTION)
                        .question(question)
                        .build();

                // 클라이언트한테 문제 보내기
                redisTemplate.convertAndSend(topic, questionMessage);
                break;

            case ANSWER:
                // redis에서 문제 꺼내오기
                String answer = getDateFromRedis("ProblemAnswer", "answer", cnt++);

                AnswerMessage answerMessage = AnswerMessage.builder()
                        .answer(answer)
                        .build();

                // 클라이언트한테 문제 보내기
                redisTemplate.convertAndSend(topic, answerMessage);
                break;

            case END:
                // 클라이언트한테 종료 신호 보내기
                redisTemplate.convertAndSend(topic,message);
                break;

            case RESULT:


        }

    }

    public String getDateFromRedis(String key, String what, int cnt) throws JsonProcessingException {
        Object jsonData = redisTemplate.opsForValue().get(key);

        System.out.println(jsonData);
//        switch (what) {
//            case "question" :
//                return problemResponseList.get(cnt).getContentResponse().getQuestion();
//
//            case "answer" :
//                return problemResponseList.get(cnt).getContentResponse().getAnswer();
//        }

        return "";
    }


    public void setDateExpire(String key, Object problemAnswerList, Duration duration) {
        // Redis에 데이터 저장 (리스트 형식으로)
        ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();

        valueOperations.set(key,problemAnswerList,duration);

    }


}