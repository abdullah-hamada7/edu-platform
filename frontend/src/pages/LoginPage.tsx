import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../services/api'
import { LogIn, Lock, Mail, AlertCircle, Loader2, CheckCircle2 } from 'lucide-react'
import { cn } from '../lib/utils'

const InputField = ({
  label,
  icon: Icon,
  ...props
}: {
  label: string,
  icon: any
} & React.InputHTMLAttributes<HTMLInputElement>) => (
  <div className="space-y-1.5">
    <label className="text-sm font-medium text-slate-200 ml-1">{label}</label>
    <div className="relative group">
      <div className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-primary transition-colors">
        <Icon size={18} />
      </div>
      <input
        {...props}
        className={cn(
          "w-full bg-slate-900/50 border border-slate-700/50 rounded-xl py-2.5 pl-10 pr-4",
          "text-slate-100 placeholder:text-slate-500",
          "focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all",
          "disabled:opacity-50 disabled:cursor-not-allowed",
          props.className
        )}
      />
    </div>
  </div>
)

const AuthButton = ({ children, loading, ...props }: any) => (
  <button
    {...props}
    disabled={loading || props.disabled}
    className={cn(
      "w-full bg-primary hover:bg-primary/90 text-primary-foreground font-semibold py-3 rounded-xl",
      "flex items-center justify-center gap-2 transition-all active:scale-[0.98]",
      "disabled:opacity-50 disabled:active:scale-100 disabled:cursor-not-allowed shadow-lg shadow-primary/20",
      props.className
    )}
  >
    {loading ? <Loader2 className="animate-spin" size={20} /> : children}
  </button>
)

export default function LoginPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [mustChangePassword, setMustChangePassword] = useState(false)
  const [newPassword, setNewPassword] = useState('')
  const [loading, setLoading] = useState(false)

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const response = await authApi.login(email, password)
      localStorage.setItem('accessToken', response.accessToken)
      localStorage.setItem('userRole', response.role)

      if (response.mustChangePassword) {
        setMustChangePassword(true)
      } else if (response.role === 'ADMIN') {
        navigate('/admin/courses')
      } else {
        navigate('/student/courses')
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid email or password. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      await authApi.changePassword(password, newPassword)
      setMustChangePassword(false)

      const role = localStorage.getItem('userRole')
      if (role === 'ADMIN') {
        navigate('/admin/courses')
      } else {
        navigate('/student/courses')
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to change password. Requirements: 8+ chars.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-slate-950 relative overflow-hidden">
      {/* Background Orbs */}
      <div className="absolute top-[-10%] right-[-10%] w-[40%] h-[40%] bg-primary/10 rounded-full blur-[120px]" />
      <div className="absolute bottom-[-10%] left-[-10%] w-[40%] h-[40%] bg-blue-600/10 rounded-full blur-[120px]" />

      <div className="w-full max-w-md px-6 relative z-10 animate-in fade-in zoom-in duration-500">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary/10 text-primary mb-4 border border-primary/20">
            <LogIn size={32} />
          </div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">Secure Math</h1>
          <p className="text-slate-400 mt-2">Elevate your learning experience</p>
        </div>

        <div className="bg-slate-900/40 backdrop-blur-xl border border-slate-800 p-8 rounded-3xl shadow-2xl">
          {error && (
            <div className="mb-6 flex items-start gap-3 p-3.5 rounded-xl bg-destructive/10 border border-destructive/20 text-destructive text-sm animate-in slide-in-from-top-2">
              <AlertCircle className="shrink-0 mt-0.5" size={18} />
              <p className="font-medium leading-tight">{error}</p>
            </div>
          )}

          {mustChangePassword ? (
            <div className="space-y-6">
              <div className="space-y-1 text-center">
                <div className="inline-flex items-center gap-2 text-primary font-medium text-sm px-3 py-1 rounded-full bg-primary/10 border border-primary/20 mb-2">
                  <Lock size={14} /> Security Update
                </div>
                <h2 className="text-xl font-bold text-white text-center">New Password Required</h2>
              </div>

              <form onSubmit={handlePasswordChange} className="space-y-5">
                <InputField
                  label="Temporary Password"
                  icon={Lock}
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your current password"
                  required
                />
                <InputField
                  label="New Secure Password"
                  icon={CheckCircle2}
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="At least 8 characters"
                  required
                  minLength={8}
                />
                <AuthButton loading={loading}>
                  Update Password & Login
                </AuthButton>
              </form>
            </div>
          ) : (
            <form onSubmit={handleLogin} className="space-y-5">
              <InputField
                label="Email Address"
                icon={Mail}
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@example.com"
                required
              />
              <InputField
                label="Password"
                icon={Lock}
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
              />

              <div className="flex items-center justify-between text-xs px-1">
                <label className="flex items-center gap-2 text-slate-400 cursor-pointer">
                  <input type="checkbox" className="rounded border-slate-700 bg-slate-800 text-primary focus:ring-primary/20" />
                  Remember me
                </label>
                <a href="#" className="text-primary hover:underline font-medium">Forgot password?</a>
              </div>

              <AuthButton loading={loading}>
                Sign in to your account
              </AuthButton>
            </form>
          )}
        </div>

        <p className="text-center mt-8 text-slate-500 text-sm">
          Protected by end-to-end encryption & hardware signing
        </p>
      </div>
    </div>
  )
}
