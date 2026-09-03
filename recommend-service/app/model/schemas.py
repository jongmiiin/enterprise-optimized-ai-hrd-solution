from pydantic import BaseModel, Field
from typing import Dict, List, Optional
from enum import Enum
from decimal import Decimal
from datetime import datetime


class CourseCategory(str, Enum):
    BACKEND = "BACKEND"
    FRONTEND = "FRONTEND"
    DEVOPS = "DEVOPS"
    DATA_SCIENCE = "DATA_SCIENCE"
    MOBILE = "MOBILE"
    SECURITY = "SECURITY"
    DATABASE = "DATABASE"
    OTHER = "OTHER"


class CourseResponse(BaseModel):
    id: int
    title: str
    description: Optional[str] = None
    category: CourseCategory
    price: Decimal
    instructorId: int
    enrollmentCount: int
    status: str
    createdAt: Optional[datetime] = None


class RecommendedCourseResponse(CourseResponse):
    recommendScore: float
    recommendReason: str
    missingSkills: List[str]
    learningOrder: int


class EnrollmentHistoryResponse(BaseModel):
    userId: int
    activeCourseIds: List[int]


class RecommendResponse(BaseModel):
    userId: int
    employeeCode: Optional[str] = None
    employeeName: Optional[str] = None
    job: Optional[str] = None
    jobLabel: Optional[str] = None
    overallLevel: Optional[str] = None
    careerGoal: Optional[str] = None
    competencyScores: Dict[str, int] = Field(default_factory=dict)
    # 마이페이지 역량 오각형(radar)용 5개 역량 (영문키 aiLiteracy 등, 1~5)
    competencies: Dict[str, int] = Field(default_factory=dict)
    recommendedCourses: List[RecommendedCourseResponse]
    basedOnCategory: Optional[CourseCategory] = None
    message: str


class EmployeeProfileResponse(BaseModel):
    userId: int
    employeeCode: str
    name: str
    job: str
    jobLabel: str
    overallLevel: str
    careerGoal: str
    competencyScores: Dict[str, int]
    skills: Dict[str, int]
    requiredSkills: Dict[str, int]
    skillGaps: Dict[str, int]


class ApiResponse(BaseModel):
    success: bool
    message: str
    data: Optional[dict] = None
