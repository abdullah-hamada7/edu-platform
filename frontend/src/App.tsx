import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import AdminDashboardPage from './pages/admin/AdminDashboardPage'
import AdminCoursesPage from './pages/admin/AdminCoursesPage'
import AdminUsersPage from './pages/admin/AdminUsersPage'
import StudentDashboardPage from './pages/StudentDashboardPage'
import StudentLessonPage from './pages/StudentLessonPage'
import StudentQuizPage from './pages/StudentQuizPage'
import StudentGradesPage from './pages/StudentGradesPage'
import StudentCoursesPage from './pages/StudentCoursesPage'
import StudentCourseDetailPage from './pages/StudentCourseDetailPage'
import AppLayout from './components/layout/AppLayout'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      {/* Protected Layout Routes */}
      <Route element={<AppLayout />}>
        {/* Admin Routes */}
        <Route path="/admin" element={<AdminDashboardPage />} />
        <Route path="/admin/courses" element={<AdminCoursesPage />} />
        <Route path="/admin/users" element={<AdminUsersPage />} />
        <Route path="/admin/analytics" element={<Navigate to="/admin" replace />} />

        {/* Student Routes */}
        <Route path="/student" element={<StudentDashboardPage />} />
        <Route path="/student/courses" element={<StudentCoursesPage />} />
        <Route path="/student/courses/:courseId" element={<StudentCourseDetailPage />} />
        <Route path="/student/lessons/:lessonId" element={<StudentLessonPage />} />
        <Route path="/student/quizzes/:quizId" element={<StudentQuizPage />} />
        <Route path="/student/grades" element={<StudentGradesPage />} />
      </Route>

      <Route path="/" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default App
