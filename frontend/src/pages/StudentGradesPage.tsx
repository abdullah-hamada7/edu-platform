import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import api from '../services/api'
import {
  Trophy,
  Calendar,
  ChevronRight,
  History,
  Timer,
  CheckCircle2,
  TrendingUp
} from 'lucide-react'
import { cn } from '../lib/utils'

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
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    })
  }

  const getPercentage = (score: number, max: number) => {
    if (max === 0) return 0
    return Math.round((score / max) * 100)
  }

  const getStatusColor = (percentage: number) => {
    if (percentage >= 90) return 'text-emerald-400 bg-emerald-400/10 border-emerald-400/20'
    if (percentage >= 70) return 'text-blue-400 bg-blue-400/10 border-blue-400/20'
    if (percentage >= 60) return 'text-amber-400 bg-amber-400/10 border-amber-400/20'
    return 'text-rose-400 bg-rose-400/10 border-rose-400/20'
  }

  const getProgressColor = (percentage: number) => {
    if (percentage >= 90) return 'bg-emerald-500'
    if (percentage >= 70) return 'bg-blue-500'
    if (percentage >= 60) return 'bg-amber-500'
    return 'bg-rose-500'
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="h-8 w-48 bg-slate-800 animate-pulse rounded-lg" />
        <div className="space-y-4">
          {[1, 2, 3].map(i => (
            <div key={i} className="h-24 bg-slate-900/40 border border-slate-800 rounded-2xl animate-pulse" />
          ))}
        </div>
      </div>
    )
  }

  const avgPercentage = grades.length > 0
    ? Math.round(grades.reduce((acc, g) => acc + getPercentage(g.score, g.maxScore), 0) / grades.length)
    : 0

  return (
    <div className="space-y-8">
      <div className="flex flex-col gap-1">
        <h1 className="text-3xl font-bold tracking-tight">Your Grades</h1>
        <p className="text-slate-400">Track your academic performance and quiz history.</p>
      </div>

      {recentResult && (
        <div className="relative overflow-hidden bg-primary/10 border border-primary/20 rounded-3xl p-8 flex flex-col md:flex-row items-center gap-6 animate-in zoom-in duration-500">
          {/* Decorative Orb */}
          <div className="absolute -top-24 -right-24 w-48 h-48 bg-primary/20 rounded-full blur-[60px]" />

          <div className="shrink-0 w-16 h-16 rounded-2xl bg-primary/20 text-primary flex items-center justify-center border border-primary/20">
            <CheckCircle2 size={32} />
          </div>

          <div className="flex-1 text-center md:text-left space-y-1">
            <h2 className="text-2xl font-bold text-white">Quiz Successfully Completed!</h2>
            <p className="text-slate-400">Excellent work! Your result has been recorded and is displayed below.</p>
          </div>

          <div className="shrink-0 bg-slate-950/50 backdrop-blur border border-white/5 rounded-2xl p-4 min-w-[140px] text-center">
            <p className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">Earned Score</p>
            <p className="text-3xl font-black text-primary">{recentResult.score.toFixed(1)}<span className="text-lg text-slate-500 ml-1">/ {recentResult.maxScore.toFixed(0)}</span></p>
          </div>
        </div>
      )}

      {/* Summary Ribbon */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-slate-900/40 border border-slate-800 p-5 rounded-2xl flex items-center gap-4">
          <div className="p-2.5 rounded-xl bg-blue-600/10 text-blue-400 border border-blue-600/20">
            <History size={20} />
          </div>
          <div>
            <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">Total Quizzes</p>
            <p className="text-xl font-bold">{grades.length}</p>
          </div>
        </div>
        <div className="bg-slate-900/40 border border-slate-800 p-5 rounded-2xl flex items-center gap-4">
          <div className="p-2.5 rounded-xl bg-emerald-600/10 text-emerald-400 border border-emerald-600/20">
            <TrendingUp size={20} />
          </div>
          <div>
            <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">Avg Perfomance</p>
            <p className="text-xl font-bold">{avgPercentage}%</p>
          </div>
        </div>
        <div className="bg-slate-900/40 border border-slate-800 p-5 rounded-2xl flex items-center gap-4">
          <div className="p-2.5 rounded-xl bg-amber-600/10 text-amber-400 border border-amber-600/20">
            <Timer size={20} />
          </div>
          <div>
            <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">Avg Grading</p>
            <p className="text-xl font-bold">{grades.length > 0 ? Math.round(grades.reduce((a, g) => a + g.gradingLatencyMs, 0) / grades.length) : 0}ms</p>
          </div>
        </div>
      </div>

      {/* Grades List */}
      <div className="space-y-4">
        {grades.length === 0 ? (
          <div className="bg-slate-900/40 border border-slate-800 rounded-3xl p-12 text-center text-slate-500">
            <Trophy size={48} className="mx-auto mb-4 opacity-20" />
            <p>No grade records found. Complete a quiz to see your performance here.</p>
          </div>
        ) : (
          grades.map((grade, index) => {
            const percentage = getPercentage(grade.score, grade.maxScore)
            return (
              <div
                key={index}
                className="group flex flex-col sm:flex-row items-center gap-6 bg-slate-900/40 border border-slate-800 p-6 rounded-2xl hover:border-slate-700 transition-all"
              >
                <div className="shrink-0 flex items-center justify-center w-12 h-12 rounded-full bg-slate-800 text-slate-400 font-bold border border-slate-700/50">
                  {index + 1}
                </div>

                <div className="flex-1 space-y-1 text-center sm:text-left">
                  <h3 className="text-lg font-bold text-white group-hover:text-primary transition-colors">
                    {grade.quizTitle || 'Mathematics Evaluation'}
                  </h3>
                  <div className="flex items-center justify-center sm:justify-start gap-4 text-xs text-slate-500 font-medium">
                    <span className="flex items-center gap-1.5"><Calendar size={14} /> {formatDate(grade.submittedAt)}</span>
                    <span className="flex items-center gap-1.5"><Timer size={14} /> {grade.gradingLatencyMs}ms latency</span>
                  </div>
                </div>

                <div className="w-full sm:w-48 space-y-2">
                  <div className="flex justify-between text-[10px] font-black uppercase tracking-widest text-slate-500">
                    <span>Performance</span>
                    <span className={cn("px-2 py-0.5 rounded-full border", getStatusColor(percentage))}>{percentage}%</span>
                  </div>
                  <div className="h-2 w-full bg-slate-800 rounded-full overflow-hidden">
                    <div
                      className={cn("h-full rounded-full transition-all duration-1000 ease-out", getProgressColor(percentage))}
                      style={{ width: `${percentage}%` }}
                    />
                  </div>
                </div>

                <div className="hidden sm:flex shrink-0 items-center justify-center w-10 h-10 rounded-xl bg-slate-800/50 text-slate-600 group-hover:bg-slate-700/50 group-hover:text-slate-200 transition-all">
                  <ChevronRight size={18} />
                </div>
              </div>
            )
          })
        )}
      </div>
    </div>
  )
}
