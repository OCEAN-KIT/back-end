-- PostGIS 확장 활성화 (geometry 타입 사용을 위해 필요)
-- Hibernate가 스키마를 생성하기 전에 실행되어야 함
CREATE EXTENSION IF NOT EXISTS postgis;
