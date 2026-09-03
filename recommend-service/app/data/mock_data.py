"""SkillFit AI 하루 MVP의 설명 가능한 추천용 목업 데이터.

사용자와 과정의 원본 정보는 각각 User/Course Service가 소유한다.
이 파일에는 Recommend Service가 사용할 역량 스냅샷과 과정 메타데이터만 둔다.
ID는 init-db/02_skillfit_seed.sql과 일치한다.
"""

EMPLOYEE_PROFILES = {
    10001: {
        "employeeCode": "skala1",
        "name": "함주헌",
        "job": "DATA_PLATFORM_ENGINEER",
        "jobLabel": "데이터 플랫폼 엔지니어",
        "overallLevel": "중",
        "careerGoal": "데이터 사이언스·온톨로지 데이터 플랫폼",
        "competencyScores": {
            "AI 기초 이해": 68,
            "데이터 활용": 84,
            "LLM 활용": 57,
            "AI 서비스 개발": 62,
            "AI 윤리·보안": 51,
        },
        "skills": {
            "DATA_ENGINEERING": 3,
            "CLOUD_INFRA": 2,
            "ONTOLOGY": 1,
            "MLOPS": 1,
            "LLM": 2,
        },
    },
    10002: {
        "employeeCode": "skala2",
        "name": "허민규",
        "job": "UX_UI_DESIGNER",
        "jobLabel": "AI UX/UI 디자이너",
        "overallLevel": "하",
        "careerGoal": "UX/UI·AI 프로덕트 디자인",
        "competencyScores": {
            "AI 기초 이해": 27,
            "데이터 활용": 32,
            "LLM 활용": 24,
            "AI 서비스 개발": 35,
            "AI 윤리·보안": 43,
        },
        "skills": {
            "AI_BASIC": 0,
            "UX_UI": 1,
            "PROTOTYPING": 0,
            "UX_RESEARCH": 1,
            "AI_PRODUCT_DESIGN": 0,
        },
    },
    10003: {
        "employeeCode": "skala3",
        "name": "박종민",
        "job": "AI_BACKEND_DEVELOPER",
        "jobLabel": "AI 백엔드 개발자",
        "overallLevel": "중",
        "careerGoal": "딥러닝·LLM 백엔드 서비스",
        "competencyScores": {
            "AI 기초 이해": 61,
            "데이터 활용": 55,
            "LLM 활용": 42,
            "AI 서비스 개발": 78,
            "AI 윤리·보안": 49,
        },
        "skills": {
            "BACKEND": 4,
            "MACHINE_LEARNING": 1,
            "DEEP_LEARNING": 1,
            "LLM": 1,
            "RAG": 0,
            "MLOPS": 2,
        },
    },
}

JOB_REQUIREMENTS = {
    "DATA_PLATFORM_ENGINEER": {
        "DATA_ENGINEERING": 4,
        "CLOUD_INFRA": 3,
        "ONTOLOGY": 4,
        "MLOPS": 3,
        "LLM": 2,
    },
    "UX_UI_DESIGNER": {
        "AI_BASIC": 2,
        "UX_UI": 4,
        "PROTOTYPING": 3,
        "UX_RESEARCH": 3,
        "AI_PRODUCT_DESIGN": 3,
    },
    "AI_BACKEND_DEVELOPER": {
        "BACKEND": 4,
        "MACHINE_LEARNING": 3,
        "DEEP_LEARNING": 4,
        "LLM": 4,
        "RAG": 3,
        "MLOPS": 3,
    },
}

