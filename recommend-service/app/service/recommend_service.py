import logging

from app.client.course_client import course_client
from app.client.enrollment_client import enrollment_client
from app.client.user_client import user_client
from app.data.mock_data import (
    COURSE_COMPETENCY_MAP,
    JOB_REQUIREMENTS,
    PROFILE_LEARNING_PATHS,
)
from app.model.schemas import (
    EmployeeProfileResponse,
    RecommendedCourseResponse,
    RecommendResponse,
)

logger = logging.getLogger(__name__)


class RecommendService:
    """직무 역량 차이와 선수 학습 순서를 사용하는 설명 가능한 MVP 추천."""

    MAX_RECOMMEND_COUNT = 3

    async def get_recommendations(self, user_id: int) -> RecommendResponse:
        logger.info(f"[RecommendService] 추천 시작 - userId: {user_id}")

        # mock_data 대신 user-service DB 프로필 조회 (백엔드추천 api명세서 §3)
        profile = await user_client.get_competency_profile(user_id)
        if not profile:
            return await self._recommend_for_unknown_user(user_id)

        history = await enrollment_client.get_enrollment_history(user_id)
        active_course_ids = set(history.activeCourseIds)
        all_courses = await course_client.get_all_courses()
        course_map = {course.id: course for course in all_courses}
        skill_gaps = self._calculate_skill_gaps(profile)

        pipeline = [
            item
            for item in PROFILE_LEARNING_PATHS.get(user_id, [])
            if item["courseId"] not in active_course_ids
            and item["courseId"] in course_map
        ]
        # 프로필별 후보 목록 자체가 현재 수준에 맞는 순서로 구성되어 있다.
        # 화면에서는 로드맵을 노출하지 않지만, 초급자에게 고급 과정이 먼저
        # 추천되지 않도록 현재 수준에 맞는 앞쪽 3개를 사용한다.
        selected = pipeline[:self.MAX_RECOMMEND_COUNT]
        raw_scores = [
            self._calculate_course_fit(item["courseId"], skill_gaps)
            for item in selected
        ]
        max_raw_score = max(raw_scores, default=1.0) or 1.0

        recommended = []
        for learning_order, (item, raw_score) in enumerate(
            zip(selected, raw_scores), start=1
        ):
            course_id = item["courseId"]
            course = course_map[course_id]
            missing_skills = self._matched_missing_skills(course_id, skill_gaps)
            recommended.append(
                RecommendedCourseResponse(
                    **course.model_dump(),
                    recommendScore=round(60 + raw_score / max_raw_score * 40, 1),
                    recommendReason=item["reason"],
                    missingSkills=missing_skills,
                    learningOrder=learning_order,
                )
            )

        logger.info(
            "[RecommendService] 역량 기반 추천 완료 - userId: %s, count: %s",
            user_id,
            len(recommended),
        )

        return RecommendResponse(
            userId=user_id,
            employeeCode=profile["employeeCode"],
            employeeName=profile["name"],
            job=profile["job"],
            jobLabel=profile["jobLabel"],
            overallLevel=profile["overallLevel"],
            careerGoal=profile["careerGoal"],
            competencyScores=profile["competencyScores"],
            competencies=profile.get("competencies") or {},
            recommendedCourses=recommended,
            basedOnCategory=None,
            message=f"{profile['name']}님의 현재 역량과 희망 방향을 반영한 맞춤 강의입니다.",
        )

    async def get_employee_profile(self, user_id: int) -> EmployeeProfileResponse | None:
        # mock_data 대신 user-service DB 프로필 조회
        profile = await user_client.get_competency_profile(user_id)
        if not profile:
            return None
        required = JOB_REQUIREMENTS[profile["job"]]
        return EmployeeProfileResponse(
            userId=user_id,
            employeeCode=profile["employeeCode"],
            name=profile["name"],
            job=profile["job"],
            jobLabel=profile["jobLabel"],
            overallLevel=profile["overallLevel"],
            careerGoal=profile["careerGoal"],
            competencyScores=profile["competencyScores"],
            skills=profile["skills"],
            requiredSkills=required,
            skillGaps=self._calculate_skill_gaps(profile),
        )

    def _calculate_skill_gaps(self, profile: dict) -> dict[str, int]:
        required = JOB_REQUIREMENTS[profile["job"]]
        return {
            skill: max(required_level - profile["skills"].get(skill, 0), 0)
            for skill, required_level in required.items()
        }

    def _calculate_course_fit(
        self, course_id: int, skill_gaps: dict[str, int]
    ) -> float:
        weights = COURSE_COMPETENCY_MAP[course_id]["skillWeights"]
        return sum(skill_gaps.get(skill, 0) * weight for skill, weight in weights.items())

    def _matched_missing_skills(
        self, course_id: int, skill_gaps: dict[str, int]
    ) -> list[str]:
        weights = COURSE_COMPETENCY_MAP[course_id]["skillWeights"]
        matched = [
            (skill, skill_gaps.get(skill, 0) * weight)
            for skill, weight in weights.items()
            if skill_gaps.get(skill, 0) > 0
        ]
        matched.sort(key=lambda item: item[1], reverse=True)
        return [skill for skill, _ in matched]

    async def _recommend_for_unknown_user(self, user_id: int) -> RecommendResponse:
        logger.info("[RecommendService] 목업 프로필 없음 - userId: %s", user_id)
        all_courses = await course_client.get_all_courses()
        popular = sorted(
            all_courses,
            key=lambda c: c.enrollmentCount,
            reverse=True,
        )[:self.MAX_RECOMMEND_COUNT]
        recommended = [
            RecommendedCourseResponse(
                **course.model_dump(),
                recommendScore=0,
                recommendReason="직원 역량 프로필이 없어 인기 과정으로 대체했습니다.",
                missingSkills=[],
                learningOrder=index,
            )
            for index, course in enumerate(popular, start=1)
        ]

        return RecommendResponse(
            userId=user_id,
            recommendedCourses=recommended,
            basedOnCategory=None,
            message="직원 역량 프로필이 없어 인기 과정으로 대체했습니다.",
        )


recommend_service = RecommendService()
