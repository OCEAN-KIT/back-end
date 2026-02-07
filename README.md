  
## OCEAN-KIT : 해양 생태 복원 성과의 시각화 및 공유를 위한 통합 ICT 솔루션
<img width="1536" height="1024" alt="OCEAN-KIT" src="https://github.com/user-attachments/assets/3321f4dd-a7e9-4db0-8c62-47075c9b8adc" />

**OCEAN-KIT**는 비영리 사단법인 **오션캠퍼스**의  해양 생태 복원 활동의 현장 기록이 **표준화/자산화되지 않아** 성과가 **신뢰, 후원, 참여 확산**으로 이어지기 어려운 페인 포인트를,  **기록(현장)과 승인/자산화(운영) → 성과 공개(대시보드) → 참여 확산(커뮤니티)** 의 하나로 플로우로 해결한 **3-Application 통합 솔루션**입니다.


<br>

## Awards

<p align="center">
  <img src="https://github.com/user-attachments/assets/ef9c7818-53d6-4404-9556-81661799b839" alt="제15회 피우다프로젝트" width="360" />
  <img src="https://github.com/user-attachments/assets/8cef5bf6-be97-46bc-aafb-a57c507d86c8" alt="정보통신산업진흥원상(최우수상, 1위)" width="360" />
</p>

- **수상** : 정보통신산업진흥원상(최우수상, 1위)
- **주제** : "임팩트 조직"을 위한 창의적 아이디어 발굴 및 SW개발-해양 생태 복원 성과의 시각화 및 공유를 위한 통합 ICT 솔루션
- **주최/주관** : 과학기술정보통신부, 정보통신산업진흥원(NIPA), 한국정보방송통신대연합, IMPACT SQUARE, Root Impact, MYSC, Impact Alliance, 다음세대재단, 사회적가치연구원(CSES), 전자신문(etnews)

<br>

## Applications Overview
### OC DASHBOARD
> **B2C | 바다숲 복원 성과를 데이터 기반 3D 지도로 시각화**
- **Geo Analytics**: **PostGIS + H3** 기반 공간 표준화/집계(구역·기간별 비교 가능한 지표 제공)
- **3D Visualization**: **Mapbox** 기반 3D 레이어로 복원 밀도/성과를 직관적으로 표현
- **AI Chatbot**: **On-Premise LLM(vLLM) + RAG**로 지표/문서 근거 기반 Q&A 제공
- **Forecasting**: **활착률·생존률·생물다양성** 등 핵심 지표의 **6개월 시계열 예측 모델** 제공
- **MLOps Pipeline**: **Airflow + MLflow**로 학습/배포 파이프라인 및 실험·모델 버전 관리

### OC UNDERWATER
> **C2C | 해양 복원 활동을 소셜 콘텐츠로 전환하여 자발적 확산 유도**
- **Marine Weather**: 지역 기반 **수온·풍속·파고** 요약 정보 제공
- **Species Dictionary**: MBRIS 데이터를 **내부 Taxon DB(자산화)** 로 적재, 미등록 종/동의어 등은 외부 표준 API로 보완하여 생물종 사전/검색 제공
- **Real-time Trending**: **실시간 인기 게시물/미션** 노출로 UGC 루프 강화


### OC RECORD
> **B2B | 수중 오프라인 환경에서의 기록 및 중앙 집중 관리(승인/자산화)**
- **Local-first Recording**: **IndexedDB** 기반 임시 저장 → 네트워크 확보 시 수동 제출
- **Underwater Input UX**: 장갑/방수팩 환경을 고려한 **수중 특화 커스텀 키보드 라이브러리**
- **Hybrid AI Assist**: 오프라인에서도 동작 가능한 **내장 AI 기반 입력 보조/오타 보정**
- **Wearable Extension**: **Garmin Watch App** 개발로 현장 기록/연동 가능성까지 확장

<br>

## Architecture

<img width="3419" height="576" alt="t" src="https://github.com/user-attachments/assets/1cf018a5-03f2-4bb1-bdab-c0f683caae08" />

<br>


## Stack
<div  align="center">
  
### Geo-Spatial
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white) ![PostGIS](https://img.shields.io/badge/PostGIS-3776AB?style=for-the-badge&logo=postgresql&logoColor=white) 
![H3](https://img.shields.io/badge/H3-1E8FFF?style=for-the-badge&logo=uber&logoColor=white)

### Backend
![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white) ![JPA](https://img.shields.io/badge/JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![Spring Cache](https://img.shields.io/badge/Spring%20Cache-In--memory-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### Frontend
![Next.js](https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=next.js&logoColor=white) ![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black) ![React Native](https://img.shields.io/badge/React_Native-61DAFB?style=for-the-badge&logo=react&logoColor=black) ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white) ![Mapbox](https://img.shields.io/badge/Mapbox-000000?style=for-the-badge&logo=mapbox&logoColor=white) ![TanStack Query](https://img.shields.io/badge/TanStack_Query-FF4154?style=for-the-badge&logo=react-query&logoColor=white)

### AI
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white) ![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white) ![vLLM](https://img.shields.io/badge/vLLM-FF6B6B?style=for-the-badge&logo=llama&logoColor=white) 
![sentence-transformers](https://img.shields.io/badge/sentence--transformers-FF6F00?style=for-the-badge&logo=huggingface&logoColor=white)

### MLOps
![Apache Airflow](https://img.shields.io/badge/Apache%20Airflow-017CEE?style=for-the-badge&logo=apacheairflow&logoColor=white)
![MLflow](https://img.shields.io/badge/MLflow-0194E2?style=for-the-badge&logo=mlflow&logoColor=white)
![ONNX](https://img.shields.io/badge/ONNX-005CED?style=for-the-badge&logo=onnx&logoColor=white)
![PMML](https://img.shields.io/badge/PMML-2E86AB?style=for-the-badge)


### Infra
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white) ![Cloudflare](https://img.shields.io/badge/Cloudflare-F38020?style=for-the-badge&logo=cloudflare&logoColor=white)  
![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white)


### Wearable

![Garmin](https://img.shields.io/badge/Garmin-000000?style=for-the-badge&logo=garmin&logoColor=white)
![Garmin Connect IQ](https://img.shields.io/badge/Garmin%20Connect%20IQ-1E8FFF?style=for-the-badge&logo=garmin&logoColor=white)
![Monkey%20C](https://img.shields.io/badge/Monkey%20C-FF6F00?style=for-the-badge&logoColor=white)
![Connect%20IQ%20SDK](https://img.shields.io/badge/Connect%20IQ%20SDK-6B7280?style=for-the-badge&logoColor=white)
</div>

<br>


## Team
| **Lead / Backend·Infra·MLOps** | **Backend** | **Frontend** | **AI** |**Design** |
| :------: |  :------: | :------: |  :------: |  :------: | 
| <img src="https://github.com/jungjiyu.png" width="180"> |  <img src="https://github.com/SEUNGHYEOKNOH.png" width="180"> | <img src="https://github.com/aryu1217.png" width="180"> | <img src="https://github.com/ksumin12.png" width="180"> | <img src="https://github.com/E99egg.png" width="180"> | 
| [정지유](https://github.com/jungjiyu) | [노승혁](https://github.com/SEUNGHYEOKNOH) | [류태현](https://github.com/aryu1217) |[권수민](https://github.com/ksumin12) |[천서현](https://github.com/E99egg)|

Made with 💙 by OCEAN-KIT Team
