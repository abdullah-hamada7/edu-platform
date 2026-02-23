import { useState, useEffect } from 'react'
import { useNavigate, useLocation, Link, Outlet } from 'react-router-dom'
import {
    Menu,
    X,
    LayoutDashboard,
    BookOpen,
    GraduationCap,
    LogOut,
    User,
    Bell,
    Settings,
    ChevronRight,
} from 'lucide-react'
import { cn } from '../../lib/utils'

interface NavItem {
    label: string
    href: string
    icon: any
    roles: string[]
}

const NAV_ITEMS: NavItem[] = [
    { label: 'Dashboard', href: '/student', icon: LayoutDashboard, roles: ['STUDENT'] },
    { label: 'My Courses', href: '/student/courses', icon: BookOpen, roles: ['STUDENT'] },
    { label: 'My Grades', href: '/student/grades', icon: GraduationCap, roles: ['STUDENT'] },
    { label: 'Admin Dashboard', href: '/admin', icon: LayoutDashboard, roles: ['ADMIN'] },
    { label: 'Courses', href: '/admin/courses', icon: BookOpen, roles: ['ADMIN'] },
    { label: 'Users', href: '/admin/users', icon: User, roles: ['ADMIN'] },
    { label: 'Analytics', href: '/admin/analytics', icon: Bell, roles: ['ADMIN'] },
]

