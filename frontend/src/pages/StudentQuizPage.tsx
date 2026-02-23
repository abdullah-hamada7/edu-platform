import { useParams, useNavigate, Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import QuizQuestion from '../features/quiz/QuizQuestion'
import api from '../services/api'
import {
  Timer,
  HelpCircle,
  Send,
  ChevronLeft,
  CheckCircle2,
  AlertCircle,
  Layout,
  Clock
} from 'lucide-react'
import { cn } from '../lib/utils'

interface Question {
  id: string
  type: 'MCQ' | 'TRUE_FALSE' | 'NUMERIC'
  promptText: string
  latexEnabled: boolean
  points: number
  position: number
  mcqOptions?: { options: string[] }
}

interface Quiz {
  id: string
  courseId: string
  title: string
  timeLimitSeconds?: number
  questions: Question[]
}

export default function StudentQuizPage() {
  const { quizId } = useParams<{ quizId: string }>()
  const navigate = useNavigate()

  const [quiz, setQuiz] = useState<Quiz | null>(null)
  const [answers, setAnswers] = useState<Map<string, string>>(new Map())
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [timeRemaining, setTimeRemaining] = useState<number | null>(null)

  useEffect(() => {
    if (!quizId) return

    api.get<Quiz>(`/student/quizzes/${quizId}`)
      .then((res) => {
        setQuiz(res.data)
        if (res.data.timeLimitSeconds) {
          setTimeRemaining(res.data.timeLimitSeconds)
        }
      })
      .catch((err: { response?: { data?: { error?: string } } }) =>
        setError(err.response?.data?.error || 'Failed to load mathematical evaluation. Access might be restricted.')
      )
      .finally(() => setLoading(false))
  }, [quizId])

  useEffect(() => {
    if (timeRemaining === null || timeRemaining <= 0) return

    const timer = setInterval(() => {
      setTimeRemaining(prev => {
        if (prev === null || prev <= 1) {
          handleSubmit()
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => clearInterval(timer)
  }, [timeRemaining])

  const handleAnswerChange = (questionId: string, value: string) => {
    setAnswers(prev => {
      const newMap = new Map(prev)
      newMap.set(questionId, value)
      return newMap
    })
  }

  const handleSubmit = async () => {
    if (!quizId || submitting) return

    setSubmitting(true)

    const answersList = Array.from(answers.entries()).map(([questionId, response]) => ({
      questionId,
      response,
    }))

    try {
      const response = await api.post(`/student/quizzes/${quizId}/submit`, {
        answers: answersList,
      })

      navigate('/student/grades', {
        state: {
          result: response.data,
          quizTitle: quiz?.title
        }
      })
    } catch (err: any) {
      setError(err.response?.data?.error || 'Critical submission error. Please contact infrastructure support.')
    } finally {
      setSubmitting(false)
    }
  }

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins}:${secs.toString().padStart(2, '0')}`
  }

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] space-y-4">
        <div className="w-12 h-12 rounded-full border-4 border-slate-800 border-t-primary animate-spin" />
        <p className="text-slate-500 font-medium">Decrypting Evaluation Data...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="max-w-md mx-auto py-20">
        <div className="bg-destructive/10 border border-destructive/20 rounded-3xl p-8 text-center space-y-6">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-destructive/20 text-destructive mb-2">
            <AlertCircle size={32} />
          </div>
          <h2 className="text-xl font-bold text-white">Access Violation</h2>
          <p className="text-slate-400 text-sm leading-relaxed">{error}</p>
          <Link to="/student/courses" className="block w-full py-3 bg-slate-800 text-white font-bold rounded-xl hover:bg-slate-700 transition-all">
            Return to Courses
          </Link>
        </div>
      </div>
    )
  }

  if (!quiz) return null

  const answeredCount = answers.size
  const totalQuestions = quiz.questions.length
  const progress = Math.round((answeredCount / totalQuestions) * 100)

  return (
    <div className="max-w-4xl mx-auto pb-20">
      {/* Sticky HUD */}
      <div className="sticky top-0 z-50 py-4 -mx-4 px-4 bg-background/80 backdrop-blur-md border-b border-white/5 mb-8">
        <div className="flex items-center justify-between gap-6 max-w-4xl mx-auto">
          <div className="flex items-center gap-3">
            <Link to="/student/courses" className="w-10 h-10 rounded-xl bg-slate-800 flex items-center justify-center text-slate-400 hover:text-white transition-colors">
              <ChevronLeft size={20} />
            </Link>
            <div className="hidden sm:block">
              <h1 className="text-sm font-bold text-white truncate max-w-[200px]">{quiz.title}</h1>
              <p className="text-[10px] text-slate-500 uppercase tracking-widest font-black">Mathematical Evaluation</p>
            </div>
          </div>

          <div className="flex-1 flex items-center gap-4 max-w-md">
            <div className="flex-1 space-y-1.5">
              <div className="flex justify-between text-[10px] font-black uppercase tracking-widest text-slate-500">
                <span>Performance Potential</span>
                <span>{progress}%</span>
              </div>
              <div className="h-1.5 w-full bg-slate-800 rounded-full overflow-hidden">
                <div
                  className="h-full bg-primary rounded-full transition-all duration-500"
                  style={{ width: `${progress}%` }}
                />
              </div>
            </div>
            <div className="shrink-0 flex items-center gap-2 px-3 py-1.5 rounded-xl bg-slate-800/50 border border-slate-700">
              <HelpCircle size={14} className="text-primary" />
              <span className="text-xs font-bold text-white">{answeredCount} of {totalQuestions}</span>
            </div>
          </div>

          <div className={cn(
            "shrink-0 flex items-center gap-2 px-4 py-1.5 rounded-xl font-mono text-sm font-bold border",
            timeRemaining !== null && timeRemaining < 120
              ? "bg-rose-500/10 border-rose-500/20 text-rose-400 animate-pulse"
              : "bg-primary/10 border-primary/20 text-primary"
          )}>
            <Timer size={16} />
            {timeRemaining !== null ? formatTime(timeRemaining) : 'No Limit'}
          </div>
        </div>
      </div>

      {/* Main Container */}
      <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
        <div className="bg-slate-900/40 border border-slate-800 p-8 rounded-[2.5rem] relative overflow-hidden">
          <div className="absolute top-0 right-0 p-8 opacity-10">
            <Layout size={120} className="text-white" />
          </div>
          <div className="relative space-y-4 max-w-2xl">
            <p className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary/10 border border-primary/20 text-[10px] font-black text-primary uppercase tracking-widest">
              Evaluation Protocol
            </p>
            <h2 className="text-3xl font-black text-white">{quiz.title}</h2>
            <p className="text-slate-400 leading-relaxed">
              Please provide your responses with precision. Some questions may require multiple steps. You can review and modify your answers at any time before final submission.
            </p>
            <div className="flex items-center gap-4 pt-4">
              <div className="flex items-center gap-2 text-xs font-bold text-slate-500">
                <Clock size={14} /> {timeRemaining ? Math.round(timeRemaining / 60) : '0'} Minutes
              </div>
              <div className="w-1 h-1 rounded-full bg-slate-700" />
              <div className="flex items-center gap-2 text-xs font-bold text-slate-500">
                <CheckCircle2 size={14} /> Academic Integrity Active
              </div>
            </div>
          </div>
        </div>

        {/* Questions Section */}
        <div className="space-y-6">
          {quiz.questions.map((question, idx) => (
            <div key={question.id} className="relative group">
              <div className="absolute -left-12 top-4 hidden lg:flex flex-col items-center gap-2 opacity-20 group-hover:opacity-100 transition-opacity">
                <span className="text-[10px] font-black text-slate-500 uppercase tracking-widest rotate-[-90deg] translate-y-8">Question</span>
                <span className="text-2xl font-black text-primary">{idx + 1}</span>
              </div>

              <div className="bg-slate-950/50 backdrop-blur-sm border border-slate-800 rounded-3xl p-8 hover:border-slate-700 transition-all shadow-xl">
                <QuizQuestion
                  question={{
                    ...question,
                    mcqOptions: question.mcqOptions?.options,
                  }}
                  answer={answers.get(question.id) || ''}
                  onAnswerChange={handleAnswerChange}
                />

                <div className="mt-6 flex justify-between items-center pt-6 border-t border-white/5">
                  <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Points Value: {question.points}</span>
                  {answers.has(question.id) && (
                    <span className="flex items-center gap-1.5 text-xs font-bold text-emerald-400">
                      <CheckCircle2 size={12} /> Response Recorded
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Submission Panel */}
        <div className="pt-8 flex flex-col items-center">
          <div className="bg-slate-900/60 border border-slate-800 p-10 rounded-[3rem] text-center space-y-6 w-full max-w-2xl transform hover:scale-[1.02] transition-all">
            <div className="space-y-2">
              <h3 className="text-2xl font-black text-white">Finalize Submission</h3>
              <p className="text-slate-400 text-sm">
                {answeredCount === totalQuestions
                  ? 'You have completed all sections. Ready to submit?'
                  : `Warning: You have only responded to ${answeredCount} out of ${totalQuestions} questions.`}
              </p>
            </div>

            <button
              onClick={handleSubmit}
              disabled={submitting || answeredCount === 0}
              className={cn(
                "w-full py-4 rounded-2xl font-black text-lg flex items-center justify-center gap-3 transition-all shadow-2xl",
                submitting || answeredCount === 0
                  ? "bg-slate-800 text-slate-500 cursor-not-allowed"
                  : "bg-primary text-primary-foreground hover:scale-[1.02] active:scale-[0.98] shadow-primary/20"
              )}
            >
              {submitting ? (
                <>
                  <div className="w-5 h-5 rounded-full border-2 border-primary-foreground/30 border-t-primary-foreground animate-spin" />
                  Submitting Results...
                </>
              ) : (
                <>
                  <Send size={20} /> Submit Evaluation
                </>
              )}
            </button>

            <p className="text-[10px] text-slate-600 font-bold uppercase tracking-widest">
              Secure Math ID: {quizId?.slice(-8)}
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
