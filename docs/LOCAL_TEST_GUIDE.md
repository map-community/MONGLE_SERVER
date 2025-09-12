# Kareer-fit 로컬 개발 환경 가이드

이 문서는 Docker를 사용하여 로컬 환경에서 `career-fit` 프로젝트를 실행하는 방법을 안내합니다.

## **사전 준비 사항 (Prerequisites)** 🛠️

-   **Docker Desktop**: [Docker 공식 홈페이지](https://www.docker.com/products/docker-desktop/)에서 자신의 OS에 맞는 버전을 설치하세요.

---

## **실행 방법 (Run)** 🚀

### **1. `.env` 파일 생성**

프로젝트 실행에 필요한 환경 변수 설정을 위해, 프로젝트의 가장 상위 경로(root)에 `.env` 파일을 생성해주세요.

> **📝 참고:** `.env` 파일에 들어가야 할 구체적인 내용은 별도 채널을 통해 공유됩니다.

### **2. Docker Compose 실행**

프로젝트 경로의 터미널에서 아래 명령어를 입력하여 모든 서비스를 실행합니다.

```bash
docker compose -f docker-compose.local.yml up --build -d
```

-   `up --build`: 소스 코드 변경이 있을 때 이미지를 새로 빌드하며 컨테이너를 실행합니다. (최초 실행 시 필수)
-   `-d`: Detached 모드의 약자로, 컨테이너를 백그라운드에서 실행하라는 의미입니다.
---

## **실행 확인 (Verification)** ✅

-   **컨테이너 목록 확인**
    새 터미널을 열고 `docker ps` 명령어를 입력했을 때, `career-fit-app`, `mysql-db`, `redis-cache` 컨테이너가 모두 `Up` 상태로 보이면 성공입니다.

-   **애플리케이션 접속**
    웹 브라우저에서 `http://localhost:8080`으로 접속하여 서비스 동작을 확인합니다.

-   **로그 확인**
    -   **실시간 로그 보기:**
        ```bash
        docker compose -f docker-compose.local.yml logs -f app
        ```
    -   **마지막 50줄 로그만 보기 (tail):**
        ```bash
        docker compose -f docker-compose.local.yml logs --tail 50 app
        ```

---

## **종료 방법 (Stop)** 🛑

테스트를 마친 후, 아래 명령어를 입력하면 실행했던 모든 컨테이너를 깔끔하게 종료하고 관련 리소스를 삭제합니다.

-   **도커 컨테이너 종료**
    -   **컨테이너 중지 및 삭제**
        ```bash
        docker compose -f docker-compose.local.yml down
        ```
    -  **컨테이너 중지**
        ```bash
        docker compose -f docker-compose.local.yml stop
        ```