export default function AppLayout() {
    const [isSidebarOpen, setIsSidebarOpen] = useState(true)
    const [userRole, setUserRole] = useState<string | null>(null)
    const navigate = useNavigate()
    const location = useLocation()

    useEffect(() => {
        const role = localStorage.getItem('userRole')
        const token = localStorage.getItem('accessToken')
        setUserRole(role)

        if (!token || !role) {
            navigate('/login', { replace: true })
            return
        }

        if (location.pathname.startsWith('/admin') && role !== 'ADMIN') {
            navigate('/student', { replace: true })
            return
        }

        if (location.pathname.startsWith('/student') && role !== 'STUDENT') {
            navigate('/admin/courses', { replace: true })
            return
        }

        // Close sidebar on mobile when navigating
        if (window.innerWidth < 768) {
            setIsSidebarOpen(false)
        }
    }, [location.pathname, navigate])

    const handleLogout = () => {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('userRole')
        navigate('/login')
    }

    const filteredNavItems = NAV_ITEMS.filter(item => item.roles.includes(userRole || ''))

    return (
        <div className="min-h-screen bg-slate-950 text-slate-100 flex overflow-hidden">
            {/* Sidebar Backdrop (Mobile) */}
            {!isSidebarOpen && window.innerWidth < 768 ? null : (
                <div
                    className={cn(
                        "fixed inset-0 bg-black/60 backdrop-blur-sm z-40 md:hidden transition-opacity",
                        isSidebarOpen ? "opacity-100" : "opacity-0 pointer-events-none"
                    )}
                    onClick={() => setIsSidebarOpen(false)}
                />
            )}

            {/* Sidebar */}
            <aside
                className={cn(
                    "fixed md:relative z-50 h-full bg-slate-900/50 backdrop-blur-xl border-r border-slate-800 transition-all duration-300 ease-in-out flex flex-col",
                    isSidebarOpen ? "w-64 translate-x-0" : "w-0 md:w-20 -translate-x-full md:translate-x-0 overflow-hidden"
                )}
            >
                {/* Sidebar Header */}
                <div className="h-16 flex items-center justify-between px-6 border-b border-slate-800/50">
                    {isSidebarOpen ? (
                        <span className="text-lg font-bold bg-gradient-to-r from-primary to-blue-400 bg-clip-text text-transparent truncate">
                            Secure Math
                        </span>
                    ) : (
                        <div className="w-8 h-8 rounded-lg bg-primary/20 flex items-center justify-center">
                            <LayoutDashboard size={18} className="text-primary" />
                        </div>
                    )}
                    <button
                        onClick={() => setIsSidebarOpen(!isSidebarOpen)}
                        className="text-slate-400 hover:text-white md:hidden"
                    >
                        <X size={20} />
                    </button>
                </div>

                {/* Navigation Links */}
                <nav className="flex-1 py-6 px-3 space-y-1">
                    {filteredNavItems.map((item) => {
                        const isActive = location.pathname === item.href || (item.href !== '/student' && location.pathname.startsWith(item.href))
                        return (
                            <Link
                                key={item.href}
                                to={item.href}
                                className={cn(
                                    "flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all group",
                                    isActive
                                        ? "bg-primary text-primary-foreground shadow-lg shadow-primary/20"
                                        : "text-slate-400 hover:bg-slate-800/50 hover:text-white"
                                )}
                            >
                                <item.icon size={20} className={cn(isActive ? "text-white" : "group-hover:text-primary transition-colors")} />
                                {isSidebarOpen && <span className="font-medium">{item.label}</span>}
                                {isSidebarOpen && isActive && <ChevronRight size={16} className="ml-auto opacity-50" />}
                            </Link>
                        )
                    })}
                </nav>

                {/* Sidebar Footer */}
                <div className="p-4 border-t border-slate-800/50 space-y-1">
                    {isSidebarOpen && (
                        <div className="p-3 mb-2 rounded-xl bg-slate-800/30 border border-slate-800/50">
                            <div className="flex items-center gap-3">
                                <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-xs font-bold ring-2 ring-blue-600/20">
                                    <User size={14} className="text-white" />
                                </div>
                                <div className="flex-1 min-w-0">
                                    <p className="text-xs font-semibold truncate">Admin User</p>
                                    <p className="text-[10px] text-slate-500 truncate lowercase">{userRole}</p>
                                </div>
                            </div>
                        </div>
                    )}
                    <button
                        onClick={handleLogout}
                        className={cn(
                            "w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-slate-400 hover:bg-destructive/10 hover:text-destructive transition-all",
                            !isSidebarOpen && "justify-center"
                        )}
                    >
                        <LogOut size={20} />
                        {isSidebarOpen && <span className="font-medium text-sm">Sign Out</span>}
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <div className="flex-1 flex flex-col min-w-0 h-screen overflow-hidden">
                {/* Header */}
                <header className="h-16 shrink-0 flex items-center justify-between px-6 bg-slate-950/50 backdrop-blur-md border-b border-slate-800/50 z-30">
                    <div className="flex items-center gap-4 text-sm font-medium text-slate-400">
                        <button
                            onClick={() => setIsSidebarOpen(!isSidebarOpen)}
                            className="hidden md:flex text-slate-400 hover:text-white"
                        >
                            <Menu size={20} />
                        </button>
                        <button
                            onClick={() => setIsSidebarOpen(true)}
                            className="md:hidden text-slate-400 hover:text-white"
                        >
                            <Menu size={20} />
                        </button>
                        <span className="hidden sm:inline">/</span>
                        <span className="text-white capitalize">{location.pathname.split('/').pop()?.replace(/-/g, ' ') || 'Dashboard'}</span>
                    </div>

                    <div className="flex items-center gap-2">
                        <button className="p-2 text-slate-400 hover:bg-slate-800 rounded-lg transition-colors relative">
                            <Bell size={18} />
                            <span className="absolute top-2 right-2 w-2 h-2 bg-primary rounded-full ring-2 ring-slate-950" />
                        </button>
                        <button className="p-2 text-slate-400 hover:bg-slate-800 rounded-lg transition-colors">
                            <Settings size={18} />
                        </button>
                    </div>
                </header>

                {/* Scrollable Area */}
                <main className="flex-1 overflow-y-auto p-6 md:p-8">
                    <div className="max-w-7xl mx-auto animate-in fade-in slide-in-from-bottom-4 duration-500">
                        <Outlet />
                    </div>
                </main>
            </div>
        </div>
    )
}
