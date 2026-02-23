import { useEffect, useState } from 'react'
import api from '../../services/api'
import {
    UserPlus,
    Search,
    MoreVertical,
    Shield,
    Mail,
    ChevronRight,
    Filter,
    UserCheck,
    Ban
} from 'lucide-react'
import { cn } from '../../lib/utils'

interface User {
    id: string
    email: string
    role: string
    status: string
    mustChangePassword?: boolean
}

export default function AdminUsersPage() {
    const [users, setUsers] = useState<User[]>([])
    const [loading, setLoading] = useState(true)
    const [search, setSearch] = useState('')
    const [showCreate, setShowCreate] = useState(false)
    const [email, setEmail] = useState('')
    const [tempPassword, setTempPassword] = useState('')
    const [role, setRole] = useState('STUDENT')
    const [saving, setSaving] = useState(false)

    useEffect(() => {
        api.get<User[]>('/admin/users')
            .then(res => setUsers(res.data))
            .catch(err => console.error('Failed to fetch admin users:', err))
            .finally(() => setLoading(false))
    }, [])

    const refreshUsers = () => {
        api.get<User[]>('/admin/users')
            .then(res => setUsers(res.data))
            .catch(err => console.error('Failed to fetch admin users:', err))
    }

    const handleCreateUser = async (event: React.FormEvent) => {
        event.preventDefault()
        if (!email || !tempPassword) return

        try {
            setSaving(true)
            await api.post('/admin/users', {
                email,
                temporaryPassword: tempPassword,
                role,
            })
            setEmail('')
            setTempPassword('')
            setRole('STUDENT')
            setShowCreate(false)
            refreshUsers()
        } catch (err) {
            console.error('Failed to create user:', err)
        } finally {
            setSaving(false)
        }
    }

    const toggleStatus = async (user: User) => {
        const nextStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
        try {
            await api.patch(`/admin/users/${user.id}/status`, { status: nextStatus })
            refreshUsers()
        } catch (err) {
            console.error('Failed to update status:', err)
        }
    }

    const filteredUsers = users.filter(u =>
        u.email.toLowerCase().includes(search.toLowerCase())
    )

    if (loading) {
        return (
            <div className="space-y-6">
                <div className="h-8 w-48 bg-slate-800 animate-pulse rounded-lg" />
                <div className="space-y-4">
                    {[1, 2, 3].map(i => (
                        <div key={i} className="h-20 bg-slate-900/40 border border-slate-800 rounded-2xl animate-pulse" />
                    ))}
                </div>
            </div>
        )
    }

    return (
        <div className="space-y-8 pb-20">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div className="space-y-1">
                    <h1 className="text-3xl font-extrabold text-white tracking-tight">User Access Control</h1>
                    <p className="text-slate-400">Manage student identities and administrative permissions.</p>
                </div>
                <button
                    onClick={() => setShowCreate((prev) => !prev)}
                    className="px-5 py-2.5 bg-primary text-primary-foreground rounded-xl text-sm font-black transition-all flex items-center gap-2 shadow-lg shadow-primary/20 hover:scale-[1.02] active:scale-[0.98]"
                >
                    <UserPlus size={18} /> Provision New User
                </button>
            </div>

            {showCreate && (
                <form
                    onSubmit={handleCreateUser}
                    className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl space-y-4"
                >
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div className="space-y-2">
                            <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">Email</label>
                            <input
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                type="email"
                                className="w-full bg-slate-950 border border-slate-800 rounded-xl py-2.5 px-4 text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-primary/50"
                                placeholder="student@securemath.local"
                                required
                            />
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">Temporary Password</label>
                            <input
                                value={tempPassword}
                                onChange={(e) => setTempPassword(e.target.value)}
                                type="password"
                                minLength={8}
                                className="w-full bg-slate-950 border border-slate-800 rounded-xl py-2.5 px-4 text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-primary/50"
                                placeholder="••••••••"
                                required
                            />
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">Role</label>
                            <select
                                value={role}
                                onChange={(e) => setRole(e.target.value)}
                                className="w-full bg-slate-950 border border-slate-800 rounded-xl py-2.5 px-4 text-sm text-slate-200 focus:outline-none focus:ring-2 focus:ring-primary/50"
                            >
                                <option value="STUDENT">Student</option>
                                <option value="ADMIN">Admin</option>
                            </select>
                        </div>
                    </div>
                    <div className="flex justify-end">
                        <button
                            type="submit"
                            disabled={saving}
                            className="px-6 py-2.5 bg-primary text-primary-foreground rounded-xl text-xs font-black uppercase tracking-widest hover:opacity-90 disabled:opacity-60"
                        >
                            {saving ? 'Creating...' : 'Create User'}
                        </button>
                    </div>
                </form>
            )}

            {/* Control Bar */}
            <div className="flex flex-col sm:flex-row gap-4 items-center justify-between bg-slate-900/40 border border-slate-800 p-4 rounded-2xl">
                <div className="relative w-full sm:max-w-xs">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" size={16} />
                    <input
                        type="text"
                        placeholder="Search by email..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="w-full bg-slate-950 border border-slate-800 rounded-xl py-2 pl-10 pr-4 text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all font-medium"
                    />
                </div>
                <div className="flex items-center gap-2">
                    <button className="p-2 rounded-lg bg-slate-800 text-slate-400 hover:text-white transition-colors border border-slate-700">
                        <Filter size={18} />
                    </button>
                    <button className="text-xs font-bold text-slate-400 hover:text-white px-3 flex items-center gap-2">
                        All Status <ChevronRight size={14} className="rotate-90" />
                    </button>
                </div>
            </div>

            {/* Users Table */}
            <div className="bg-slate-900/40 border border-slate-800 rounded-3xl overflow-hidden shadow-2xl">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="border-b border-white/5 bg-slate-800/20">
                            <th className="px-6 py-4 text-[10px] font-black text-slate-500 uppercase tracking-widest">Subscriber Identity</th>
                            <th className="px-6 py-4 text-[10px] font-black text-slate-500 uppercase tracking-widest hidden sm:table-cell">Role</th>
                            <th className="px-6 py-4 text-[10px] font-black text-slate-500 uppercase tracking-widest hidden lg:table-cell">Account Status</th>
                            <th className="px-6 py-4 text-[10px] font-black text-slate-500 uppercase tracking-widest text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-white/5">
                        {filteredUsers.length === 0 ? (
                            <tr>
                                <td colSpan={4} className="px-6 py-20 text-center text-slate-500 italic">
                                    No matching subscribers identified.
                                </td>
                            </tr>
                        ) : (
                            filteredUsers.map((user) => (
                                <tr key={user.id} className="group hover:bg-white/[0.02] transition-colors cursor-pointer">
                                    <td className="px-6 py-5">
                                        <div className="flex items-center gap-4">
                                            <div className="w-10 h-10 rounded-full bg-slate-800/80 text-primary flex items-center justify-center border border-slate-700 group-hover:ring-2 group-hover:ring-primary/20 transition-all font-bold">
                                                {user.email.substring(0, 2).toUpperCase()}
                                            </div>
                                            <div className="min-w-0">
                                                <p className="text-sm font-bold text-white group-hover:text-primary transition-colors truncate">{user.email}</p>
                                                <div className="flex items-center gap-1.5 text-[10px] text-slate-500 font-bold uppercase tracking-wider">
                                                    <Mail size={10} /> {user.id.slice(0, 8)}
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                    <td className="px-6 py-5 hidden sm:table-cell">
                                        <span className={cn(
                                            "inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md text-[10px] font-black uppercase tracking-widest border",
                                            user.role === 'ADMIN' ? "bg-indigo-500/10 text-indigo-400 border-indigo-500/20" : "bg-blue-500/10 text-blue-400 border-blue-500/20"
                                        )}>
                                            <Shield size={10} /> {user.role}
                                        </span>
                                    </td>
                                    <td className="px-6 py-5 hidden lg:table-cell">
                                        <div className="flex items-center gap-2">
                                            <div className={cn("w-1.5 h-1.5 rounded-full", user.status === 'ACTIVE' ? "bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.5)]" : "bg-rose-500")} />
                                            <span className="text-xs font-bold text-slate-300">{user.status.charAt(0) + user.status.slice(1).toLowerCase()}</span>
                                        </div>
                                    </td>
                                    <td className="px-6 py-5 text-right">
                                        <div className="flex items-center justify-end gap-2">
                                            <button className="p-2 rounded-lg bg-slate-800/50 text-slate-500 hover:text-emerald-400 hover:bg-emerald-500/10 transition-all" title="Enroll in Course">
                                                <UserCheck size={18} />
                                            </button>
                                            <button
                                                onClick={() => toggleStatus(user)}
                                                className="p-2 rounded-lg bg-slate-800/50 text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 transition-all"
                                                title="Restrict Access"
                                            >
                                                <Ban size={18} />
                                            </button>
                                            <button className="p-2 rounded-lg bg-slate-800/50 text-slate-500 hover:text-white hover:bg-slate-700 transition-all">
                                                <MoreVertical size={18} />
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            <div className="flex items-center justify-between px-4">
                <p className="text-[10px] font-bold text-slate-500 uppercase tracking-widest">System Registry: {filteredUsers.length} Users</p>
                <div className="flex items-center gap-2">
                    <span className="text-[10px] font-bold text-slate-600">Page 1 of 1</span>
                </div>
            </div>
        </div>
    )
}
