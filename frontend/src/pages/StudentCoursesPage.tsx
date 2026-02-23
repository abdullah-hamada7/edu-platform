import { useEffect, useState } from 'react'
import { courseApi, Course } from '../services/api'
import { Link } from 'react-router-dom'

export default function StudentCoursesPage() {
  const [courses, setCourses] = useState<Course[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    courseApi.listEnrolled()
      .then(setCourses)
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <div className="flex justify-center items-center min-h-screen">Loading courses...</div>
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">My Courses</h1>

      {courses.length === 0 ? (
        <p className="text-gray-600">You are not enrolled in any courses yet.</p>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {courses.map(course => (
            <div key={course.id} className="bg-white rounded-lg shadow p-6">
              <h2 className="text-lg font-semibold mb-2">{course.title}</h2>
              <p className="text-gray-600 text-sm mb-4">{course.description}</p>
              <Link 
                to={`/student/courses/${course.id}`}
                className="text-blue-600 hover:underline text-sm"
              >
                View Course
              </Link>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
