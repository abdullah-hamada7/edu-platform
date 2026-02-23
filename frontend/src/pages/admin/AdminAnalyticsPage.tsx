import { useEffect, useState } from 'react'
import api from '../../services/api'
import { BarChart3, ChevronRight, Loader2 } from 'lucide-react'

interface QuizSummary {
  id: string
  title: string
  status: string
}

interface GradeRecord {
  quizId: string
  score: number
  maxScore: number
  submittedAt: string
  gradingLatencyMs: number
}

export default function AdminAnalyticsPage() {
  const [quizzes, setQuizzes] = useState<QuizSummary[]>([])
  const [selectedQuiz, setSelectedQuiz] = useState<string>('')
  const [attempts, setAttempts] = useState<GradeRecord[]>([])
  const [loading, setLoading] = useState(true)
  const [attemptLoading, setAttemptLoading] = useState(false)

  useEffect(() => {
    api.get<QuizSummary[]>('/admin/quizzes')
      .then(res => setQuizzes(res.data))
      .finally(() => setLoading(false))
  }, [])

  const loadAttempts = async (quizId: string) => {
    setSelectedQuiz(quizId)
    setAttemptLoading(true)
    try {
      const response = await api.get<GradeRecord[]>(`/admin/analytics/quizzes/${quizId}/attempts`)
      setAttempts(response.data)
    } finally {
      setAttemptLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] gap-4 text-center">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-slate-500 font-medium uppercase tracking-widest text-[10px]">Loading analytics...</p>
      </div>
    )
  }

  return (
    <div className="space-y-8 pb-20">
      <div className="flex items-center gap-3">
        <BarChart3 size={24} className="text-primary" />
        <div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">Analytics Engine</h1>
          <p className="text-slate-400">Review quiz attempts and grading latency.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-6 space-y-4">
          <h2 className="text-sm font-black text-slate-300 uppercase tracking-widest">Quizzes</h2>
          <div className="space-y-2">
            {quizzes.length === 0 ? (
              <p className="text-xs text-slate-500">No quizzes available.</p>
            ) : (
              quizzes.map(quiz => (
                <button
                  key={quiz.id}
                  onClick={() => loadAttempts(quiz.id)}
                  className={`w-full flex items-center justify-between px-4 py-3 rounded-xl border transition-all ${
                    selectedQuiz === quiz.id
                      ? 'border-primary/40 bg-primary/10 text-primary'
                      : 'border-slate-800 bg-slate-950/40 text-slate-300'
                  }`}
                >
                  <div className="text-left">
                    <p className="text-sm font-bold">{quiz.title}</p>
                    <p className="text-[10px] uppercase tracking-widest text-slate-500">{quiz.status}</p>
                  </div>
                  <ChevronRight size={16} />
                </button>
              ))
            )}
          </div>
        </div>

        <div className="lg:col-span-2 bg-slate-900/40 border border-slate-800 rounded-2xl p-6">
          <h2 className="text-sm font-black text-slate-300 uppercase tracking-widest mb-4">Quiz Attempts</h2>
          {attemptLoading ? (
            <div className="flex items-center gap-3 text-slate-500">
              <Loader2 className="w-5 h-5 animate-spin" /> Loading attempts...
            </div>
          ) : attempts.length === 0 ? (
            <p className="text-xs text-slate-500">Select a quiz to view attempts.</p>
          ) : (
            <div className="space-y-2">
              {attempts.map((attempt, idx) => (
                <div key={idx} className="flex items-center justify-between px-4 py-3 rounded-xl border border-slate-800 bg-slate-950/40 text-xs text-slate-300">
                  <div>
                    <p className="text-sm font-bold">Score {attempt.score.toFixed(1)} / {attempt.maxScore.toFixed(1)}</p>
                    <p className="text-[10px] text-slate-500">{new Date(attempt.submittedAt).toLocaleString()}</p>
                  </div>
                  <span className="text-[10px] font-bold uppercase tracking-widest text-slate-500">{attempt.gradingLatencyMs}ms</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
