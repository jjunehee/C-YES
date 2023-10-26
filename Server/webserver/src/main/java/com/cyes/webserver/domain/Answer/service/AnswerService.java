package com.cyes.webserver.domain.Answer.service;

import com.cyes.webserver.domain.Answer.dto.AnswerResponse;
import com.cyes.webserver.domain.Answer.dto.AnswerSaveServiceRequest;
import com.cyes.webserver.domain.Answer.entity.Answer;
import com.cyes.webserver.domain.Answer.repository.AnswerRepository;
import com.cyes.webserver.domain.member.entity.Member;
import com.cyes.webserver.domain.member.repository.MemberRepository;
import com.cyes.webserver.domain.problem.dto.ProblemResponse;
import com.cyes.webserver.domain.problem.repository.ProblemRepository;
import com.cyes.webserver.domain.quiz.repository.QuizRepository;
import com.cyes.webserver.exception.CustomException;
import com.cyes.webserver.exception.CustomExceptionList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.cyes.webserver.exception.CustomExceptionList.QUIZ_NOT_FOUND_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService{
    private final MemberRepository memberRepository;
    private final AnswerRepository answerRepository;
    private final QuizRepository quizRepository;
    private final ProblemRepository problemRepository;

    /**답안을 제출하는 메서드
     * 같은 문항에 이미 답을 제출했는지 체크하고
     * 제출하지 않았다면 저장한다.
    */
    @Transactional
    public AnswerResponse save(AnswerSaveServiceRequest answerSaveServiceRequest){
        Long memberId = answerSaveServiceRequest.getMemberId();
        Long quizId = answerSaveServiceRequest.getQuizId();
        Integer problemNumber = answerSaveServiceRequest.getProblemNumber();

        //존재하는 멤버인지 확인
//        memberRepository.findById(memberId).orElseThrow(()-> {throw new CustomException(CustomExceptionList.MEMBER_NOT_FOUND_ERROR);});
        //퀴즈 존재하는지 확인
//        quizRepository.findById(quizId).orElseThrow(() -> {throw new CustomException(CustomExceptionList.QUIZ_NOT_FOUND_ERROR);});

        //이미 답을 제출한 적 있는지 확인
        Optional<Answer> findOptionalAnswer = answerRepository.findAnswerByMemberIdAndQuizIdAndProblemNumber(memberId, quizId, problemNumber);

        if (findOptionalAnswer.isPresent()){
            throw new CustomException(CustomExceptionList.ALREADY_SUBMIT);
        }

        //Dto -> Entity
        Answer answer = answerSaveServiceRequest.toEntity();

        //save
        answerRepository.save(answer);

        //Entity -> Dto
        AnswerResponse answerResponse = answer.toAnswerResponse();

        return answerResponse;
    }

    //Sort정보를 Pageable에 넘겨줘야 한다.
    public List<AnswerResponse> findAnswerByMemberIdAndQuizId(Long memberId, Long quizId){
        //problem_number를 기준으로 오름차순 정렬을 해주는 Sort 객체 생성한다.
        Sort sort = Sort.by(Sort.Order.asc("problem_number"));

        //memberId, quizId, Sort를 파라미터로 넘겨준다.
        List<Answer> answerList = answerRepository.findAnswerByMemberIdAndQuizId(memberId, quizId, sort);

        //Response로 바꿔준다.
        List<AnswerResponse> answerResponseList = toAnswerResponse(answerList);

        //반환
        return answerResponseList;
    }

    private List<AnswerResponse> toAnswerResponse(List<Answer> answerList){

        List<AnswerResponse> list = new ArrayList<>();

        for (Answer answer : answerList) {
            AnswerResponse answerResponse = answer.toAnswerResponse();
            list.add(answerResponse);
        }
        return list;
    }
}