-- SkillFit AI MVP 최신 목업 데이터
-- 데모 계정 비밀번호: Skillfit1!

INSERT INTO users (id, email, password, name, role, created_at, updated_at)
VALUES
    (10001, 'skala1@skillfit.ai', '$2y$10$bFHc/WhC5AaxnIzhohZHUO7hXmP5qW.E/hseaDBBq72OFfkAjVm.G', '함주헌', 'STUDENT', NOW(6), NOW(6)),
    (10002, 'skala2@skillfit.ai', '$2y$10$bFHc/WhC5AaxnIzhohZHUO7hXmP5qW.E/hseaDBBq72OFfkAjVm.G', '허민규', 'STUDENT', NOW(6), NOW(6)),
    (10003, 'skala3@skillfit.ai', '$2y$10$bFHc/WhC5AaxnIzhohZHUO7hXmP5qW.E/hseaDBBq72OFfkAjVm.G', '박종민', 'STUDENT', NOW(6), NOW(6)),
    (10004, 'hr@skillfit.ai', '$2y$10$bFHc/WhC5AaxnIzhohZHUO7hXmP5qW.E/hseaDBBq72OFfkAjVm.G', 'HR 담당자', 'INSTRUCTOR', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE
    email = VALUES(email),
    password = VALUES(password),
    name = VALUES(name),
    role = VALUES(role),
    updated_at = NOW(6);

-- 하루 MVP에서는 enrollment_count를 강의 단계(1~5)로 사용한다.
INSERT INTO courses
    (id, title, description, category, price, instructor_id, enrollment_count, status, created_at, updated_at)
VALUES
    (11001, '비전공자를 위한 클라우드 기초', '클라우드 핵심 개념과 IaaS, PaaS, SaaS를 이해한다.', 'DEVOPS', 33000, 10004, 1, 'ACTIVE', NOW(6), NOW(6)),
    (11002, 'Docker와 Kubernetes 입문', '컨테이너와 오케스트레이션 기초를 익힌다.', 'DEVOPS', 55000, 10004, 2, 'ACTIVE', NOW(6), NOW(6)),
    (11003, 'AWS 기반 클라우드 아키텍처', '네트워크, 스토리지, 보안과 고가용성 설계를 학습한다.', 'DEVOPS', 79000, 10004, 3, 'ACTIVE', NOW(6), NOW(6)),
    (11004, 'Cloud Native MSA 운영 실전', 'MSA 관찰성, 장애 대응과 모니터링을 다룬다.', 'DEVOPS', 99000, 10004, 4, 'ACTIVE', NOW(6), NOW(6)),
    (11005, 'Kubernetes 기반 AI·MLOps 플랫폼', 'Kubernetes에서 AI 워크로드와 모델 배포·운영을 구축한다.', 'DEVOPS', 121000, 10004, 5, 'ACTIVE', NOW(6), NOW(6)),
    (11006, '누구나 이해하는 AI·머신러닝 기초', '비전공자도 AI 핵심 개념과 지도학습을 이해한다.', 'OTHER', 33000, 10004, 1, 'ACTIVE', NOW(6), NOW(6)),
    (11007, 'PyTorch로 시작하는 딥러닝', '신경망 기초부터 학습 루프까지 직접 구현한다.', 'DATA_SCIENCE', 55000, 10004, 2, 'ACTIVE', NOW(6), NOW(6)),
    (11008, 'Transformer와 LLM 동작 원리', 'Attention, Transformer, 임베딩을 통해 LLM 구조를 학습한다.', 'DATA_SCIENCE', 66000, 10004, 3, 'ACTIVE', NOW(6), NOW(6)),
    (11009, 'FastAPI와 OpenAI API 서비스 개발', '백엔드 역량을 AI API 서비스 개발로 연결한다.', 'BACKEND', 59000, 10004, 3, 'ACTIVE', NOW(6), NOW(6)),
    (11010, 'LangChain 기반 RAG 서비스 구축', '사내 문서와 벡터 DB를 활용한 RAG 서비스를 구축한다.', 'BACKEND', 89000, 10004, 4, 'ACTIVE', NOW(6), NOW(6)),
    (11011, 'LangGraph AI Agent와 LLMOps 고도화', 'AI Agent 워크플로, 평가, 배포와 모니터링을 다룬다.', 'BACKEND', 110000, 10004, 5, 'ACTIVE', NOW(6), NOW(6)),
    (11012, '비전공자를 위한 UX/UI 입문', 'UX 기초, 사용자 정의와 정보 구조를 학습한다.', 'FRONTEND', 33000, 10004, 1, 'ACTIVE', NOW(6), NOW(6)),
    (11013, 'Figma 기반 UI와 프로토타이핑', '와이어프레임, 컴포넌트와 인터랙션을 제작한다.', 'FRONTEND', 49500, 10004, 2, 'ACTIVE', NOW(6), NOW(6)),
    (11014, 'UX 리서치와 사용자 데이터 분석', '정성·정량 조사와 데이터로 UX 의사결정을 검증한다.', 'FRONTEND', 66000, 10004, 3, 'ACTIVE', NOW(6), NOW(6)),
    (11015, '생성형 AI 기반 UX/UI 워크플로', 'AI로 리서치, 아이디어 발굴과 프로토타이핑을 가속한다.', 'FRONTEND', 79000, 10004, 4, 'ACTIVE', NOW(6), NOW(6)),
    (11016, 'AI 프로덕트 UX와 디자인 시스템', 'AI 프로덕트 UX 원칙과 확장 가능한 디자인 시스템을 설계한다.', 'FRONTEND', 99000, 10004, 5, 'ACTIVE', NOW(6), NOW(6)),
    (11017, 'Python·Pandas 데이터 처리 입문', 'Python과 Pandas로 전처리, 정제와 시각화를 수행한다.', 'DATA_SCIENCE', 44000, 10004, 1, 'ACTIVE', NOW(6), NOW(6)),
    (11018, 'Spark·Kafka 스트리밍 데이터 파이프라인', '대용량 처리와 스트리밍 데이터 파이프라인을 구축한다.', 'DATA_SCIENCE', 79000, 10004, 2, 'ACTIVE', NOW(6), NOW(6)),
    (11019, '클라우드 데이터 플랫폼 아키텍처', 'Data Lake, Warehouse, Lakehouse 운영 구조를 설계한다.', 'DATA_SCIENCE', 99000, 10004, 3, 'ACTIVE', NOW(6), NOW(6)),
    (11020, '온톨로지·지식 그래프 기반 데이터 플랫폼', '도메인 지식, 온톨로지와 검색을 연결한 데이터 플랫폼을 구축한다.', 'DATA_SCIENCE', 110000, 10004, 4, 'ACTIVE', NOW(6), NOW(6)),
    (11021, '영업 담당자를 위한 생성형 AI 입문', '[영업] 고객 이메일, 미팅 준비와 영업 문서 작성에 생성형 AI를 활용한다.', 'OTHER', 33000, 10004, 1, 'ACTIVE', NOW(6), NOW(6)),
    (11022, 'AI 기반 고객 분석과 제안서 작성', '[영업] 고객 요구사항을 분석해 맞춤 제안서 초안을 작성한다.', 'FRONTEND', 55000, 10004, 2, 'ACTIVE', NOW(6), NOW(6)),
    (11023, 'AI 영업기회 분석과 파이프라인 관리', '[영업] 상담·거래 데이터를 분석해 우선 대응 영업기회를 도출한다.', 'DATA_SCIENCE', 79000, 10004, 3, 'ACTIVE', NOW(6), NOW(6)),
    (11024, '생성형 AI 마케팅 콘텐츠 제작', '[마케팅] 광고·SNS·이메일 마케팅 콘텐츠 초안을 생성한다.', 'FRONTEND', 33000, 10004, 1, 'ACTIVE', NOW(6), NOW(6)),
    (11025, 'AI 고객 리뷰·VOC 분석', '[마케팅] 고객 리뷰와 의견을 분류해 요구사항과 감성을 분석한다.', 'DATA_SCIENCE', 55000, 10004, 2, 'ACTIVE', NOW(6), NOW(6)),
    (11026, 'AI 기반 캠페인 성과 분석과 최적화', '[마케팅] 캠페인 데이터를 분석해 채널별 성과와 개선 방향을 도출한다.', 'DATA_SCIENCE', 79000, 10004, 3, 'ACTIVE', NOW(6), NOW(6)),
    (11027, 'HR 담당자를 위한 안전한 생성형 AI 활용', '[HR] 개인정보를 보호하면서 인사·교육 업무에 생성형 AI를 활용한다.', 'SECURITY', 33000, 10004, 1, 'ACTIVE', NOW(6), NOW(6)),
    (11028, 'AI 기반 직무역량 분석과 교육과정 설계', '[HR] 직무 요구역량과 직원 역량의 차이를 분석해 교육을 설계한다.', 'DATA_SCIENCE', 55000, 10004, 2, 'ACTIVE', NOW(6), NOW(6)),
    (11029, 'AI HRD 추천 시스템 기획', '[HR] 직원별 교육 추천 기준과 학습경로 및 성과 측정 방법을 설계한다.', 'OTHER', 79000, 10004, 3, 'ACTIVE', NOW(6), NOW(6)),
    (11030, '재무·회계 담당자를 위한 생성형 AI 입문', '[재무·회계] 회계 문서 요약과 재무 보고서 초안 작성에 AI를 활용한다.', 'OTHER', 33000, 10004, 1, 'ACTIVE', NOW(6), NOW(6)),
    (11031, 'AI 기반 증빙·정산 업무 자동화', '[재무·회계] 증빙과 정산 자료의 확인·분류·요약 작업을 자동화한다.', 'DEVOPS', 55000, 10004, 2, 'ACTIVE', NOW(6), NOW(6)),
    (11032, 'AI 재무데이터 분석과 이상징후 탐지', '[재무·회계] 비용과 매출 데이터를 분석해 비정상적인 변화와 확인 대상을 찾는다.', 'DATA_SCIENCE', 79000, 10004, 3, 'ACTIVE', NOW(6), NOW(6)),
    (11033, '상품기획·MD를 위한 생성형 AI 활용', '[상품기획·MD] 상품명·상세 설명·판매 포인트와 프로모션 문구를 작성한다.', 'FRONTEND', 33000, 10004, 1, 'ACTIVE', NOW(6), NOW(6)),
    (11034, 'AI 기반 시장 트렌드와 고객 리뷰 분석', '[상품기획·MD] 검색어·리뷰·판매 데이터에서 상품기획 트렌드를 도출한다.', 'DATA_SCIENCE', 55000, 10004, 2, 'ACTIVE', NOW(6), NOW(6)),
    (11035, 'AI 수요예측과 상품 운영 최적화', '[상품기획·MD] 상품별 판매 흐름을 분석해 수요와 재고 운영 방향을 판단한다.', 'DATA_SCIENCE', 79000, 10004, 3, 'ACTIVE', NOW(6), NOW(6)),
    (11036, '고객상담 담당자를 위한 생성형 AI 활용', '[고객상담] 고객 문의를 요약하고 상황에 맞는 답변 초안을 작성한다.', 'OTHER', 33000, 10004, 1, 'ACTIVE', NOW(6), NOW(6)),
    (11037, 'AI 기반 고객문의 분류와 VOC 분석', '[고객상담] 문의를 유형과 긴급도로 분류하고 반복되는 불만 원인을 분석한다.', 'DATA_SCIENCE', 55000, 10004, 2, 'ACTIVE', NOW(6), NOW(6)),
    (11038, 'RAG 기반 사내 상담 지식 활용', '[고객상담] 사내 매뉴얼과 FAQ를 검색해 근거 있는 상담 답변을 제공한다.', 'BACKEND', 79000, 10004, 3, 'ACTIVE', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    category = VALUES(category),
    price = VALUES(price),
    instructor_id = VALUES(instructor_id),
    enrollment_count = VALUES(enrollment_count),
    status = VALUES(status),
    updated_at = NOW(6);

