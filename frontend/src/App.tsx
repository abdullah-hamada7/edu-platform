import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import AdminDashboardPage from './pages/admin/AdminDashboardPage'
import StudentDashboardPage from './pages/StudentDashboardPage'
import StudentLessonPage from './pages/StudentLessonPage'
import StudentQuizPage from './pages/StudentQuizPage'
import StudentGradesPage from './pages/StudentGradesPage'
import StudentCoursesPage from './pages/StudentCoursesPage'
import StudentCourseDetailPage from './pages/StudentCourseDetailPage'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/admin/*" element={<AdminDashboardPage />} />
      <Route path="/student" element={<StudentDashboardPage />} />
      <Route path="/student/courses" element={<StudentCoursesPage />} />
      <Route path="/student/courses/:courseId" element={<StudentCourseDetailPage />} />
      <Route path="/student/lessons/:lessonId" element={<StudentLessonPage />} />
      <Route path="/student/quizzes/:quizId" element={<StudentQuizPage />} />
      <Route path="/student/grades" element={<StudentGradesPage />} />
      <Route path="/" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default App
