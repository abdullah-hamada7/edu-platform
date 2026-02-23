import {
  useEffect,
  useState
} from 'react'
import {
  Users,
  BookOpen,
  BarChart3,
  ShieldCheck,
  Layout,
  Plus,
  ChevronRight,
  TrendingUp,
  Activity,
  History,
  Loader2
} from 'lucide-react'
import { Link } from 'react-router-dom'
import api from '../../services/api'
import { cn } from '../../lib/utils'

interface SummaryData {
  totalStudents: number
  activeCourses: number
  totalExaminations: number
  systemStatus: string
  averagePerformance: number
  storageUtilization: number
}

export default function AdminDashboardPage() {
  const [data, setData] = useState<SummaryData | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get<SummaryData>('/admin/analytics/summary')
      .then(res => setData(res.data))
      .catch(err => console.error('Failed to fetch admin summary:', err))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] gap-4 text-center">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-slate-500 font-medium animate-pulse uppercase tracking-widest text-[10px]">Aggregating Intelligence...</p>
      </div>
    )
  }

  const stats = [
    { label: 'Total Students', value: (data?.totalStudents || 0).toLocaleString(), icon: Users, color: 'text-blue-500', bg: 'bg-blue-500/10' },
    { label: 'Active Courses', value: (data?.activeCourses || 0).toString(), icon: BookOpen, color: 'text-emerald-500', bg: 'bg-emerald-500/10' },
    { label: 'Examinations', value: (data?.totalExaminations || 0).toString(), icon: BarChart3, color: 'text-amber-500', bg: 'bg-amber-400/10' },
    { label: 'System Status', value: data?.systemStatus || 'Unknown', icon: ShieldCheck, color: 'text-indigo-500', bg: 'bg-indigo-500/10' },
  ]

  const managementCards = [
    {
      title: 'Course Management',
      desc: 'Architect curricula, manage multimedia lessons, and configure evaluation criteria.',
      path: '/admin/courses',
      icon: Layout,
      count: `${data?.activeCourses || 0} Published`
    },
    {
      title: 'User Access Control',
      desc: 'Provision student accounts, manage enrollments, and audit security logs.',
      path: '/admin/users',
      icon: ShieldCheck,
      count: `${data?.totalStudents || 0} Enrolled`
    },
    {
      title: 'Analytics Engine',
      desc: 'Real-time performance tracking, grade distributions, and platform engagement.',
      path: '/admin/analytics',
      icon: BarChart3,
      count: 'View Reports'
    }
  ]

  return (
    <div className="space-y-8 pb-20">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-3xl font-extrabold text-white tracking-tight">Administrative Command</h1>
          <p className="text-slate-400">System-wide overview and infrastructure management.</p>
        </div>
        <div className="flex gap-3">
          <button className="px-4 py-2 bg-slate-800 text-white rounded-xl text-sm font-bold border border-slate-700 hover:bg-slate-700 transition-all flex items-center gap-2">
            <History size={16} /> Audit Logs
          </button>
          <button className="px-4 py-2 bg-primary text-primary-foreground rounded-xl text-sm font-black transition-all flex items-center gap-2 shadow-lg shadow-primary/20">
            <Plus size={16} /> New Resource
          </button>
        </div>
      </div>

      {/* Metrics Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((stat, idx) => (
          <div key={idx} className="bg-slate-900/40 border border-slate-800 p-5 rounded-2xl flex flex-col gap-4">
            <div className="flex items-center justify-between">
              <div className={cn("p-2.5 rounded-xl border", stat.bg, stat.color, "border-current/10")}>
                <stat.icon size={20} />
              </div>
              <div className="text-[10px] font-bold text-slate-500 uppercase tracking-widest flex items-center gap-1">
                <TrendingUp size={10} className="text-emerald-500" /> Adaptive Sync
              </div>
            </div>
            <div>
              <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">{stat.label}</p>
              <p className="text-2xl font-black text-white">{stat.value}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
        {/* Management Cards */}
        <div className="lg:col-span-2 space-y-4">
          <h2 className="text-lg font-bold flex items-center gap-2 text-white">
            <Activity size={18} className="text-primary" />
            Management Modules
          </h2>
          <div className="space-y-4">
            {managementCards.map((card, idx) => (
              <Link
                key={idx}
                to={card.path}
                className="group block bg-slate-900/40 border border-slate-800 p-6 rounded-3xl hover:border-primary/50 transition-all relative overflow-hidden"
              >
                <div className="absolute top-0 right-0 p-8 opacity-5 group-hover:opacity-10 transition-opacity">
                  <card.icon size={80} className="text-white" />
                </div>
                <div className="relative flex items-center justify-between">
                  <div className="space-y-4 max-w-md">
                    <div className="space-y-1">
                      <h3 className="text-xl font-black text-white group-hover:text-primary transition-colors">{card.title}</h3>
                      <p className="text-sm text-slate-400 leading-relaxed">{card.desc}</p>
                    </div>
                    <span className="inline-block px-3 py-1 rounded-full bg-slate-800 text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                      {card.count}
                    </span>
                  </div>
                  <div className="w-12 h-12 rounded-2xl bg-slate-800 flex items-center justify-center text-slate-500 group-hover:bg-primary group-hover:text-primary-foreground transition-all">
                    <ChevronRight size={24} />
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>

        {/* Sidebar / Maintenance */}
        <div className="bg-slate-900/40 border border-slate-800 rounded-[2.5rem] p-8 space-y-8">
          <div className="space-y-1 text-center">
            <h3 className="text-lg font-bold text-white">System Integrity</h3>
            <p className="text-xs text-slate-500">Security and maintenance tasks.</p>
          </div>

          <div className="space-y-4">
            <div className="p-4 rounded-2xl bg-slate-950/50 border border-slate-800 flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-300">Platform Performance</span>
              <span className="px-2 py-1 rounded-md bg-emerald-500/10 text-[10px] font-black text-emerald-500 uppercase">{Math.round(data?.averagePerformance || 0)}%</span>
            </div>
            <div className="p-4 rounded-2xl bg-slate-950/50 border border-slate-800 flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-300">Database Optimizer</span>
              <button className="text-[10px] font-black text-primary hover:underline uppercase tracking-widest">Run Now</button>
            </div>
            <div className="p-4 rounded-2xl bg-rose-500/5 border border-rose-500/20 flex items-center justify-between">
              <span className="text-xs font-semibold text-rose-200">2 Restricted Actions</span>
              <span className="px-2 py-1 rounded-md bg-rose-500/20 text-[10px] font-black text-rose-500 uppercase tracking-widest border border-rose-500/20">Critical</span>
            </div>
          </div>

          <div className="pt-4">
            <p className="text-[10px] font-bold text-slate-600 uppercase tracking-widest mb-4">Storage Utilization</p>
            <div className="space-y-2">
              <div className="flex justify-between text-[10px] font-bold text-slate-400">
                <span>Media Library</span>
                <span>{data?.storageUtilization || 0}%</span>
              </div>
              <div className="h-1.5 w-full bg-slate-800 rounded-full overflow-hidden">
                <div
                  className="h-full bg-primary rounded-full transition-all duration-1000"
                  style={{ width: `${data?.storageUtilization || 0}%` }}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
