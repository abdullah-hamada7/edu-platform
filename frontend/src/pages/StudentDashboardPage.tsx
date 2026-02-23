export default function StudentDashboardPage() {
  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-white shadow">
        <div className="container mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">Student Dashboard</h1>
          <button 
            onClick={() => {
              localStorage.removeItem('accessToken')
              window.location.href = '/login'
            }}
            className="text-sm text-gray-600 hover:text-gray-800"
          >
            Logout
          </button>
        </div>
      </header>
      
      <main className="container mx-auto px-4 py-8">
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <a 
            href="/student/courses" 
            className="bg-white p-6 rounded-lg shadow hover:shadow-md transition-shadow"
          >
            <h2 className="text-lg font-semibold mb-2">My Courses</h2>
            <p className="text-gray-600">View enrolled courses and lessons</p>
          </a>
          
          <a 
            href="/student/grades" 
            className="bg-white p-6 rounded-lg shadow hover:shadow-md transition-shadow"
          >
            <h2 className="text-lg font-semibold mb-2">My Grades</h2>
            <p className="text-gray-600">View quiz scores and progress</p>
          </a>
        </div>
      </main>
    </div>
  )
}
