package com.cyes.webserver.redis.listener;

import com.cyes.webserver.domain.problem.dto.ProblemResponse;
import com.cyes.webserver.domain.stompSocket.service.answer.AnswerService;
import com.cyes.webserver.domain.stompSocket.service.end.EndService;
import com.cyes.webserver.domain.stompSocket.service.problem.ProblemService;
import com.cyes.webserver.domain.stompSocket.service.result.ResultService;
import com.cyes.webserver.domain.stompSocket.service.start.StartService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskFactory {

    private final StartService startService;
    private final ProblemService problemService;
    private final AnswerService answerService;
    private final EndService endService;
    private final ResultService resultService;


    public void quizProcedureTask(Long quizId) throws InterruptedException, JsonProcessingException {

        List<ProblemResponse> problems; // 문제 찾아오기
        long solvableTime = 20000L; // 기본 20초

        problems = startService.startSession(quizId);
        System.out.println("problems = " + problems);
        Thread.sleep(1000);

        for (ProblemResponse problem : problems) {
            problemService.sendProblem(quizId, problem);
            Thread.sleep(solvableTime);

            answerService.sendAnswer(quizId, problem);
            Thread.sleep(3000);
        }

        endService.sendEnd(quizId);
        Thread.sleep(1000);
        resultService.sendResult(quizId,problems);
    }
}
