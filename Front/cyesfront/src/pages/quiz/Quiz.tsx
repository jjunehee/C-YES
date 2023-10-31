import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import RoundCornerBtn from "../../components/RoundCornerBtn";
import "./Quiz.css";
import SockJS from "sockjs-client";
import Stomp from "stompjs";
import { useSelector } from "react-redux";

interface ModalProps {
  showModal: boolean;
  showContent: boolean;
  toggleContent: () => void;
}

function Modal(props: ModalProps) {
  const { showModal, showContent, toggleContent } = props;
  const navigate = useNavigate();

  const moveMain = () => {
    //메인페이지 이동
    navigate("/live");
  };

  const handleCompleteClick = () => {
    toggleContent();
    // 랭킹 산정 완료 버튼을 클릭하면 랭킹내용 표시
  };

  // showModal이 false일 경우 null 반환
  if (!showModal) {
    return null;
  }

  return (
    showModal && (
      <div className="modal">
        <div className="modal-content">
          {showContent ? (
            <div className="modal-items">
              <div className="loading-background">
                <div className="loading-topic">
                  <p>🏆 오늘의 랭킹</p>
                </div>
              </div>

              <div className="rank-content">
                <p>1등 00 </p>
                <p>2등 00 </p>
                <p>3등 00 </p>
              </div>

              <RoundCornerBtn type="submit" onClick={moveMain} bgHover="black">
                메인
              </RoundCornerBtn>
            </div>
          ) : (
            //로딩중
            <div className="rank-loading">
              <div className="loading-background">
                <div className="end-topic">🥁 퀴즈 종료</div>
              </div>

              <div className="loading-text">순위 산정 중</div>
              <img src="/img/loading.gif" alt="로딩 중" width={60}></img>
              <button onClick={handleCompleteClick}>산정 완료</button>
            </div>
          )}
        </div>
      </div>
    )
  );
}

