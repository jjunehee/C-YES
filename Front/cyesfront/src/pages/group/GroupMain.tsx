import React, { useState, useEffect } from "react";
import "./GroupMain.css";
import BottomNav from "../../components/bottomnav/BottomNav";

type Props = {};

const GroupMain = (props: Props) => {
  const [currentTime, setCurrentTime] = useState(
    new Date().toLocaleTimeString()
  );

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date().toLocaleTimeString());
    }, 1000); // 매 1초마다 시간 업데이트

    return () => clearInterval(timer); // 컴포넌트 언마운트 시 인터벌 제거
  }, []);

  return (
    <div className="">
      <div className="group-main-container">
        <div className="group-title-container">그룹퀴즈</div>
        <div className="group-description-container">
          <span>그룹을 결성해 퀴즈를 만들고{"\n"}</span>
          <span>방에 접속해 라이브 퀴즈를 풀어보아요!</span>
        </div>
        <div className="group-search-container">
          <img src="/icon/search.png" alt=""></img>
          <input placeholder="퀴즈 방 검색"></input>
        </div>
        <div className="fast-quiz-container">
          <div className="quiz-text-container">빠른 퀴즈</div>
          <div className="fast-quiz-lineup-container">
            <div className="fast-quiz-box">
              <div className="quiz-box-title">수인이 문제집</div>
              <div className="quiz-box-text">{currentTime}</div>
              <div className="quiz-box-text">문제수: 20개</div>
              <div className="quiz-box-text">출제자: 5반 킹카</div>
              <div className="quiz-box-text"># 네트워크, OS</div>
            </div>
            <div className="fast-quiz-box">
              <div className="quiz-box-title">태영이와 아이들</div>
              <div className="quiz-box-text">{currentTime}</div>
              <div className="quiz-box-text">문제수: 20개</div>
              <div className="quiz-box-text">출제자: 4반 킹카</div>
              <div className="quiz-box-text"># 자바의 근본</div>
            </div>
          </div>
          <div className="group-result-container">
            <div className="quiz-text-container">검색된 퀴즈</div>
            <div className="result-quiz-box"></div>
          </div>
        </div>
      </div>
      <div className="bottom-nav">
        <BottomNav checkCS={false} checkLive={false} checkGroup={true} />
      </div>
    </div>
  );
};

export default GroupMain;
