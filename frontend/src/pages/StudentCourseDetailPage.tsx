import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import api from '../services/api'

interface CourseDetail {
  id: string
  title: string
  description?: string
  chapters: Chapter[]
}

interface Chapter {
  id: string
  title: string
  position: number
  lessons: Lesson[]
}

interface Lesson {
  id: string
  title: string
  position: number
  hasVideo: boolean
}

export default function StudentCourseDetailPage() {
  const { courseId } = useParams<{ courseId: string }>()
  const [course, setCourse] = useState<CourseDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [quizzes, setQuizzes] = useState<any[]>([])

  useEffect(() => {
    if (!courseId) return

    Promise.all([
      api.get(`/student/courses/${courseId}`),
      api.get(`/student/quizzes?courseId=${courseId}`)
    ])
      .then(([courseRes, quizzesRes]) => {
        setCourse(courseRes.data)
        setQuizzes(quizzesRes.data || [])
      })
      .finally(() => setLoading(false))
  }, [courseId])

  if (loading) {
    return <div className="flex justify-center items-center min-h-screen">Loading...</div>
  }

  if (!course) {
    return <p>Course not found</p>
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <h1 className="text-2xl font-bold mb-2">{course.title}</h1>
      <p className="text-gray-600 mb-6">{course.description}</p>

      {quizzes.length > 0 && (
        <div className="mb-8 bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">Quizzes</h2>
          <div className="space-y-2">
            {quizzes.map((quiz: any) => (
              <a
                key={quiz.id}
                href={`/student/quizzes/${quiz.id}`}
                className="block p-3 bg-gray-50 rounded hover:bg-gray-100"
              >
                <span className="font-medium">{quiz.title}</span>
              </a>
            ))}
          </div>
        </div>
      )}

      <div className="bg-white rounded-lg shadow">
        <h2 className="text-lg font-semibold p-4 border-b">Content</h2>

        {course.chapters.map(chapter => (
          <div key={chapter.id} className="border-b last:border-b-0">
            <div className="p-4 bg-gray-50 font-medium">
              {chapter.position + 1}. {chapter.title}
            </div>
            <div className="divide-y">
              {chapter.lessons.map(lesson => (
                <div key={lesson.id} className="p-4 flex justify-between items-center">
                  <span>{lesson.title}</span>
                  {lesson.hasVideo && (
                    <a
                      href={`/student/lessons/${lesson.id}`}
                      className="text-blue-600 hover:underline text-sm"
                    >
                      Watch Video
                    </a>
                  )}
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
