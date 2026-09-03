import logging
from fastapi import APIRouter, Depends, HTTPException
from app.config.security import verify_token
from app.model.schemas import EmployeeProfileResponse, RecommendResponse
from app.service.recommend_service import recommend_service

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/recommend", tags=["recommend"])


@router.get("/profiles/{user_id}", response_model=EmployeeProfileResponse)
async def get_employee_profile(
    user_id: int,
    token_payload: dict = Depends(verify_token),
):
    """직원의 직무·현재 역량·부족 역량 조회 (user-service DB 기반)."""
    profile = await recommend_service.get_employee_profile(user_id)
    if not profile:
        raise HTTPException(status_code=404, detail="직원 역량 프로필이 없습니다.")
    return profile


@router.get("/{user_id}", response_model=RecommendResponse)
async def get_recommendations(
    user_id: int,
    token_payload: dict = Depends(verify_token)
):
    """
    GET /recommend/{userId} - 사용자 기반 강의 추천

    추천 규칙:
    - 직무 요구 수준과 현재 역량의 차이를 계산
    - 직원의 희망 방향에 맞는 후보 강의 선정
    - 기존 ACTIVE 과정 제외
    - 상위 3개 과정의 추천 퍼센트·이유 반환
    """
    logger.info(f"[Router] 추천 요청 - userId: {user_id}")
    return await recommend_service.get_recommendations(user_id)


@router.get("/health", include_in_schema=False)
async def health_check():
    return {"status": "UP", "service": "recommend-service"}
