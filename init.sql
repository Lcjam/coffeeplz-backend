-- MySQL 초기화 스크립트
-- 데이터베이스와 사용자가 이미 docker-compose.yml에서 생성되므로
-- 여기서는 기본 설정만 진행

-- UTF-8 설정 확인
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 타임존 설정
SET time_zone = '+09:00';

-- 데이터베이스 사용
USE coffeeplz;

-- 초기화 완료 로그
SELECT 'CoffeePlz Database initialized successfully!' as message; 