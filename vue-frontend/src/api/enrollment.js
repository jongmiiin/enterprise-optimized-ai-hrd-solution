import api from './index.js'

export const enrollmentApi = {
  getMyEnrollments() {
    return api.get('/api/enrollments/my')
  },
  enroll(courseId) {
    return api.post('/api/enrollments', { courseId })
  },
  cancel(enrollmentId) {
    return api.delete(`/api/enrollments/${enrollmentId}`)
  },
  getRecommendations(userId) {
    return api.get(`/api/recommend/${userId}`)
  },

  // HR 승인 관리
  getAdminEnrollments(status = 'PENDING') {
    return api.get('/api/enrollments/admin', { params: { status } })
  },
  approve(enrollmentId) {
    return api.post(`/api/enrollments/${enrollmentId}/approve`)
  }
}
