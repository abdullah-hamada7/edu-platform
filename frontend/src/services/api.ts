import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  const fingerprint = localStorage.getItem('deviceFingerprint')
  if (fingerprint) {
    config.headers['X-Device-Fingerprint'] = fingerprint
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
