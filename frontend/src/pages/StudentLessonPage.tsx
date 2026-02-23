import { useParams, useNavigate, Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import SecureHlsPlayer from '../features/player/SecureHlsPlayer'
import { playbackApi, PlaybackGrant } from '../services/api'
import {
  ChevronLeft,
  AlertTriangle,
  RefreshCcw,
  Play,
  MonitorPlay,
  ArrowLeft,
  FileText
} from 'lucide-react'

export default function StudentLessonPage() {
  const { lessonId } = useParams<{ lessonId: string }>()
  const navigate = useNavigate()
  const [grant, setGrant] = useState<PlaybackGrant | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!lessonId) return

    const fingerprint = localStorage.getItem('deviceFingerprint') || generateFingerprint()
    localStorage.setItem('deviceFingerprint', fingerprint)

    playbackApi
      .requestGrant(lessonId, fingerprint)
      .then(setGrant)
      .catch((err) => {
        setError(err.response?.data?.error || 'Access Denied: You might have reached your active device limit or are not enrolled in this course.')
      })
      .finally(() => setLoading(false))
  }, [lessonId])

  const handleExpired = () => {
    setError('Playback session expired for security reasons. Please refresh the page to continue learning.')
  }

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] space-y-6">
        <div className="relative">
          <div className="w-20 h-20 rounded-full border-4 border-slate-800 border-t-primary animate-spin" />
          <div className="absolute inset-0 flex items-center justify-center">
            <Play size={24} className="text-primary" />
          </div>
        </div>
        <div className="text-center space-y-2">
          <p className="text-xl font-bold text-white">Initializing Secure Stream</p>
          <p className="text-slate-500 text-sm">Authorizing your device and preparing mathematics content...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="max-w-2xl mx-auto py-20 px-6">
        <div className="bg-destructive/10 border border-destructive/20 rounded-3xl p-10 text-center space-y-6 animate-in zoom-in duration-300">
          <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-destructive/20 text-destructive mb-2">
            <AlertTriangle size={40} />
          </div>
          <div className="space-y-2">
            <h2 className="text-2xl font-black text-white">Playback Interrupted</h2>
            <p className="text-slate-400 leading-relaxed">{error}</p>
          </div>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-3">
            <button
              onClick={() => window.location.reload()}
              className="w-full sm:w-auto px-6 py-3 bg-white text-black font-bold rounded-xl flex items-center justify-center gap-2 hover:bg-slate-200 transition-all"
            >
              <RefreshCcw size={18} /> Retry Connection
            </button>
            <button
              onClick={() => navigate('/student/courses')}
              className="w-full sm:w-auto px-6 py-3 bg-slate-800 text-white font-bold rounded-xl flex items-center justify-center gap-2 hover:bg-slate-700 transition-all"
            >
              <ArrowLeft size={18} /> Return to Courses
            </button>
          </div>
        </div>
      </div>
    )
  }

  if (!grant) {
    return (
      <div className="text-center py-20">
        <p className="text-slate-500">No content available for this lesson.</p>
      </div>
    )
  }

  return (
    <div className="max-w-5xl mx-auto space-y-8 pb-20">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div className="space-y-1">
          <Link to="/student/courses" className="inline-flex items-center gap-2 text-xs font-bold text-slate-500 hover:text-white uppercase tracking-widest transition-colors">
            <ChevronLeft size={14} /> Back to Curriculum
          </Link>
          <h1 className="text-3xl font-black text-white tracking-tight flex items-center gap-3 pt-1">
            <MonitorPlay size={28} className="text-primary" />
            Interactive Lesson
          </h1>
        </div>

        <div className="hidden md:flex items-center gap-4 bg-slate-900/50 border border-slate-800 rounded-2xl p-2 px-4">
          <div className="text-right">
            <p className="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Active Stream</p>
            <p className="text-xs font-bold text-emerald-400">Security Verified</p>
          </div>
          <div className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse outline outline-4 outline-emerald-500/20" />
        </div>
      </div>

      {/* Primary Video Container */}
      <div className="group relative bg-black rounded-[2.5rem] overflow-hidden shadow-2xl border border-white/5 ring-1 ring-white/10 ring-inset">
        <SecureHlsPlayer
          manifestUrl={grant.manifestUrl}
          watermarkSeed={grant.watermarkSeed}
          expiresAt={new Date(grant.expiresAt)}
          onExpired={handleExpired}
        />

        {/* Decorative corner accent */}
        <div className="absolute top-0 right-0 w-32 h-32 bg-primary/20 blur-[60px] pointer-events-none opacity-0 group-hover:opacity-100 transition-opacity" />
      </div>

      {/* Lesson Info / Meta */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="md:col-span-2 bg-slate-900/40 border border-slate-800 p-8 rounded-3xl space-y-4">
          <h2 className="text-xl font-bold text-white flex items-center gap-2">
            <FileText size={20} className="text-primary" />
            About this Lesson
          </h2>
          <p className="text-slate-400 leading-relaxed">
            This session covers critical mathematical models and validation strategies. Ensure you have your workspace ready for the interactive exercises that follow this video.
          </p>
        </div>
        <div className="bg-primary/10 border border-primary/20 p-8 rounded-3xl flex flex-col justify-center items-center text-center space-y-4">
          <p className="text-xs font-bold text-primary uppercase tracking-widest">Next Step</p>
          <h3 className="text-lg font-bold text-white leading-tight">Test your understanding with a short quiz</h3>
          <button className="w-full py-3 bg-primary text-primary-foreground font-black rounded-xl hover:opacity-90 transition-all shadow-lg shadow-primary/20">
            Launch Quiz
          </button>
        </div>
      </div>
    </div>
  )
}

function generateFingerprint(): string {
  const components = [
    navigator.userAgent,
    navigator.language,
    screen.width + 'x' + screen.height,
    new Date().getTimezoneOffset().toString(),
  ]
  return btoa(components.join('|')).slice(0, 32)
}
