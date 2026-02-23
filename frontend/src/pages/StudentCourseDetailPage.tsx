import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../services/api'
import {
  ChevronLeft,
  PlayCircle,
  FileText,
  Clock,
  ArrowRight,
  BookOpen,
  Layout
} from 'lucide-react'

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
    return (
      <div className="space-y-6">
        <div className="h-4 w-32 bg-slate-800 animate-pulse rounded" />
        <div className="h-10 w-96 bg-slate-800 animate-pulse rounded-lg" />
        <div className="space-y-4">
          {[1, 2, 3].map(i => (
            <div key={i} className="h-48 bg-slate-900/40 border border-slate-800 rounded-3xl animate-pulse" />
          ))}
        </div>
      </div>
    )
  }

  if (!course) {
    return (
      <div className="text-center py-20">
        <h2 className="text-2xl font-bold text-white">Course not found</h2>
        <Link to="/student/courses" className="text-primary hover:underline mt-4 inline-block">Return to Catalog</Link>
      </div>
    )
  }

  return (
    <div className="max-w-5xl mx-auto space-y-8 pb-20">
      {/* Breadcrumbs & Title */}
      <div className="space-y-4">
        <Link to="/student/courses" className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-white transition-colors">
          <ChevronLeft size={16} /> Back to Courses
        </Link>
        <div className="flex flex-col gap-2">
          <h1 className="text-3xl md:text-4xl font-extrabold text-white tracking-tight leading-tight">{course.title}</h1>
          <p className="text-slate-400 text-lg max-w-2xl">{course.description || 'Master the concepts of this curriculum through structured lessons and challenging evaluations.'}</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
        {/* Course Content */}
        <div className="lg:col-span-2 space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-bold flex items-center gap-2">
              <Layout size={20} className="text-primary" />
              Course Curriculum
            </h2>
            <span className="text-xs font-medium text-slate-500 uppercase tracking-widest">{course.chapters.length} Chapters</span>
          </div>

          <div className="space-y-4">
            {course.chapters.map((chapter, idx) => (
              <div key={chapter.id} className="bg-slate-900/40 border border-slate-800 rounded-3xl overflow-hidden">
                <div className="px-6 py-4 bg-slate-800/30 border-b border-slate-800/50 flex items-center justify-between">
                    <div className="flex items-center gap-4">
                        <span className="text-xs font-bold text-primary bg-primary/10 w-8 h-8 rounded-lg flex items-center justify-center border border-primary/20">{idx + 1}</span>
                        <h3 className="font-bold text-white">{chapter.title}</h3>
                    </div>
                    <span className="text-xs text-slate-500 font-medium">{chapter.lessons.length} lessons</span>
                </div>
                <div className="divide-y divide-slate-800/30">
                  {chapter.lessons.map((lesson) => (
                    <div key={lesson.id} className="group p-4 px-6 flex items-center justify-between hover:bg-slate-800/20 transition-all">
                      <div className="flex items-center gap-3">
                        <div className="text-slate-600 group-hover:text-primary transition-colors">
                          {lesson.hasVideo ? <PlayCircle size={18} /> : <FileText size={18} />}
                        </div>
                        <span className="text-sm font-medium text-slate-300 group-hover:text-white transition-colors">{lesson.title}</span>
                      </div>
                      {lesson.hasVideo && (
                        <Link
                          to={`/student/lessons/${lesson.id}`}
                          className="px-3 py-1.5 rounded-lg bg-slate-800 text-xs font-bold text-slate-300 hover:bg-primary hover:text-primary-foreground transition-all flex items-center gap-1.5"
                        >
                          Launch <ArrowRight size={12} />
                        </Link>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Sidebar / Quizzes */}
        <div className="space-y-6">
          <div className="bg-slate-900/40 border border-slate-800 p-6 rounded-3xl sticky top-8">
            <h3 className="text-lg font-bold mb-6 flex items-center gap-2">
              <BookOpen size={20} className="text-amber-400" />
              Knowledge Checks
            </h3>

            {quizzes.length === 0 ? (
              <div className="text-center py-6 px-4 rounded-2xl bg-slate-950/50 border border-slate-800/50">
                <p className="text-xs text-slate-500 italic">No quizzes available for this course yet.</p>
              </div>
            ) : (
              <div className="space-y-3">
                {quizzes.map((quiz) => (
                  <Link
                    key={quiz.id}
                    to={`/student/quizzes/${quiz.id}`}
                    className="flex items-center justify-between p-4 rounded-2xl bg-slate-800/40 border border-slate-800 hover:border-amber-400/50 hover:bg-slate-800 transition-all group"
                  >
                    <div className="space-y-1">
                      <p className="text-sm font-bold text-white group-hover:text-amber-400 transition-colors">{quiz.title}</p>
                      <div className="flex items-center gap-2 text-[10px] text-slate-500 font-bold uppercase tracking-wider">
                        <Clock size={10} /> 15 mins
                      </div>
                    </div>
                    <ArrowRight size={16} className="text-slate-600 group-hover:text-amber-400 group-hover:translate-x-1 transition-all" />
                  </Link>
                ))}
              </div>
            )}

            <div className="mt-8 p-4 rounded-2xl bg-primary/5 border border-primary/10">
              <p className="text-[10px] font-bold text-primary uppercase tracking-widest mb-2 text-center">Your Progress</p>
              <div className="flex items-end justify-center gap-1 mb-2">
                <span className="text-2xl font-black text-white leading-none">65</span>
                <span className="text-xs text-slate-500 font-bold mb-0.5">%</span>
              </div>
              <div className="h-1.5 w-full bg-slate-800 rounded-full overflow-hidden">
                <div className="h-full w-[65%] bg-primary rounded-full" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
