import React, { useState } from "react";
import "./quizwordcreate.css";
import axios from "axios"; // Axios 라이브러리 import
const QuizWordCrate = () => {
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState("");
  const [category, setCategory] = useState("");
  const [choice1, setChoice1] = useState("");
  const [choice2, setChoice2] = useState("");
  const [choice3, setChoice3] = useState("");

  const noCheckProblemWord = () => {
    const quizWordRequest = {
      question: question,
      answer: answer,
      choices: [choice1, choice2, choice3],
    };

    const noCheckProblemWord = () => {
      // Axios를 사용하여 API 호출 수행
      axios
        .post(
          `http://localhost:5000/api/adminproblem/yes-four-select-insert?category=${category}`,
          quizWordRequest
        )
        .then((response) => {
          console.log("API 응답 데이터: ", response.data);
          // 원하는 작업 수행
        })
        .catch((error) => {
          console.error("API 호출 중 오류 발생: ", error);
        });
    };
  };
  return (
    <div className="login-container">
      <div className="fourquiz">
        4지선다 생성
        <br></br>
        <br></br>
        <textarea
          // type="text"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          placeholder="문제를 입력하세요."
          className="text-field"
          rows={8}
          cols={40}
        />
        <br></br>
        <input
          type="text"
          value={answer}
          onChange={(e) => setAnswer(e.target.value)}
          placeholder="정답을 입력하세요."
          className="input-field"
        />
        <br></br>
        <input
          type="text"
          value={choice1}
          onChange={(e) => setChoice1(e.target.value)}
          placeholder="오답1을 입력하세요."
          className="input-field"
        />
        <br></br>
        <input
          type="text"
          value={choice2}
          onChange={(e) => setChoice2(e.target.value)}
          placeholder="오답2을 입력하세요."
          className="input-field"
        />
        <br></br>
        <input
          type="text"
          value={choice3}
          onChange={(e) => setChoice3(e.target.value)}
          placeholder="오답3을 입력하세요."
          className="input-field"
        />
        <br></br>
        <input
          type="text"
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          placeholder="카테고리를 입력하세요."
          className="input-field"
        />
        <br></br>
        <button
          onClick={noCheckProblemWord}
          className="no-check-problem-word-button"
        >
          문제 만들기
        </button>
      </div>
    </div>
  );
};

export default QuizWordCrate;
