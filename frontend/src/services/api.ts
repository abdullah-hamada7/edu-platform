import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'

const api = axios.create({
  baseURL: API_BASE,
})

api.interceptors.request.use((config) => {
  const ensureFingerprint = () => {
    const existing = localStorage.getItem('deviceFingerprint')
    if (existing) return existing
    if (typeof navigator === 'undefined' || typeof screen === 'undefined') return null
    const components = [
      navigator.userAgent,
      navigator.language,
      screen.width + 'x' + screen.height,
      new Date().getTimezoneOffset().toString(),
    ]
    const fingerprint = btoa(components.join('|')).slice(0, 32)
    localStorage.setItem('deviceFingerprint', fingerprint)
    return fingerprint
  }

  const isFormData = typeof FormData !== 'undefined' && config.data instanceof FormData
  if (!isFormData) {
    if (config.headers && 'set' in config.headers) {
      config.headers.set('Content-Type', 'application/json')
    } else {
      config.headers = { ...(config.headers as any), 'Content-Type': 'application/json' }
    }
  }

  const token = localStorage.getItem('accessToken')
  if (token) {
    if (config.headers && 'set' in config.headers) {
      config.headers.set('Authorization', `Bearer ${token}`)
    } else {
      config.headers = { ...(config.headers as any), Authorization: `Bearer ${token}` }
    }
  }
  const fingerprint = ensureFingerprint()
  if (fingerprint) {
    if (config.headers && 'set' in config.headers) {
      config.headers.set('X-Device-Fingerprint', fingerprint)
    } else {
      config.headers = { ...(config.headers as any), 'X-Device-Fingerprint': fingerprint }
    }
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export interface PlaybackGrant {
  manifestUrl: string
  expiresAt: string
  watermarkSeed: string
  courseId: string
}

export const playbackApi = {
  requestGrant: async (lessonId: string, deviceFingerprint: string): Promise<PlaybackGrant> => {
    const response = await api.post(`/student/lessons/${lessonId}/playback-grant`, {
      deviceFingerprint,
    })
    return response.data
  },
}

export interface Course {
  id: string
  title: string
  description?: string
}

export const courseApi = {
  listEnrolled: async (): Promise<Course[]> => {
    const response = await api.get('/student/courses')
    return response.data
  },
}

export interface LoginResponse {
  accessToken: string
  role: string
  mustChangePassword: boolean
}

export const authApi = {
  login: async (email: string, password: string): Promise<LoginResponse> => {
    const response = await api.post('/auth/login', { email, password })
    return response.data
  },

  changePassword: async (currentPassword: string, newPassword: string): Promise<void> => {
    await api.post('/auth/change-password', {
      currentPassword,
      newPassword,
    })
  },
}

export default api
