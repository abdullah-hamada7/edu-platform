import { useEffect, useState } from 'react'

interface WatermarkOverlayProps {
  seed: string
  userId?: string
}

interface Position {
  x: number
  y: number
}

export default function WatermarkOverlay({ seed, userId }: WatermarkOverlayProps) {
  const [position, setPosition] = useState<Position>({ x: 10, y: 10 })
  const [opacity, setOpacity] = useState(0.3)

  useEffect(() => {
    const calculatePosition = () => {
      const timestamp = Date.now()
      const period = 30000
      const offset = timestamp % period
      const quadrant = Math.floor((timestamp / 15000) % 4)

      const positions: Position[] = [
        { x: 10, y: 10 },
        { x: 80, y: 10 },
        { x: 10, y: 80 },
        { x: 80, y: 80 },
      ]

      setPosition(positions[quadrant])
    }

    calculatePosition()
    const interval = setInterval(calculatePosition, 15000)

    return () => clearInterval(interval)
  }, [seed])

  useEffect(() => {
    const interval = setInterval(() => {
      setOpacity(0.2 + Math.random() * 0.2)
    }, 5000)

    return () => clearInterval(interval)
  }, [])

  return (
    <div
      className="absolute pointer-events-none select-none text-white text-xs font-mono z-50"
      style={{
        left: `${position.x}%`,
        top: `${position.y}%`,
        opacity,
        transform: 'translate(-50%, -50%)',
        textShadow: '1px 1px 2px rgba(0,0,0,0.8)',
      }}
    >
      {userId && <span className="block">{userId}</span>}
      <span className="block">{seed}</span>
    </div>
  )
}
