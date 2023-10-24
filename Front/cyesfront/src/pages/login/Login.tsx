import React from 'react';
import './Login.css';
import { useKakaoLogin } from '../../hooks/useKakaoLogin';
import BottomNav from '../../components/BottomNav';

// env로 변경
const REST_API_KEY = "17a1b4d9fcc605cecc82fd7399c0717f"; 
const REDIRECT_URI = "http://localhost:3000/login/kakao/callback"; 
const KAKAO_AUTH_URI = `https://kauth.kakao.com/oauth/authorize?client_id=${REST_API_KEY}&redirect_uri=${REDIRECT_URI}&response_type=code`;

const Login = () => {
    // useKakaoLogin(); // 카카오 로그인 Hook 사용

    const handleKakaoLogin = () => {
        window.location.href = KAKAO_AUTH_URI;
    };

    const naverLogin = async () => {
    };

    return (
        <div className='login-container'>
            <img className='logo-img' src='/img/cyes_logo_img.png' alt='logo img'/>
            <button onClick={handleKakaoLogin} className="login-button kakao-login"/>
            <button onClick={naverLogin} className="login-button naver-login" />
            <BottomNav></BottomNav>
        </div>
    );
};

export default Login;
