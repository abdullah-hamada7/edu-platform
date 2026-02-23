import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import api from '../../services/api'
import {
  ChevronLeft,
  Layers,
  Plus,
  BookOpen,
  GraduationCap,
  Users,
  ClipboardCheck,
} from 'lucide-react'

interface CourseDetail {
  id: string
  title: string
  description?: string
  status: string
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

interface User {
  id: string
  email: string
  role: string
  status: string
}

interface Enrollment {
  id: string
  studentId: string
  status: string
  enrolledAt: string
}

interface QuizSummary {
  id: string
  title: string
  status: string
}

interface VideoAsset {
  id: string
  transcodeStatus: string
  hlsManifestKey?: string
}

export default function AdminCourseDetailPage() {
  const { courseId } = useParams<{ courseId: string }>()
  const [course, setCourse] = useState<CourseDetail | null>(null)
  const [students, setStudents] = useState<User[]>([])
  const [enrollments, setEnrollments] = useState<Enrollment[]>([])
  const [quizzes, setQuizzes] = useState<QuizSummary[]>([])
  const [videoAssets, setVideoAssets] = useState<VideoAsset[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [chapterTitle, setChapterTitle] = useState('')
  const [chapterPosition, setChapterPosition] = useState('')
  const [activeChapterId, setActiveChapterId] = useState<string | null>(null)
  const [lessonTitle, setLessonTitle] = useState('')
  const [lessonPosition, setLessonPosition] = useState('')
  const [lessonVideoAssetId, setLessonVideoAssetId] = useState('')
  const [uploading, setUploading] = useState(false)
  const [uploadFile, setUploadFile] = useState<File | null>(null)

  const [selectedStudentId, setSelectedStudentId] = useState('')
  const [quizTitle, setQuizTitle] = useState('')
  const [quizTimeLimit, setQuizTimeLimit] = useState('')
  const [selectedQuizId, setSelectedQuizId] = useState('')
  const [questionType, setQuestionType] = useState<'MCQ' | 'TRUE_FALSE' | 'NUMERIC'>('MCQ')
  const [questionPrompt, setQuestionPrompt] = useState('')
  const [questionPoints, setQuestionPoints] = useState('1')
  const [questionPosition, setQuestionPosition] = useState('')
  const [latexEnabled, setLatexEnabled] = useState(false)
  const [mcqOptions, setMcqOptions] = useState('')
  const [mcqCorrectIndex, setMcqCorrectIndex] = useState('0')
  const [trueFalseValue, setTrueFalseValue] = useState('true')
  const [numericValue, setNumericValue] = useState('')
  const [numericTolerance, setNumericTolerance] = useState('0.01')

  useEffect(() => {
    if (!courseId) return

    Promise.all([
      api.get<CourseDetail>(`/admin/courses/${courseId}`),
      api.get<Enrollment[]>(`/admin/courses/${courseId}/enrollments`),
      api.get<User[]>('/admin/users'),
      api.get<QuizSummary[]>(`/admin/courses/${courseId}/quizzes`),
      api.get<VideoAsset[]>('/admin/videos'),
    ])
      .then(([courseRes, enrollmentRes, usersRes, quizzesRes, videosRes]) => {
        setCourse(courseRes.data)
        setEnrollments(enrollmentRes.data)
        setStudents(usersRes.data.filter(u => u.role === 'STUDENT'))
        setQuizzes(quizzesRes.data)
        setVideoAssets(videosRes.data)
        setError(null)
      })
      .catch((err) => {
        console.error('Failed to load course detail:', err)
        setError(err.response?.data?.error || 'Failed to load course detail.')
      })
      .finally(() => setLoading(false))
  }, [courseId])

  const refreshCourse = async () => {
    if (!courseId) return
    const response = await api.get<CourseDetail>(`/admin/courses/${courseId}`)
    setCourse(response.data)
  }

  const refreshEnrollments = async () => {
    if (!courseId) return
    const response = await api.get<Enrollment[]>(`/admin/courses/${courseId}/enrollments`)
    setEnrollments(response.data)
  }

  const refreshQuizzes = async () => {
    if (!courseId) return
    const response = await api.get<QuizSummary[]>(`/admin/courses/${courseId}/quizzes`)
    setQuizzes(response.data)
  }

  const refreshVideoAssets = async () => {
    const response = await api.get<VideoAsset[]>('/admin/videos')
    setVideoAssets(response.data)
  }

  const handleCreateChapter = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!courseId || !chapterTitle.trim()) return

    try {
      await api.post(`/admin/courses/${courseId}/chapters`, {
        title: chapterTitle,
        position: chapterPosition ? Number(chapterPosition) : undefined,
      })
      setChapterTitle('')
      setChapterPosition('')
      setError(null)
      refreshCourse()
    } catch (err: any) {
      console.error('Failed to create chapter:', err)
      setError(err.response?.data?.error || 'Failed to create chapter.')
    }
  }

