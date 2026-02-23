import { useEffect, useRef } from 'react'
import katex from 'katex'

interface KatexQuestionRendererProps {
  text: string
  latexEnabled: boolean
  className?: string
}

export default function KatexQuestionRenderer({ 
  text, 
  latexEnabled, 
  className = '' 
}: KatexQuestionRendererProps) {
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!containerRef.current) return

    if (latexEnabled) {
      const latexBlocks = text.split(/(\$[^$]+\$)/g)
      
      containerRef.current.innerHTML = latexBlocks
        .map(block => {
          if (block.startsWith('$') && block.endsWith('$')) {
            const latex = block.slice(1, -1)
            try {
              return katex.renderToString(latex, {
                throwOnError: false,
                displayMode: false,
              })
            } catch (e) {
              console.error('KaTeX error:', e)
              return block
            }
          }
          return block
        })
        .join('')
    } else {
      containerRef.current.textContent = text
    }
  }, [text, latexEnabled])

  return (
    <div 
      ref={containerRef} 
      className={`question-text ${className}`}
    />
  )
}
