package com.cyes.webserver.domain.quiz.controller;

import com.cyes.webserver.domain.quiz.dto.*;
import com.cyes.webserver.domain.quiz.service.QuizService;
import com.cyes.webserver.domain.stompSocket.dto.SessionMessage;
import com.cyes.webserver.domain.stompSocket.service.MessageService;
import com.cyes.webserver.exception.CustomException;
import com.cyes.webserver.exception.CustomExceptionList;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final MessageService messageService;


    
    /*
    라이브 퀴즈쇼 정보 조회 API
    */
    @GetMapping("/live/info")
    private ResponseEntity<QuizInfoResponse> getQuizInfo() {

        return ResponseEntity.status(HttpStatus.OK).body(quizService.searchQuiz());

    }

    @GetMapping("/test/{type}")
    private void getProblemAnswer(@PathVariable("type") String type) throws JsonProcessingException {
        SessionMessage sessionMessage = new SessionMessage();

        if(type.equals("START")) {
            sessionMessage.setType(SessionMessage.MessageType.START);
        } else if(type.equals("QUESTION")) {
            sessionMessage.setType(SessionMessage.MessageType.QUESTION);
        } else if(type.equals("ANSWER")) {
            sessionMessage.setType(SessionMessage.MessageType.ANSWER);
        } else if(type.equals("END")){
            sessionMessage.setType(SessionMessage.MessageType.END);
        } else if(type.equals("RESULT")) {
            sessionMessage.setType(SessionMessage.MessageType.RESULT);
        }

        messageService.sendToUsers(sessionMessage);
    }


    /*
    라이브 퀴즈쇼 개설 APi
     */
    @PostMapping
    private ResponseEntity<QuizCreateResponse> createQuiz(@RequestBody QuizCreateRequest quizCreateRequest) {

        // service로 보내는 Dto로 변환
        QuizCreateRequestToService quizCreateRequestToService = quizCreateRequest.create();

        // service 호출
        QuizCreateResponse quizCreateResponse = quizService.createQuiz(quizCreateRequestToService);

        return ResponseEntity.status(HttpStatus.OK).body(quizCreateResponse);
    }

}