# level은 학습 순서, skillWeights는 '부족도 × 보완 가중치' 계산에 사용한다.
COURSE_COMPETENCY_MAP = {
    11001: {"track": "CLOUD", "level": 1, "skillWeights": {"CLOUD_INFRA": 0.5}},
    11002: {"track": "CLOUD", "level": 2, "skillWeights": {"CLOUD_INFRA": 0.8, "MLOPS": 0.2}},
    11003: {"track": "CLOUD", "level": 3, "skillWeights": {"CLOUD_INFRA": 1.0}},
    11004: {"track": "CLOUD", "level": 4, "skillWeights": {"CLOUD_INFRA": 0.7, "BACKEND": 0.3}},
    11005: {"track": "CLOUD", "level": 5, "skillWeights": {"CLOUD_INFRA": 0.4, "MLOPS": 1.0}},
    11006: {"track": "AI_LLM", "level": 1, "skillWeights": {"AI_BASIC": 1.0, "MACHINE_LEARNING": 0.4}},
    11007: {"track": "AI_LLM", "level": 2, "skillWeights": {"MACHINE_LEARNING": 0.5, "DEEP_LEARNING": 1.0}},
    11008: {"track": "AI_LLM", "level": 3, "skillWeights": {"DEEP_LEARNING": 0.5, "LLM": 1.0}},
    11009: {"track": "AI_LLM", "level": 3, "skillWeights": {"BACKEND": 0.8, "LLM": 0.5}},
    11010: {"track": "AI_LLM", "level": 4, "skillWeights": {"LLM": 0.6, "RAG": 1.0}},
    11011: {"track": "AI_LLM", "level": 5, "skillWeights": {"LLM": 0.5, "RAG": 0.5, "MLOPS": 0.8}},
    11012: {"track": "UX_UI", "level": 1, "skillWeights": {"UX_UI": 0.7}},
    11013: {"track": "UX_UI", "level": 2, "skillWeights": {"UX_UI": 0.6, "PROTOTYPING": 1.0}},
    11014: {"track": "UX_UI", "level": 3, "skillWeights": {"UX_UI": 0.4, "UX_RESEARCH": 1.0}},
    11015: {"track": "UX_UI", "level": 4, "skillWeights": {"AI_BASIC": 0.4, "UX_UI": 0.7, "AI_PRODUCT_DESIGN": 0.8}},
    11016: {"track": "UX_UI", "level": 5, "skillWeights": {"UX_UI": 0.7, "AI_PRODUCT_DESIGN": 1.0}},
    11017: {"track": "DATA_PLATFORM", "level": 1, "skillWeights": {"DATA_ENGINEERING": 0.5}},
    11018: {"track": "DATA_PLATFORM", "level": 2, "skillWeights": {"DATA_ENGINEERING": 1.0}},
    11019: {"track": "DATA_PLATFORM", "level": 3, "skillWeights": {"DATA_ENGINEERING": 0.8, "CLOUD_INFRA": 0.7}},
    11020: {"track": "DATA_PLATFORM", "level": 4, "skillWeights": {"DATA_ENGINEERING": 0.4, "ONTOLOGY": 1.0, "RAG": 0.3}},
}

# 발표에서 보여줄 기본 파이프라인. ACTIVE 과정은 실제 계산 시 제외한다.
PROFILE_LEARNING_PATHS = {
    10001: [
        {"courseId": 11018, "reason": "통계 지식을 대규모 스트리밍 데이터 처리 역량으로 확장합니다."},
        {"courseId": 11019, "reason": "데이터 파이프라인을 기업용 클라우드 플랫폼 구조로 발전시킵니다."},
        {"courseId": 11020, "reason": "가장 큰 역량 차이인 온톨로지와 지식 그래프를 집중 보완합니다."},
        {"courseId": 11005, "reason": "완성한 데이터·AI 플랫폼을 안정적으로 운영하는 MLOps 역량을 강화합니다."},
    ],
    10002: [
        {"courseId": 11006, "reason": "비전공자가 AI 기반 디자인 도구를 이해할 수 있도록 기초부터 시작합니다."},
        {"courseId": 11012, "reason": "사용자 중심 설계를 위한 UX/UI 기본기를 먼저 확보합니다."},
        {"courseId": 11013, "reason": "아이디어를 실제 화면과 프로토타입으로 표현하는 역량을 보완합니다."},
        {"courseId": 11014, "reason": "감각이 아닌 사용자 데이터로 디자인을 검증하는 방법을 익힙니다."},
        {"courseId": 11015, "reason": "생성형 AI를 활용해 리서치와 프로토타이핑 속도를 높입니다."},
        {"courseId": 11016, "reason": "AI 프로덕트와 확장 가능한 디자인 시스템 단계로 고도화합니다."},
    ],
    10003: [
        {"courseId": 11006, "reason": "기존 백엔드 역량에 부족한 머신러닝 기본 개념을 보완합니다."},
        {"courseId": 11007, "reason": "낮은 딥러닝 역량을 PyTorch 구현 실습으로 끌어올립니다."},
        {"courseId": 11008, "reason": "딥러닝 기초를 Transformer와 LLM 구조 이해로 확장합니다."},
        {"courseId": 11009, "reason": "강점인 백엔드 개발을 실제 AI API 서비스 구현에 활용합니다."},
        {"courseId": 11010, "reason": "외부 지식을 활용하는 실무형 RAG 역량을 보완합니다."},
        {"courseId": 11011, "reason": "AI Agent의 평가·배포·운영까지 학습해 고급 단계로 발전합니다."},
    ],
}
