#!/bin/bash

# Jenkins 상태 확인 스크립트

echo "🔍 Jenkins 상태 확인 중..."
echo "================================"

# 컨테이너 상태 확인
echo "📦 Docker 컨테이너 상태:"
docker ps -a | grep jenkins

echo ""
echo "🌐 웹 서버 상태:"
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080)
if [ "$HTTP_STATUS" -eq 200 ] || [ "$HTTP_STATUS" -eq 403 ]; then
    echo "✅ Jenkins 웹 서버 실행 중 (HTTP $HTTP_STATUS)"
    echo "🔗 접속 URL: http://localhost:8080"
else
    echo "❌ Jenkins 웹 서버에 접속할 수 없음 (HTTP $HTTP_STATUS)"
fi

echo ""
echo "💾 볼륨 정보:"
docker volume ls | grep jenkins

echo ""
echo "📊 리소스 사용량:"
docker stats jenkins --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}" 2>/dev/null || echo "Jenkins 컨테이너가 실행 중이지 않음"

echo ""
echo "🔧 유용한 명령어:"
echo "  - Jenkins 시작: docker start jenkins"
echo "  - Jenkins 중지: docker stop jenkins"
echo "  - Jenkins 재시작: docker restart jenkins"
echo "  - Jenkins 로그: docker logs jenkins"
echo "  - 실시간 로그: docker logs -f jenkins" 