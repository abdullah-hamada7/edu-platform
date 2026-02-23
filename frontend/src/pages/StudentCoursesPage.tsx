import { useEffect, useState } from 'react'
import { courseApi, Course } from '../services/api'
import { Link } from 'react-router-dom'
import { BookOpen, ArrowRight, Layers, Layout, Clock } from 'lucide-react'

export default function StudentCoursesPage() {
  const [courses, setCourses] = useState<Course[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    courseApi.listEnrolled()
      .then(setCourses)
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="h-8 w-48 bg-slate-800 animate-pulse rounded-lg" />
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3].map(i => (
            <div key={i} className="h-64 bg-slate-900/40 border border-slate-800 rounded-3xl animate-pulse" />
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-col gap-1">
        <h1 className="text-3xl font-bold tracking-tight">Your Courses</h1>
        <p className="text-slate-400">Continue your educational journey with these modules.</p>
      </div>

      {courses.length === 0 ? (
        <div className="bg-slate-900/40 border border-slate-800 rounded-3xl p-12 text-center space-y-4">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-slate-800 text-slate-400 mb-2">
            <BookOpen size={32} />
          </div>
          <h2 className="text-xl font-bold text-white">No courses yet</h2>
          <p className="text-slate-400 max-w-xs mx-auto">You are not enrolled in any courses. Explore the catalog to get started!</p>
          <Link to="/student" className="bg-primary text-primary-foreground px-6 py-2.5 rounded-xl font-semibold hover:opacity-90 transition-all">
            Back to Dashboard
          </Link>
        </div>
      ) : (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {courses.map(course => (
            <div
              key={course.id}
              className="group relative bg-slate-900/40 border border-slate-800 rounded-3xl p-8 hover:border-primary/50 transition-all hover:shadow-2xl hover:shadow-primary/5 flex flex-col"
            >
              <div className="mb-6 flex items-start justify-between">
                <div className="p-3 rounded-2xl bg-blue-600/10 text-blue-400 border border-blue-600/20">
                  <Layers size={24} />
                </div>
                <div className="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-wider text-emerald-400 bg-emerald-400/10 px-2.5 py-1 rounded-full border border-emerald-400/20">
                  <Clock size={10} /> Active
                </div>
              </div>

              <div className="flex-1 space-y-3">
                <h2 className="text-xl font-bold text-white group-hover:text-primary transition-colors line-clamp-1">{course.title}</h2>
                <p className="text-slate-400 text-sm leading-relaxed line-clamp-2">
                  {course.description || 'No description provided for this course. Start learning to explore its contents.'}
                </p>
              </div>

              <div className="mt-8 pt-6 border-t border-slate-800/20 flex items-center justify-between">
                <div className="flex items-center gap-2 text-xs font-medium text-slate-500">
                  <Layout size={14} /> 8 Lessons
                </div>
                <Link
                  to={`/student/courses/${course.id}`}
                  className="inline-flex items-center gap-2 text-sm font-bold text-primary hover:text-white transition-colors"
                >
                  Enter Course <ArrowRight size={16} />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
