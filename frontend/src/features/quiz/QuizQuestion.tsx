import { useState } from 'react'
import KatexQuestionRenderer from '../features/quiz/KatexQuestionRenderer'

interface Question {
  id: string
  type: 'MCQ' | 'TRUE_FALSE' | 'NUMERIC'
  promptText: string
  latexEnabled: boolean
  points: number
  position: number
  mcqOptions?: string[]
}

interface QuizQuestionProps {
  question: Question
  answer: string
  onAnswerChange: (questionId: string, value: string) => void
}

export default function QuizQuestion({ question, answer, onAnswerChange }: QuizQuestionProps) {
  const [numericValue, setNumericValue] = useState('')

  const handleNumericChange = (value: string) => {
    setNumericValue(value)
    onAnswerChange(question.id, value)
  }

  return (
    <div className="p-6 bg-white rounded-lg shadow mb-4">
      <div className="flex justify-between items-start mb-4">
        <span className="text-sm text-gray-500">Question {question.position + 1}</span>
        <span className="text-sm font-medium text-blue-600">{question.points} pts</span>
      </div>

      <KatexQuestionRenderer 
        text={question.promptText} 
        latexEnabled={question.latexEnabled}
        className="text-lg mb-4"
      />

      {question.type === 'MCQ' && question.mcqOptions && (
        <div className="space-y-2">
          {question.mcqOptions.map((option, index) => (
            <label 
              key={index}
              className={`flex items-center p-3 border rounded-lg cursor-pointer transition-colors ${
                answer === String(index) 
                  ? 'border-blue-500 bg-blue-50' 
                  : 'hover:bg-gray-50'
              }`}
            >
              <input
                type="radio"
                name={question.id}
                value={index}
                checked={answer === String(index)}
                onChange={() => onAnswerChange(question.id, String(index))}
                className="mr-3"
              />
              <KatexQuestionRenderer 
                text={option} 
                latexEnabled={question.latexEnabled}
              />
            </label>
          ))}
        </div>
      )}

      {question.type === 'TRUE_FALSE' && (
        <div className="flex gap-4">
          <label 
            className={`flex-1 flex items-center justify-center p-4 border rounded-lg cursor-pointer transition-colors ${
              answer === 'true' 
                ? 'border-blue-500 bg-blue-50' 
                : 'hover:bg-gray-50'
            }`}
          >
            <input
              type="radio"
              name={question.id}
              value="true"
              checked={answer === 'true'}
              onChange={() => onAnswerChange(question.id, 'true')}
              className="mr-2"
            />
            True
          </label>
          <label 
            className={`flex-1 flex items-center justify-center p-4 border rounded-lg cursor-pointer transition-colors ${
              answer === 'false' 
                ? 'border-blue-500 bg-blue-50' 
                : 'hover:bg-gray-50'
            }`}
          >
            <input
              type="radio"
              name={question.id}
              value="false"
              checked={answer === 'false'}
              onChange={() => onAnswerChange(question.id, 'false')}
              className="mr-2"
            />
            False
          </label>
        </div>
      )}

      {question.type === 'NUMERIC' && (
        <div className="mt-4">
          <input
            type="number"
            step="any"
            value={numericValue}
            onChange={(e) => handleNumericChange(e.target.value)}
            placeholder="Enter your answer"
            className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
      )}
    </div>
  )
}
