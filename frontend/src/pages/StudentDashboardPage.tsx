import {
  useEffect,
  useState
} from 'react'
import {
  BookOpen,
  GraduationCap,
  Award,
  Clock,
  ChevronRight,
  TrendingUp,
  Zap,
  Activity,
  ArrowUpRight,
  Loader2
} from 'lucide-react'
import { Link } from 'react-router-dom'
import api from '../services/api'
import { cn } from '../lib/utils'

interface SummaryData {
  activeCoursesCount: number
  averageScore: number
  platformRank: string
  recentActivity: Array<{
    quizId: string
    quizTitle: string
    score: number
    maxScore: number
    submittedAt: string
  }>
  weeklyProgressPercentage: number
}

export default function StudentDashboardPage() {
  const [data, setData] = useState<SummaryData | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get<SummaryData>('/student/grades/summary')
      .then(res => setData(res.data))
      .catch(err => console.error('Failed to fetch summary:', err))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-slate-500 font-medium animate-pulse uppercase tracking-widest text-[10px]">Synchronizing Intel...</p>
      </div>
    )
  }

  const stats = [
    { label: 'Active Courses', value: data?.activeCoursesCount || '0', icon: BookOpen, color: 'text-blue-400', bg: 'bg-blue-400/10' },
    { label: 'Avg. Score', value: `${Math.round(data?.averageScore || 0)}%`, icon: TrendingUp, color: 'text-emerald-400', bg: 'bg-emerald-400/10' },
    { label: 'Platform Rank', value: data?.platformRank || 'N/A', icon: Award, color: 'text-amber-400', bg: 'bg-amber-400/10' },
  ]

  const actions = [
    {
      title: 'Current Focus',
      desc: 'Continue your deep dive into mathematical disciplines.',
      href: '/student/courses',
      icon: zapIcon,
      footer: 'Session Ready',
      promo: 'Current Path'
    },
    {
      title: 'Academic Catalog',
      desc: 'Explore the full spectrum of mathematical disciplines and research modules.',
      href: '/student/courses',
      icon: BookOpen,
      footer: 'Learning path sync active',
      promo: 'Discovery'
    },
    {
      title: 'Performance Registry',
      desc: 'Review historical evaluations, grade trends, and academic milestones.',
      href: '/student/grades',
      icon: GraduationCap,
      footer: 'Evaluation Data Synced',
      promo: 'Intelligence'
    },
  ]

  return (
    <div className="space-y-12 pb-20 max-w-6xl mx-auto">
      {/* Immersive Welcome */}
      <div className="relative overflow-hidden bg-slate-900/60 border border-slate-800 p-10 rounded-[3rem] shadow-2xl">
        <div className="absolute top-0 right-0 p-8 opacity-5">
          <Activity size={200} className="text-white" />
        </div>
        <div className="relative space-y-4 max-w-2xl">
          <p className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary/10 border border-primary/20 text-[10px] font-black text-primary uppercase tracking-widest">
            Learning Intelligence Active
          </p>
          <h1 className="text-4xl md:text-5xl font-black text-white leading-tight tracking-tighter">
            Welcome back, <span className="text-primary italic">Scholar.</span>
          </h1>
          <p className="text-slate-400 text-lg leading-relaxed">
            You are currently at <span className="text-white font-bold">{data?.weeklyProgressPercentage}%</span> of your weekly cognitive goal. Complete one more module to reach peak performance.
          </p>
          <div className="flex items-center gap-6 pt-4">
            <div className="space-y-1.5">
              <div className="flex justify-between text-[10px] font-black uppercase tracking-widest text-slate-500">
                <span>Weekly Progress</span>
                <span>{data?.weeklyProgressPercentage}%</span>
              </div>
              <div className="h-2 w-48 bg-slate-800 rounded-full overflow-hidden">
                <div
                  className="h-full bg-primary rounded-full transition-all duration-1000"
                  style={{ width: `${data?.weeklyProgressPercentage}%` }}
                />
              </div>
            </div>
            <Link to="/student/courses" className="flex items-center gap-2 text-xs font-black text-white hover:text-primary transition-colors group">
              Optimized Recommendations <ArrowUpRight size={14} className="group-hover:-translate-y-0.5 group-hover:translate-x-0.5 transition-transform" />
            </Link>
          </div>
        </div>
      </div>

      {/* Metrics Section */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
        {stats.map((stat) => (
          <div key={stat.label} className="bg-slate-900/40 border border-slate-800 p-8 rounded-[2rem] flex flex-col gap-6 hover:border-slate-700 transition-all group">
            <div className={cn("w-12 h-12 rounded-2xl flex items-center justify-center border transition-all group-hover:scale-110", stat.bg, stat.color, "border-current/10")}>
              <stat.icon size={24} />
            </div>
            <div>
              <p className="text-[10px] font-black text-slate-500 uppercase tracking-widest mb-1">{stat.label}</p>
              <p className="text-3xl font-black text-white">{stat.value}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Action Hub */}
      <div className="space-y-6">
        <h2 className="text-xl font-black text-white flex items-center gap-3">
          <Activity size={20} className="text-primary" />
          Learning Control Hub
        </h2>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {actions.map((action) => (
            <Link
              key={action.title}
              to={action.href}
              className="group relative flex flex-col bg-slate-950/50 border border-slate-800 rounded-[2.5rem] p-8 hover:border-primary/50 transition-all hover:bg-slate-900/40"
            >
              <div className="mb-6 flex items-center justify-between">
                <div className="inline-flex items-center justify-center w-12 h-12 rounded-2xl bg-slate-900 text-primary border border-white/5 group-hover:scale-110 transition-transform">
                  <action.icon size={22} />
                </div>
                <span className="text-[9px] font-black text-slate-600 uppercase tracking-widest px-2.5 py-1 rounded-md bg-white/5 group-hover:text-primary transition-colors">
                  {action.promo}
                </span>
              </div>

              <div className="flex-1 space-y-3">
                <h3 className="text-xl font-black text-white group-hover:text-primary transition-colors flex items-center gap-2">
                  {action.title}
                  <ChevronRight size={18} className="opacity-0 -translate-x-2 group-hover:opacity-100 group-hover:translate-x-0 transition-all" />
                </h3>
                <p className="text-slate-400 text-sm leading-relaxed">{action.desc}</p>
              </div>

              <div className="mt-10 pt-6 border-t border-white/5 flex items-center justify-between">
                <span className="text-[10px] font-bold text-slate-600 uppercase tracking-tight flex items-center gap-2">
                  <div className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse shadow-[0_0_8px_rgba(16,185,129,0.5)]" />
                  {action.footer}
                </span>
                <div className="w-8 h-8 rounded-full bg-slate-900 flex items-center justify-center text-slate-500 group-hover:bg-primary group-hover:text-primary-foreground transition-all">
                  <ArrowUpRight size={16} />
                </div>
              </div>
            </Link>
          ))}
        </div>
      </div>

      {/* Bottom Insights */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div className="bg-slate-900/40 border border-slate-800 p-8 rounded-[2.5rem] space-y-6">
          <h4 className="text-sm font-black text-white uppercase tracking-widest flex items-center gap-2">
            <Clock size={16} className="text-primary" /> Recent Activity
          </h4>
          <div className="space-y-4">
            {data?.recentActivity && data.recentActivity.length > 0 ? data.recentActivity.map(activity => (
              <div key={activity.quizId} className="flex items-center gap-4 p-4 rounded-2xl bg-slate-950/30 border border-white/5">
                <div className="w-2 h-2 rounded-full bg-primary" />
                <div className="flex-1 min-w-0">
                  <p className="text-xs font-bold text-white truncate">{activity.quizTitle}</p>
                  <p className="text-[10px] text-slate-500 uppercase font-bold tracking-tight">
                    Grade: {Math.round((activity.score / activity.maxScore) * 100)}% • {new Date(activity.submittedAt).toLocaleDateString()}
                  </p>
                </div>
              </div>
            )) : (
              <p className="text-slate-500 text-xs font-bold uppercase tracking-widest py-4 text-center">No Activity Recorded</p>
            )}
          </div>
        </div>
        <div className="bg-primary/5 border border-primary/20 p-8 rounded-[2.5rem] flex flex-col justify-center items-center text-center space-y-4 relative overflow-hidden group">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_50%_120%,rgba(59,130,246,0.1),transparent)]" />
          <Award size={48} className="text-primary mb-2 group-hover:scale-110 transition-transform duration-500" />
          <h4 className="text-xl font-black text-white relative">Academic Excellence Award</h4>
          <p className="text-slate-400 text-sm max-w-xs relative">High-performance metrics detected. Keep up the cognitive trajectory for platform recognition.</p>
          <Link to="/student/grades" className="px-6 py-2 bg-primary text-primary-foreground rounded-xl text-[10px] font-black uppercase tracking-widest shadow-xl shadow-primary/20 relative">
            Registry Profile
          </Link>
        </div>
      </div>
    </div>
  )
}

function zapIcon({ size }: { size?: number }) {
  return <Zap size={size} />
}
