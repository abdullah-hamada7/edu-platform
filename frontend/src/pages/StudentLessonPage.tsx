import { useParams, useNavigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import SecureHlsPlayer from '../features/player/SecureHlsPlayer'
import { playbackApi, PlaybackGrant } from '../services/api'

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
        setError(err.response?.data?.error || 'Failed to load video')
      })
      .finally(() => setLoading(false))
  }, [lessonId])

  const handleExpired = () => {
    setError('Playback session expired. Please refresh the page.')
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p>Loading video...</p>
      </div>
    )
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

  if (!grant) {
    return <p>No video available</p>
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">Lesson</h1>
      <SecureHlsPlayer
        manifestUrl={grant.manifestUrl}
        watermarkSeed={grant.watermarkSeed}
        expiresAt={new Date(grant.expiresAt)}
        onExpired={handleExpired}
      />
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
