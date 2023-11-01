import React, { useEffect, useState } from "react";
import BottomNav from "../../components/bottomnav/BottomNav";
import "./Live.css";
import CountdownTimer from "../../components/CountdownTimer";
import RoundCornerBtn from "../../components/RoundCornerBtn";
import { useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import { getMainQuizInfo } from "../../api/QuizAPI";

type Props = {};

type Quiz = {
    quizId: number;
    quizTitle: string;
    quizStartDate: Date;
};

const Live = (props: Props) => {
    const [mainQuiz, setMainQuiz] = useState<Quiz>({
        quizId: -1,
        quizTitle: "퀴즈 일정이 없습니다",
        quizStartDate: new Date(),
    });

    const [joinable, setJoinable] = useState<boolean>(false);

    const navigate = useNavigate();
    const memberInfo = useSelector((state: any) => state.member);

    const getLiveInfo = async () => {
        const mainQuizInfo = await getMainQuizInfo();

        if (mainQuizInfo == null) {
            return;
        }

        setMainQuiz({
            ...mainQuizInfo,
            quizStartDate: new Date(mainQuizInfo.quizStartDate),
        });
    };

    useEffect(() => {
        getLiveInfo();
    }, []);

    const enterRoom = () => {
        // 다른 페이지로 이동
        navigate("/quiz");
    };

    return (
        <div className="live-container">
            <div className="content">
                <div className="title-text">
                    <p>SSAFY</p>
                    <img src="/img/live_logo.png" alt=""></img>
                </div>
                <CountdownTimer
                    targetHour={mainQuiz.quizStartDate.getHours()}
                    targetMin={mainQuiz.quizStartDate.getMinutes()}
                />
                <p style={{ fontSize: "26px" }}>{mainQuiz.quizTitle}</p>
                {joinable && (
                    <RoundCornerBtn
                        width="150px"
                        height="45px"
                        bgcolor="#FF5733"
                        bghover="#853828"
                        fontSize="16px"
                        fontColor="#FFFFFF"
                        onClick={enterRoom}
                    >
                        대기실 입장
                    </RoundCornerBtn>
                )}
            </div>
            <BottomNav checkCS={false} checkLive={true} checkGroup={false} />
        </div>
    );
};

export default Live;
