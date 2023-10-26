package com.cyes.webserver.domain.stompSocket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionBody {
    String question;
    List<String> selections;

    public enum QuestionType {
        SINGLE, FOUR, OX
    }
}