  const handleCreateLesson = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!activeChapterId || !lessonTitle.trim()) return

    try {
      await api.post(`/admin/courses/chapters/${activeChapterId}/lessons`, {
        title: lessonTitle,
        position: lessonPosition ? Number(lessonPosition) : undefined,
        videoAssetId: lessonVideoAssetId || undefined,
      })
      setLessonTitle('')
      setLessonPosition('')
      setLessonVideoAssetId('')
      setActiveChapterId(null)
      setError(null)
      refreshCourse()
    } catch (err: any) {
      console.error('Failed to create lesson:', err)
      setError(err.response?.data?.error || 'Failed to create lesson.')
    }
  }

  const handleEnrollStudent = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!courseId || !selectedStudentId) return

    try {
      await api.post(`/admin/courses/${courseId}/enrollments`, {
        studentId: selectedStudentId,
      })
      setSelectedStudentId('')
      setError(null)
      refreshEnrollments()
    } catch (err: any) {
      console.error('Failed to enroll student:', err)
      setError(err.response?.data?.error || 'Failed to enroll student.')
    }
  }

  const handleCreateQuiz = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!courseId || !quizTitle.trim()) return

    try {
      await api.post(`/admin/courses/${courseId}/quizzes`, {
        title: quizTitle,
        timeLimitSeconds: quizTimeLimit ? Number(quizTimeLimit) : undefined,
        status: 'DRAFT',
      })
      setQuizTitle('')
      setQuizTimeLimit('')
      setError(null)
      refreshQuizzes()
    } catch (err: any) {
      console.error('Failed to create quiz:', err)
      setError(err.response?.data?.error || 'Failed to create quiz.')
    }
  }

  const buildAnswerKey = () => {
    if (questionType === 'MCQ') {
      const options = mcqOptions.split('\n').map(o => o.trim()).filter(Boolean)
      return JSON.stringify({
        options,
        correctIndex: Number(mcqCorrectIndex),
      })
    }
    if (questionType === 'TRUE_FALSE') {
      return JSON.stringify({ value: trueFalseValue === 'true' })
    }
    return JSON.stringify({
      value: Number(numericValue),
      tolerance: Number(numericTolerance || '0.01'),
    })
  }

  const handleAddQuestion = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!selectedQuizId || !questionPrompt.trim()) return

    try {
      await api.post(`/admin/quizzes/${selectedQuizId}/questions`, {
        type: questionType,
        promptText: questionPrompt,
        latexEnabled,
        answerKey: buildAnswerKey(),
        points: Number(questionPoints),
        position: questionPosition ? Number(questionPosition) : undefined,
      })
      setQuestionPrompt('')
      setQuestionPoints('1')
      setQuestionPosition('')
      setMcqOptions('')
      setMcqCorrectIndex('0')
      setNumericValue('')
      setNumericTolerance('0.01')
      setError(null)
    } catch (err: any) {
      console.error('Failed to add question:', err)
      setError(err.response?.data?.error || 'Failed to add question.')
    }
  }

  const handlePublishQuiz = async (quizId: string) => {
    try {
      await api.post(`/admin/quizzes/${quizId}/publish`)
      setError(null)
      refreshQuizzes()
    } catch (err: any) {
      console.error('Failed to publish quiz:', err)
      setError(err.response?.data?.error || 'Failed to publish quiz.')
    }
  }

  const handlePublishCourse = async () => {
    if (!courseId) return
    try {
      await api.post(`/admin/courses/${courseId}/publish`)
      refreshCourse()
    } catch (err: any) {
      console.error('Failed to publish course:', err)
      setError(err.response?.data?.error || 'Failed to publish course.')
    }
  }

  const handleArchiveCourse = async () => {
    if (!courseId) return
    try {
      await api.post(`/admin/courses/${courseId}/archive`)
      refreshCourse()
    } catch (err: any) {
      console.error('Failed to archive course:', err)
      setError(err.response?.data?.error || 'Failed to archive course.')
    }
  }

  const handleUploadVideo = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!uploadFile) return
    const formData = new FormData()
    formData.append('file', uploadFile)
    try {
      setUploading(true)
      await api.post('/admin/videos/upload', formData)
      setUploadFile(null)
      setError(null)
      refreshVideoAssets()
    } catch (err: any) {
      console.error('Failed to upload video:', err)
      setError(err.response?.data?.error || 'Failed to upload video.')
    } finally {
      setUploading(false)
    }
  }

  const handleRefreshVideo = async (assetId: string) => {
    try {
      await api.post(`/admin/videos/${assetId}/refresh`)
      setError(null)
      refreshVideoAssets()
    } catch (err: any) {
      console.error('Failed to refresh video asset:', err)
      setError(err.response?.data?.error || 'Failed to refresh video asset.')
    }
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="h-6 w-40 bg-slate-800 animate-pulse rounded-lg" />
        <div className="h-10 w-72 bg-slate-800 animate-pulse rounded-lg" />
      </div>
    )
  }

  if (!course) {
    return (
      <div className="text-center py-20">
        <p className="text-slate-500">Course not found.</p>
      </div>
    )
  }

  const studentLookup = new Map(students.map(s => [s.id, s.email]))

  return (
    <div className="space-y-10 pb-20">
      <div className="space-y-4">
        <Link to="/admin/courses" className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-white transition-colors">
          <ChevronLeft size={16} /> Back to Courses
        </Link>
        <div className="flex items-center justify-between gap-6">
          <div className="space-y-1">
            <h1 className="text-3xl font-black text-white flex items-center gap-3">
              <Layers size={24} className="text-primary" /> {course.title}
            </h1>
            <p className="text-slate-400 max-w-2xl">{course.description || 'No description provided for this curriculum.'}</p>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-[10px] font-black uppercase tracking-widest text-slate-500">Status: {course.status}</span>
            {course.status !== 'PUBLISHED' && (
              <button
                onClick={handlePublishCourse}
                className="px-3 py-1.5 rounded-lg bg-primary/20 text-primary text-[10px] font-black uppercase tracking-widest"
              >
                Publish
              </button>
            )}
            {course.status !== 'ARCHIVED' && (
              <button
                onClick={handleArchiveCourse}
                className="px-3 py-1.5 rounded-lg bg-slate-800 text-slate-300 text-[10px] font-black uppercase tracking-widest"
              >
                Archive
              </button>
            )}
          </div>
        </div>
      </div>

      {error && (
        <div className="bg-destructive/10 border border-destructive/20 text-destructive text-sm rounded-2xl p-4">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="space-y-6">
          <div className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl space-y-4">
            <h2 className="text-lg font-black text-white flex items-center gap-2"><BookOpen size={18} className="text-primary" /> Video Assets</h2>
            <form onSubmit={handleUploadVideo} className="flex flex-col md:flex-row gap-3">
              <input
                type="file"
                accept="video/*"
                onChange={(e) => setUploadFile(e.target.files?.[0] || null)}
                className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-200"
              />
              <button
                type="submit"
                disabled={!uploadFile || uploading}
                className="px-4 py-2 bg-primary text-primary-foreground rounded-xl text-xs font-black uppercase tracking-widest disabled:opacity-60"
              >
                {uploading ? 'Uploading...' : 'Upload'}
              </button>
            </form>
            <div className="space-y-2">
              {videoAssets.length === 0 ? (
                <p className="text-xs text-slate-500">No video assets uploaded yet.</p>
              ) : (
                videoAssets.map(asset => (
                  <div key={asset.id} className="flex items-center justify-between text-xs text-slate-300 bg-slate-950/60 border border-slate-800 rounded-xl px-4 py-2">
                    <div>
                      <p className="font-bold">{asset.id}</p>
                      <p className="text-[10px] uppercase tracking-widest text-slate-500">{asset.transcodeStatus}</p>
                    </div>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => setLessonVideoAssetId(asset.id)}
                        className="px-2 py-1 rounded-lg bg-primary/20 text-primary text-[10px] font-black uppercase"
                      >
                        Use
                      </button>
                      <button
                        onClick={() => handleRefreshVideo(asset.id)}
                        className="px-2 py-1 rounded-lg bg-slate-800 text-slate-300 text-[10px] font-black uppercase"
                      >
                        Refresh
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
          <div className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl space-y-4">
            <h2 className="text-lg font-black text-white flex items-center gap-2"><BookOpen size={18} className="text-primary" /> Add Chapter</h2>
            <form onSubmit={handleCreateChapter} className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <input
                value={chapterTitle}
                onChange={(e) => setChapterTitle(e.target.value)}
                placeholder="Chapter title"
                className="md:col-span-2 bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-200"
                required
              />
              <input
                value={chapterPosition}
                onChange={(e) => setChapterPosition(e.target.value)}
                placeholder="Position"
                type="number"
                className="bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-200"
              />
              <div className="md:col-span-3 flex justify-end">
                <button className="px-4 py-2 bg-primary text-primary-foreground rounded-xl text-xs font-black uppercase tracking-widest">
                  Add Chapter
                </button>
              </div>
            </form>
          </div>

          <div className="space-y-4">
            {course.chapters.map((chapter) => (
              <div key={chapter.id} className="bg-slate-900/40 border border-slate-800 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 flex items-center justify-between border-b border-slate-800/50">
                  <div>
                    <p className="text-sm font-black text-white">{chapter.title}</p>
                    <p className="text-[10px] text-slate-500 uppercase tracking-widest">Chapter {chapter.position + 1}</p>
                  </div>
                  <button
                    onClick={() => setActiveChapterId(activeChapterId === chapter.id ? null : chapter.id)}
                    className="text-xs font-bold text-primary flex items-center gap-1"
                  >
                    <Plus size={14} /> Add Lesson
                  </button>
                </div>
                {activeChapterId === chapter.id && (
                  <form onSubmit={handleCreateLesson} className="p-4 grid grid-cols-1 md:grid-cols-4 gap-3 bg-slate-950/40">
                    <input
                      value={lessonTitle}
                      onChange={(e) => setLessonTitle(e.target.value)}
                      placeholder="Lesson title"
                      className="md:col-span-2 bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-slate-200"
                      required
                    />
                    <input
                      value={lessonPosition}
                      onChange={(e) => setLessonPosition(e.target.value)}
                      placeholder="Position"
                      type="number"
                      className="bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-slate-200"
                    />
                    <input
                      value={lessonVideoAssetId}
                      onChange={(e) => setLessonVideoAssetId(e.target.value)}
                      placeholder="Video Asset ID (optional)"
                      className="bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-slate-200"
                    />
                    <div className="md:col-span-4 flex justify-end">
                      <button className="px-4 py-2 bg-primary text-primary-foreground rounded-xl text-xs font-black uppercase tracking-widest">Create Lesson</button>
                    </div>
                  </form>
                )}
                <div className="divide-y divide-slate-800/40">
                  {chapter.lessons.map((lesson) => (
                    <div key={lesson.id} className="px-6 py-3 flex items-center justify-between">
                      <p className="text-sm text-slate-300">{lesson.title}</p>
                      <span className="text-[10px] uppercase tracking-widest text-slate-500">{lesson.hasVideo ? 'video' : 'no video'}</span>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl space-y-4">
            <h2 className="text-lg font-black text-white flex items-center gap-2"><Users size={18} className="text-primary" /> Enroll Student</h2>
            <form onSubmit={handleEnrollStudent} className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <select
                value={selectedStudentId}
                onChange={(e) => setSelectedStudentId(e.target.value)}
                className="md:col-span-2 bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-200"
                required
              >
                <option value="">Select student</option>
                {students.map(student => (
                  <option key={student.id} value={student.id}>{student.email}</option>
                ))}
              </select>
              <button className="px-4 py-2 bg-primary text-primary-foreground rounded-xl text-xs font-black uppercase tracking-widest">
                Enroll
              </button>
            </form>
            <div className="space-y-2">
              {enrollments.map(enrollment => (
                <div key={enrollment.id} className="flex items-center justify-between text-xs text-slate-400 bg-slate-950/60 border border-slate-800 rounded-xl px-4 py-2">
                  <span>{studentLookup.get(enrollment.studentId) || enrollment.studentId}</span>
                  <span className="uppercase tracking-widest">{enrollment.status}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl space-y-4">
            <h2 className="text-lg font-black text-white flex items-center gap-2"><ClipboardCheck size={18} className="text-primary" /> Create Quiz</h2>
            <form onSubmit={handleCreateQuiz} className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <input
                value={quizTitle}
                onChange={(e) => setQuizTitle(e.target.value)}
                placeholder="Quiz title"
                className="md:col-span-2 bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-200"
                required
              />
              <input
                value={quizTimeLimit}
                onChange={(e) => setQuizTimeLimit(e.target.value)}
                placeholder="Time limit (sec)"
                type="number"
                className="bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-200"
              />
              <div className="md:col-span-3 flex justify-end">
                <button className="px-4 py-2 bg-primary text-primary-foreground rounded-xl text-xs font-black uppercase tracking-widest">Create Quiz</button>
              </div>
            </form>
            <div className="space-y-2">
              {quizzes.map(quiz => (
                <div key={quiz.id} className="flex items-center justify-between text-xs text-slate-300 bg-slate-950/60 border border-slate-800 rounded-xl px-4 py-2">
                  <span className="font-bold">{quiz.title}</span>
                  <div className="flex items-center gap-2">
                    <span className="uppercase tracking-widest text-slate-500">{quiz.status}</span>
                    {quiz.status !== 'PUBLISHED' && (
                      <button
                        onClick={() => handlePublishQuiz(quiz.id)}
                        className="px-2 py-1 rounded-lg bg-primary/20 text-primary text-[10px] font-black uppercase"
                      >
                        Publish
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl space-y-4">
            <h2 className="text-lg font-black text-white flex items-center gap-2"><GraduationCap size={18} className="text-primary" /> Add Quiz Question</h2>
            <form onSubmit={handleAddQuestion} className="space-y-4">
              <select
                value={selectedQuizId}
                onChange={(e) => setSelectedQuizId(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-200"
                required
              >
                <option value="">Select quiz</option>
                {quizzes.map(quiz => (
                  <option key={quiz.id} value={quiz.id}>{quiz.title}</option>
                ))}
              </select>
              <textarea
                value={questionPrompt}
                onChange={(e) => setQuestionPrompt(e.target.value)}
                placeholder="Question prompt (supports LaTeX if enabled)"
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-slate-200 min-h-[100px]"
                required
              />
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <select
                  value={questionType}
                  onChange={(e) => setQuestionType(e.target.value as 'MCQ' | 'TRUE_FALSE' | 'NUMERIC')}
                  className="bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-200"
                >
                  <option value="MCQ">MCQ</option>
                  <option value="TRUE_FALSE">True/False</option>
                  <option value="NUMERIC">Numeric</option>
                </select>
                <input
                  value={questionPoints}
                  onChange={(e) => setQuestionPoints(e.target.value)}
                  placeholder="Points"
                  type="number"
                  className="bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-200"
                />
                <input
                  value={questionPosition}
                  onChange={(e) => setQuestionPosition(e.target.value)}
                  placeholder="Position"
                  type="number"
                  className="bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-200"
                />
                <label className="flex items-center gap-2 text-xs text-slate-400">
                  <input
                    type="checkbox"
                    checked={latexEnabled}
                    onChange={(e) => setLatexEnabled(e.target.checked)}
                    className="rounded border-slate-700 bg-slate-900"
                  />
                  LaTeX Enabled
                </label>
              </div>

              {questionType === 'MCQ' && (
                <div className="space-y-2">
                  <textarea
                    value={mcqOptions}
                    onChange={(e) => setMcqOptions(e.target.value)}
                    placeholder="MCQ options (one per line)"
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-slate-200 min-h-[100px]"
                  />
                  <input
                    value={mcqCorrectIndex}
                    onChange={(e) => setMcqCorrectIndex(e.target.value)}
                    placeholder="Correct option index (0-based)"
                    type="number"
                    className="bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-200"
                  />
                </div>
              )}

              {questionType === 'TRUE_FALSE' && (
                <select
                  value={trueFalseValue}
                  onChange={(e) => setTrueFalseValue(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-200"
                >
                  <option value="true">True</option>
                  <option value="false">False</option>
                </select>
              )}

              {questionType === 'NUMERIC' && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <input
                    value={numericValue}
                    onChange={(e) => setNumericValue(e.target.value)}
                    placeholder="Expected value"
                    type="number"
                    className="bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-200"
                  />
                  <input
                    value={numericTolerance}
                    onChange={(e) => setNumericTolerance(e.target.value)}
                    placeholder="Tolerance"
                    type="number"
                    className="bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-200"
                  />
                </div>
              )}

              <div className="flex justify-end">
                <button className="px-4 py-2 bg-primary text-primary-foreground rounded-xl text-xs font-black uppercase tracking-widest">Add Question</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}
