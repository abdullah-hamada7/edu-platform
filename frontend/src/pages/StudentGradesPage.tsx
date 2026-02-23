import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import api from '../services/api'

interface GradeRecord {
  quizId: string
  quizTitle?: string
  score: number
  maxScore: number
  submittedAt: string
  gradingLatencyMs: number
}

export default function StudentGradesPage() {
  const location = useLocation()
  const [grades, setGrades] = useState<GradeRecord[]>([])
  const [loading, setLoading] = useState(true)
  const recentResult: GradeRecord | null = location.state?.result ?? null

  useEffect(() => {
    api.get<GradeRecord[]>('/student/grades')
      .then((res) => setGrades(res.data))
      .finally(() => setLoading(false))
  }, [])

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  const getPercentage = (score: number, max: number) => {
    if (max === 0) return 0
    return Math.round((score / max) * 100)
  }

  const getGradeColor = (percentage: number) => {
    if (percentage >= 90) return 'text-green-600'
    if (percentage >= 70) return 'text-blue-600'
    if (percentage >= 60) return 'text-yellow-600'
    return 'text-red-600'
  }

  if (loading) {
    return <div className="flex justify-center items-center min-h-screen">Loading grades...</div>
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <h1 className="text-2xl font-bold mb-6">My Grades</h1>

      {recentResult && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 mb-6">
          <h2 className="text-lg font-semibold text-blue-800 mb-2">Quiz Submitted!</h2>
          <div className="flex items-center gap-4">
            <div className="text-3xl font-bold text-blue-600">
              {recentResult.score.toFixed(1)} / {recentResult.maxScore.toFixed(1)}
            </div>
            <div className="text-sm text-blue-600">
              Graded in {recentResult.gradingLatencyMs}ms
            </div>
          </div>
        </div>
      )}

      {grades.length === 0 ? (
        <p className="text-gray-600">No grades yet. Complete some quizzes to see your progress!</p>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-sm font-medium text-gray-600">Quiz</th>
                <th className="px-6 py-3 text-left text-sm font-medium text-gray-600">Score</th>
                <th className="px-6 py-3 text-left text-sm font-medium text-gray-600">Percentage</th>
                <th className="px-6 py-3 text-left text-sm font-medium text-gray-600">Submitted</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {grades.map((grade, index) => {
                const percentage = getPercentage(grade.score, grade.maxScore)
                return (
                  <tr key={index}>
                    <td className="px-6 py-4">{grade.quizTitle || 'Untitled Quiz'}</td>
                    <td className="px-6 py-4">
                      {grade.score.toFixed(1)} / {grade.maxScore.toFixed(1)}
                    </td>
                    <td className={`px-6 py-4 font-medium ${getGradeColor(percentage)}`}>
                      {percentage}%
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600">
                      {formatDate(grade.submittedAt)}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}

      {grades.length > 0 && (
        <div className="mt-6 grid grid-cols-3 gap-4">
          <div className="bg-white rounded-lg shadow p-4 text-center">
            <p className="text-sm text-gray-600">Total Quizzes</p>
            <p className="text-2xl font-bold">{grades.length}</p>
          </div>
          <div className="bg-white rounded-lg shadow p-4 text-center">
            <p className="text-sm text-gray-600">Average Score</p>
            <p className="text-2xl font-bold">
              {grades.length > 0
                ? Math.round(grades.reduce((acc, g) => acc + getPercentage(g.score, g.maxScore), 0) / grades.length)
                : 0}%
            </p>
          </div>
          <div className="bg-white rounded-lg shadow p-4 text-center">
            <p className="text-sm text-gray-600">Avg Grading Time</p>
            <p className="text-2xl font-bold">
              {grades.length > 0
                ? Math.round(grades.reduce((acc, g) => acc + g.gradingLatencyMs, 0) / grades.length)
                : 0}ms
            </p>
          </div>
        </div>
      )}
    </div>
  )
}
