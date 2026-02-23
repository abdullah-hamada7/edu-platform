import { useParams, useNavigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import QuizQuestion from '../features/quiz/QuizQuestion'
import api from '../services/api'

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
        setError(err.response?.data?.error || 'Failed to load quiz')
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
      setError(err.response?.data?.error || 'Failed to submit quiz')
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
    return <div className="flex justify-center items-center min-h-screen">Loading quiz...</div>
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen">
        <p className="text-red-600 mb-4">{error}</p>
        <button
          onClick={() => navigate('/student/courses')}
          className="px-4 py-2 bg-blue-600 text-white rounded"
        >
          Back to Courses
        </button>
      </div>
    )
  }

  if (!quiz) {
    return <p>Quiz not found</p>
  }

  const answeredCount = answers.size
  const totalQuestions = quiz.questions.length

  return (
    <div className="container mx-auto px-4 py-8 max-w-3xl">
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h1 className="text-2xl font-bold mb-2">{quiz.title}</h1>

        <div className="flex justify-between items-center text-sm text-gray-600">
          <span>{totalQuestions} questions</span>
          {timeRemaining !== null && (
            <span className={`font-mono ${timeRemaining < 60 ? 'text-red-600 font-bold' : ''}`}>
              Time: {formatTime(timeRemaining)}
            </span>
          )}
        </div>

        <div className="mt-4 bg-gray-200 rounded-full h-2">
          <div
            className="bg-blue-600 h-2 rounded-full transition-all"
            style={{ width: `${(answeredCount / totalQuestions) * 100}%` }}
          />
        </div>
        <p className="text-sm text-gray-600 mt-1">
          {answeredCount} of {totalQuestions} answered
        </p>
      </div>

      {quiz.questions.map((question) => (
        <QuizQuestion
          key={question.id}
          question={{
            ...question,
            mcqOptions: question.mcqOptions?.options,
          }}
          answer={answers.get(question.id) || ''}
          onAnswerChange={handleAnswerChange}
        />
      ))}

      <div className="mt-6 flex justify-end">
        <button
          onClick={handleSubmit}
          disabled={submitting || answeredCount < totalQuestions}
          className={`px-6 py-3 rounded-lg font-medium ${submitting || answeredCount < totalQuestions
              ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
              : 'bg-blue-600 text-white hover:bg-blue-700'
            }`}
        >
          {submitting ? 'Submitting...' : 'Submit Quiz'}
        </button>
      </div>
    </div>
  )
}