const Quiz: React.FC = () => {
  const [questions, setQuestions] = useState([
    {
      question:
        "프로그래밍에 집중한 유연한 개발 방식으로 상호작용, 소프트웨어, 협력, 변화 대응에 가치를 두는 이것은?",
      answer: "애자일",
    },
    { question: "두 번째 질문", answer: "아보카도" },
    { question: "세 번째 질문", answer: "답3" },
  ]);

  const [currentQuestion, setCurrentQuestion] = useState(0);
  const [progress, setProgress] = useState(0);
  const [submitted, setSubmitted] = useState(false);
  const [showConfirmation, setShowConfirmation] = useState(false);
  const answerInput = useRef<HTMLTextAreaElement | null>(null);
  const [isTextareaEnabled, setIsTextareaEnabled] = useState(true);
  const [textareaValue, setTextareaValue] = useState("");

  const [showModalContent, setShowModalContent] = useState(false);
  const [isQuizStarted, setIsQuizStarted] = useState(false);

  const [isThisQuestionStarted, setIsThisQuestionStarted] = useState(false);

  const handleTextareaChange = (
    event: React.ChangeEvent<HTMLTextAreaElement>
  ) => {
    setTextareaValue(event.target.value);
  };

  const startQuiz = () => {
    setIsQuizStarted(true);
  };

  const startThisQuestion = () => {
    setIsThisQuestionStarted(true);
  };
  const toggleSubmit = () => {
    //여기서 backend랑 통신하면 댈듯
    if (!submitted) {
      setSubmitted(true); // 제출 완료 상태로 설정
      setIsTextareaEnabled(false); //textarea 비활성화
    }
  };

  const toggleContent = () => {
    setShowModalContent(true);
  };

  const [showModal, setShowModal] = useState(false);

  const openModal = () => {
    setShowModal(true);
  };

  const handleConfirmAnswer = () => {
    // 정답 확인 버튼을 눌렀을 때
    setShowConfirmation(true);

    if (currentQuestion < questions.length - 1) {
      setTimeout(() => {
        setShowConfirmation(false);

        // 문제 바뀌는 구간
        setSubmitted(false);
        setCurrentQuestion(currentQuestion + 1);

        // textarea 활성화
        setIsTextareaEnabled(true);
        setTextareaValue("");

        setIsThisQuestionStarted(false);
        setProgress(0);
      }, 3000); // 3초 후에 다음 문제로 이동
    }
  };

  // 웹소켓 연결

  const [webSocket, setWebSocket] = useState<Stomp.Client>();

  // redux 에서 가져오기
  const quizState = useSelector((state: any) => state.quiz.quiz);
  // console.log("리덕스에서 가져온 quiz: ", quizState);
  const quizId = quizState.quizId;
  const submit = quizState.submit;
  const answer = quizState.answer;

  const memberState = useSelector((state: any) => state.member.member);
  const memberId = memberState.memberId;
  //const memberId

  // 메세지 받았을 시 컨트롤 함수
  const messageHandler = (recv: any) => {
    switch (recv.type) {
      case "START":
        // 문제 받을 준비
        return;

      case "PROBLEM":
        // 문제랑 답 숫자를 state에 저장
        // 문제 출력
        return;

      case "ANSWER":
        // 답을 answer redux state에 저장
        // 내가 제출한 답 submit과, answer의 같은 인덱스를 비교해서 정답인지 출력
        return;

      case "END":
        // 모든 제출 정답에 대해 총 점수 계산해서 점수를 state 에 저장
        // 계산만 해놓고 기다리기 모달 띄우기
        return;

      case "RESULT":
        // 결과를 받아와서 띄우기
        // 내 총점도 띄우기
        // 모든 처리 완료 하면
        webSocket?.disconnect(() => {});
        return;

      default:
      // 이건 와서는 안됨. 에러 처리
    }
  };

  // 답안 제출 웹소켓 전송
  const sendSubmit = (data: any) => {
    webSocket?.send(
      "/pub/session/message/submit",
      {},
      JSON.stringify({
        quizId: quizId,
        type: "SUBMIT",
        memberId: memberId,
        submit: data,
      })
    );
  };

  useEffect(() => {
    // 리덕스 연결해서 정보 가져오면 지워버리세요
    // const ws = new Client({
    //     brokerURL: `ws://localhost:5000/quiz/session`,
    //     debug(str) {
    //         console.log(`debug`, str);
    //     },
    //     reconnectDelay: 5000,
    //     connectionTimeout: 100000,
    //     heartbeatIncoming: 4000,
    //     heartbeatOutgoing: 4000,
    // });

    // ws.onConnect = (frame) => {
    //     ws.subscribe("/sub/quiz/session/" + sessionId, (message) => {
    //         // recv 콜백 함수
    //         const recvData = JSON.parse(message.body);
    //         console.log(recvData);
    //         //TODO: 메세지 타입별로 처리
    //     });

    //     ws.publish({
    //         destination: "/pub/session/message",
    //         body: JSON.stringify({
    //             type: "ENTER",
    //             sessionId: sessionId,
    //             senderId: memberId,
    //             senderNickname: memberNickname,
    //             message: "안녕하세요?",
    //         }),
    //     });
    // };

    // ws.onStompError = (frame) => {
    //     console.log(`Broker reported Error`, frame.headers.message);
    //     console.log(`Additional details:${frame.body}`);
    // };

    // ws.activate();

    // setWebSocket(ws);

    const sock = new SockJS(`${process.env.REACT_APP_CLIENT_URI}/quiz/session`);
    const ws = Stomp.over(sock);

    ws.connect(
      {},
      (frame) => {
        ws.subscribe("/sub/quiz/session/" + quizId, (message) => {
          // recv 콜백 함수
          console.log("메세지 받았다");
          const recvData = JSON.parse(message.body);
          console.log(recvData);
          messageHandler(recvData);
        });
        ws.send(
          "/pub/session/message/enter",
          {},
          JSON.stringify({
            type: "ENTER",
            quizId: quizId.current,
          })
        );
        setWebSocket(ws);
      },
      (err) => {
        console.log(err);
      }
    );
  }, []);

  useEffect(() => {
    const timer = setInterval(() => {
      if (isQuizStarted && progress >= 100) {
        clearInterval(timer);
        if (currentQuestion < questions.length - 1) {
          setIsTextareaEnabled(false);
          //   setShowConfirmation(true);

          //   setTimeout(() => {
          //     setShowConfirmation(false);

          //     // 문제 바뀌는 구간
          //     setSubmitted(false);
          //     setCurrentQuestion(currentQuestion + 1);

          //     // textarea 활성화
          //     setIsTextareaEnabled(true);
          //     setTextareaValue("");

          //     setIsThisQuestionStarted(false);
          //     setProgress(0);
          //   }, 3000); // 3초 후에 다음 문제로 이동
        } else {
          // 마지막 문제일 때도 답을 보여주도록 수정
          setIsTextareaEnabled(false);
          //setShowConfirmation(true);

          setTimeout(() => {
            setShowConfirmation(false);

            // modal 표시 코드
            openModal();
          }, 3000);
        }
      } else if (isQuizStarted && isThisQuestionStarted) {
        setProgress(progress + 0.2);
      }
    }, 0.5); // 0.01초마다 업데이트 (1000 이 1초)

    return () => {
      clearInterval(timer);
    };
  }, [
    progress,
    currentQuestion,
    questions,
    isQuizStarted,
    isThisQuestionStarted,
  ]);

  return (
    <div className="container">
      <img className="live-logo-img" src="/img/live_logo.png" alt="" />
      <div className="head">SSA피드 퀴즈</div>

      <div className="form">
        <div className="form-group">
          <div className="quiz-container">
            <div className="quiz">
              <div className="quiz-content">
                {isQuizStarted ? (
                  isThisQuestionStarted ? (
                    questions[currentQuestion].question
                  ) : (
                    <div>
                      <button onClick={startThisQuestion}>문제 도착</button>
                    </div>
                  )
                ) : (
                  <div>
                    <div>대기 중</div>
                    <button onClick={startQuiz}>시작하기</button>
                  </div>
                )}
              </div>
            </div>
          </div>

          <div>
            {isQuizStarted && isThisQuestionStarted ? (
              <div className="answer-box" style={{ display: "flex" }}>
                {Array.from({
                  length: questions[currentQuestion].answer.length,
                }).map((_, index) => (
                  <div key={index} className="square">
                    {showConfirmation
                      ? questions[currentQuestion].answer[index]
                      : null}
                  </div>
                ))}
              </div>
            ) : (
              <div></div>
            )}
          </div>
          {isQuizStarted ? (
            isThisQuestionStarted ? (
              // questions[currentQuestion].question

              <div className="input-content">
                <div>
                  <textarea
                    ref={answerInput}
                    id="answer-input"
                    name="content"
                    value={textareaValue}
                    onChange={handleTextareaChange}
                    disabled={!isTextareaEnabled} // 비활성화 상태 조절
                    style={{
                      backgroundColor: isTextareaEnabled
                        ? "white"
                        : "lightgray", // 배경색 제어
                      color: isTextareaEnabled ? "black" : "gray", // 텍스트 색상 제어
                    }}
                    placeholder={isTextareaEnabled ? "입력하세요" : " "} // placeholder 설정
                  />
                </div>

                <div>
                  <RoundCornerBtn
                    type="submit"
                    onClick={() => toggleSubmit()}
                    bgColor={submitted ? "#265587" : undefined}
                    bgHover="#265587"
                    disabled={submitted}
                  >
                    {submitted ? "제출 완료" : "제출"}
                  </RoundCornerBtn>
                </div>

                <div>
                  {/* <button onClick={startThisQuestion}>서버 연결</button> */}
                  <button onClick={handleConfirmAnswer}>정답 도착</button>
                </div>
              </div>
            ) : (
              <div></div>
            )
          ) : (
            <div>
              <textarea placeholder="퀴즈가 곧 시작합니다!" disabled />
            </div>
          )}
        </div>

        <div>
          <ProgressBar progress={progress} />
        </div>
      </div>

      <Modal
        showModal={showModal}
        showContent={showModalContent}
        toggleContent={toggleContent}
      />
    </div>
  );
};

const ProgressBar: React.FC<{ progress: number }> = ({ progress }) => (
  <div className="progress-bar">
    <div
      style={{
        width: `${progress}%`,
        backgroundColor: progress >= 80 ? "red" : "blue",
        height: "10px",
      }}
    />
  </div>
);

export default Quiz;
