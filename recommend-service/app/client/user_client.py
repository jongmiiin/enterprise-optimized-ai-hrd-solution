import httpx
import logging
from typing import Optional
from app.config.settings import settings

logger = logging.getLogger(__name__)


class UserServiceClient:
    """
    User Service REST 클라이언트
    - 직원 역량 프로필 조회 (백엔드추천 api명세서 §3)
    - mock_data(EMPLOYEE_PROFILES)를 대체하는 DB 기반 프로필 소스
    """

    def __init__(self):
        self.base_url = settings.user_service_url

    async def get_competency_profile(self, user_id: int) -> Optional[dict]:
        """
        GET /users/internal/{userId}/competency-profile
        반환 dict 키: employeeCode, name, job, jobLabel, overallLevel, careerGoal,
                      competencyScores(0~100), skills(0~5)
        실패/없음 시 None → 추천 서비스는 인기 과정 fallback.
        """
        url = f"{self.base_url}/api/users/internal/{user_id}/competency-profile"
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                response = await client.get(url)
                response.raise_for_status()
                data = response.json()
                # 프로필 미설정(직무 없음) → 없음으로 간주
                if not data or not data.get("job"):
                    return None
                return data
        except httpx.HTTPError as e:
            logger.error(f"[UserClient] 역량 프로필 조회 실패 - userId: {user_id}, error: {e}")
            return None


user_client = UserServiceClient()
