package com.lecture.enrollment.service;

import com.lecture.enrollment.dto.EnrollmentDto;
import com.lecture.enrollment.entity.Enrollment;
import com.lecture.enrollment.kafka.EnrollmentKafkaProducer;
import com.lecture.enrollment.kafka.KafkaEvent;
import com.lecture.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseServiceClient courseServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final EnrollmentKafkaProducer kafkaProducer;
    private final EnrollmentWriteService enrollmentWriteService;
    private final UserServiceClient userServiceClient;

    /**
     * 수강신청 전체 흐름
     * 1. 강의 존재 확인
     * 2. 중복 수강 확인
     * 3. Enrollment 생성 및 즉시 커밋 (PENDING)
     * 4. HR 승인을 기다림 (결제는 승인 API에서 실행)
     */
    public EnrollmentDto.EnrollmentResponse enroll(Long userId, Long courseId) {
        if (!courseServiceClient.existsCourse(courseId)) {
            throw new IllegalArgumentException("존재하지 않는 강의입니다: " + courseId);
        }

        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new IllegalArgumentException("이미 수강신청한 강의입니다");
        }

        Enrollment enrollment = enrollmentWriteService.createPendingEnrollment(userId, courseId);

        log.info("[EnrollmentService] 수강신청 완료 (HR 승인 대기) - enrollmentId: {}", enrollment.getId());
        return EnrollmentDto.EnrollmentResponse.from(enrollment);
    }

    /**
     * HR 승인 화면용 상태별 신청 목록.
     * User/Course 테이블을 직접 조인하지 않고 각 소유 서비스의 내부 API를 호출한다.
     */
    public List<EnrollmentDto.AdminEnrollmentResponse> getAdminEnrollments(
            Long requesterId, Enrollment.Status status) {
        assertInstructor(requesterId);

        return enrollmentRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(enrollment -> {
                    UserServiceClient.UserInfo user = userServiceClient.getUser(enrollment.getUserId());
                    Map<String, Object> course = courseServiceClient.getCourse(enrollment.getCourseId());

                    return EnrollmentDto.AdminEnrollmentResponse.builder()
                            .enrollmentId(enrollment.getId())
                            .userId(enrollment.getUserId())
                            .userName(user.getName())
                            .userEmail(user.getEmail())
                            .courseId(enrollment.getCourseId())
                            .courseTitle((String) course.get("title"))
                            .courseCategory(normalizeCategory(String.valueOf(course.get("category"))))
                            .price(toBigDecimal(course.get("price")))
                            .status(enrollment.getStatus())
                            .createdAt(enrollment.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * HR 승인 버튼 한 번으로 실제 강의 가격의 결제를 완료한다.
     * 결제 완료 이벤트를 받은 Kafka Consumer가 Enrollment를 ACTIVE로 전환한다.
     */
    public EnrollmentDto.ApprovalResponse approveEnrollment(Long requesterId, Long enrollmentId) {
        assertInstructor(requesterId);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "수강 신청을 찾을 수 없습니다: " + enrollmentId));

        if (enrollment.getStatus() != Enrollment.Status.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "이미 처리된 수강 신청입니다: " + enrollmentId);
        }

        Map<String, Object> course = courseServiceClient.getCourse(enrollment.getCourseId());
        BigDecimal price = toBigDecimal(course.get("price"));
        PaymentServiceClient.PaymentResult payment = paymentServiceClient.requestPayment(
                enrollment.getUserId(), enrollment.getCourseId(), price);

        if (payment == null || !"COMPLETED".equals(payment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "결제가 완료되지 않았습니다");
        }

        log.info("[EnrollmentService] HR 승인 및 결제 완료 - enrollmentId: {}, paymentId: {}",
                enrollmentId, payment.getPaymentId());

        return EnrollmentDto.ApprovalResponse.builder()
                .enrollmentId(enrollmentId)
                .paymentId(payment.getPaymentId())
                .paymentStatus(payment.getStatus())
                .enrollmentStatus(enrollment.getStatus())
                .build();
    }

    /**
     * 수강 활성화
     */
    @Transactional
    public void activateEnrollment(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "수강 정보를 찾을 수 없습니다 - userId: " + userId + ", courseId: " + courseId));

        if (enrollment.getStatus() == Enrollment.Status.ACTIVE) {
            log.info("[EnrollmentService] 이미 활성화된 수강 신청 - enrollmentId: {}", enrollment.getId());
            return;
        }

        enrollment.activate();

        courseServiceClient.increaseEnrollmentCount(courseId);

        kafkaProducer.publishEnrollmentCompleted(
                KafkaEvent.EnrollmentCompletedEvent.builder()
                        .enrollmentId(enrollment.getId())
                        .userId(userId)
                        .courseId(courseId)
                        .build()
        );

        log.info("[EnrollmentService] 수강 활성화 완료 - enrollmentId: {}", enrollment.getId());
    }

    /**
     * 사용자 수강 목록 조회
     * - course-service에서 강의 상세 정보를 붙여서 반환
     */
    public List<EnrollmentDto.EnrollmentResponse> getEnrollmentsByUser(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);

        return enrollments.stream()
                .map(enrollment -> {
                    Map<String, Object> courseInfo = courseServiceClient.getCourse(enrollment.getCourseId());

                    EnrollmentDto.CourseSummary courseSummary = EnrollmentDto.CourseSummary.builder()
                            .id(toLong(courseInfo.get("id")))
                            .title((String) courseInfo.get("title"))
                            .description((String) courseInfo.get("description"))
                            .category(normalizeCategory((String) courseInfo.get("category")))
                            .price(toInteger(courseInfo.get("price")))
                            .thumbnail((String) courseInfo.get("thumbnail"))
                            .instructorName(
                                    firstNonNull(
                                            (String) courseInfo.get("instructorName"),
                                            (String) courseInfo.get("teacherName"),
                                            (String) courseInfo.get("instructor_name")
                                    )
                            )
                            .enrollmentCount(toInteger(
                                    firstNonNullObject(
                                            courseInfo.get("enrollmentCount"),
                                            courseInfo.get("enrollment_count")
                                    )
                            ))
                            .build();

                    return EnrollmentDto.EnrollmentResponse.from(enrollment, courseSummary);
                })
                .collect(Collectors.toList());
    }

    /**
     * 수강 이력 조회 - 추천 서비스용
     */
    public EnrollmentDto.EnrollmentHistoryResponse getEnrollmentHistory(Long userId) {
        List<Long> activeCourseIds = enrollmentRepository
                .findByUserIdAndStatus(userId, Enrollment.Status.ACTIVE)
                .stream()
                .map(Enrollment::getCourseId)
                .collect(Collectors.toList());

        return EnrollmentDto.EnrollmentHistoryResponse.builder()
                .userId(userId)
                .activeCourseIds(activeCourseIds)
                .build();
    }

    private String normalizeCategory(String category) {
        if (category == null) return null;

        return switch (category) {
            case "BACKEND" -> "개발·코딩 AI";
            case "FRONTEND" -> "생성형 AI 활용";
            case "DEVOPS" -> "AI 운영 자동화";
            case "DATA", "DATA_SCIENCE" -> "데이터·AI 분석";
            case "SECURITY" -> "AI 보안";
            case "OTHER" -> "AI 기초·공통";
            default -> category;
        };
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(value.toString());
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "강의 가격 정보가 없습니다");
        }
        if (value instanceof BigDecimal decimal) return decimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        return new BigDecimal(value.toString());
    }

    private void assertInstructor(Long requesterId) {
        UserServiceClient.UserInfo requester = userServiceClient.getUser(requesterId);
        if (!"INSTRUCTOR".equals(requester.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "HR 담당자만 접근할 수 있습니다");
        }
    }

    private String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private Object firstNonNullObject(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
