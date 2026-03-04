<div align="center">

# 🏠 Houseping

**"이 청약, 넣을만할까?"**

분양가 vs 실거래가 비교 분석 서비스

[![Live Demo](https://img.shields.io/badge/Live-house--ping.com-ff6b6b)](https://house-ping.com)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)
[![Built with Claude](https://img.shields.io/badge/Built%20with-Claude-blueviolet?logo=anthropic)](https://claude.ai)

</div>

---

## 이런 문제를 해결합니다

청약 공고를 봐도 **"이게 비싼 건지 싼 건지"** 감이 안 잡힙니다.
분양가는 나와 있지만, 주변 시세와 직접 비교하려면 여러 사이트를 돌아다녀야 합니다.

Houseping은 **청약 분양가와 주변 실거래가를 자동으로 비교**해서, 사용자가 한눈에 판단할 수 있게 도와줍니다.

## 주요 기능

| 기능 | 설명 |
|------|------|
| **청약 목록** | 청약Home·LH 통합 수집, 마감/시작 임박순 정렬 |
| **가격 배지** | 주변 신축 실거래 중앙값 대비 분양가 수준을 한눈에 판정 |
| **시세 비교 분석** | 평형별 분양가 vs 같은 동 신축 실거래가 비교, 예상 차익 계산 |
| **경쟁률 조회** | 발표 후 타입·순위·지역별 경쟁률 확인 |
| **주간 블로그** | AI가 금주 청약을 요약·분석하여 블로그 포스트 자동 발행 |
| **가점 계산기** | 청약 가점 항목별 점수 계산 |
| **청약 가이드** | 청약 절차·용어 가이드 6페이지 |

## 데이터 출처

- [청약Home 공공데이터 API](https://data.go.kr) — 청약 공고, 분양가, 경쟁률
- [국토교통부 실거래가 API](https://data.go.kr) — 아파트 매매 실거래가
- [LH 청약센터](https://apply.lh.or.kr) — LH 임대·분양 청약

> 모든 데이터는 공공 API를 통해 자동 수집되며, 일 1회 동기화됩니다.

## 기술 문서

프로젝트 설계와 로컬 환경 세팅은 아래 문서를 참고하세요.

| 문서 | 내용 |
|------|------|
| [아키텍처](docs/architecture.md) | 모듈 구조, 의존 그래프, 설계 결정 |
| [AI 블로그 파이프라인](docs/blog-pipeline.md) | AI 자동 발행 흐름, 프롬프트 설계, 스코어링 |
| [시작하기](docs/getting-started.md) | 요구사항, DB 설정, 환경 변수, 실행 |
