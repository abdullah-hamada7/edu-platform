import { useState, useRef, useEffect } from 'react'
import Hls from 'hls.js'
import WatermarkOverlay from './WatermarkOverlay'

interface SecureHlsPlayerProps {
  manifestUrl: string
  watermarkSeed: string
  expiresAt: Date
  onExpired?: () => void
}

export default function SecureHlsPlayer({ 
  manifestUrl, 
  watermarkSeed, 
  expiresAt,
  onExpired 
}: SecureHlsPlayerProps) {
  const videoRef = useRef<HTMLVideoElement>(null)
  const hlsRef = useRef<Hls | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!videoRef.current) return

    if (Hls.isSupported()) {
      const hls = new Hls({
        maxBufferLength: 30,
        maxMaxBufferLength: 60,
      })
      hlsRef.current = hls

      hls.loadSource(manifestUrl)
      hls.attachMedia(videoRef.current)

      hls.on(Hls.Events.MANIFEST_PARSED, () => {
        videoRef.current?.play().catch(() => {})
      })

      hls.on(Hls.Events.ERROR, (_, data) => {
        if (data.fatal) {
          setError('Failed to load video')
          console.error('HLS error:', data)
        }
      })

      return () => {
        hls.destroy()
      }
    } else if (videoRef.current.canPlayType('application/vnd.apple.mpegurl')) {
      videoRef.current.src = manifestUrl
      videoRef.current.addEventListener('loadedmetadata', () => {
        videoRef.current?.play().catch(() => {})
      })
    }
  }, [manifestUrl])

  useEffect(() => {
    const checkExpiry = () => {
      if (new Date() >= new Date(expiresAt)) {
        onExpired?.()
        setError('Playback session expired')
      }
    }

    const interval = setInterval(checkExpiry, 5000)
    return () => clearInterval(interval)
  }, [expiresAt, onExpired])

  if (error) {
    return (
      <div className="flex items-center justify-center h-96 bg-gray-900 text-white rounded-lg">
        <p>{error}</p>
      </div>
    )
  }

  return (
    <div className="relative w-full aspect-video bg-black rounded-lg overflow-hidden">
      <video
        ref={videoRef}
        className="w-full h-full"
        controls
        playsInline
      />
      <WatermarkOverlay seed={watermarkSeed} />
    </div>
  )
}
