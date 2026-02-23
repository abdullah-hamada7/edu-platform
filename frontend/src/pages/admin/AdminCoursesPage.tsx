import { useEffect, useState } from 'react'
import api from '../../services/api'
import {
    Plus,
    Search,
    MoreVertical,
    BookOpen,
    Layers,
    ChevronRight,
    Filter
} from 'lucide-react'
import { Link } from 'react-router-dom'

interface Course {
    id: string
    title: string
    description?: string
    chaptersCount?: number
    status?: string
}

export default function AdminCoursesPage() {
    const [courses, setCourses] = useState<Course[]>([])
    const [loading, setLoading] = useState(true)
    const [search, setSearch] = useState('')
    const [showCreate, setShowCreate] = useState(false)
    const [title, setTitle] = useState('')
    const [description, setDescription] = useState('')
    const [creating, setCreating] = useState(false)
    const [statusFilter, setStatusFilter] = useState('ALL')

    useEffect(() => {
        api.get<Course[]>('/admin/courses')
            .then(res => setCourses(res.data))
            .catch(err => console.error('Failed to fetch admin courses:', err))
            .finally(() => setLoading(false))
    }, [])

    const handleCreateCourse = async (event: React.FormEvent) => {
        event.preventDefault()
        if (!title.trim()) return

        try {
            setCreating(true)
            const response = await api.post<Course>('/admin/courses', { title, description })
            setCourses(prev => [response.data, ...prev])
            setTitle('')
            setDescription('')
            setShowCreate(false)
        } catch (err) {
            console.error('Failed to create course:', err)
        } finally {
            setCreating(false)
        }
    }

    const filteredCourses = courses.filter(c => {
        const matchesSearch = c.title.toLowerCase().includes(search.toLowerCase())
        const matchesStatus = statusFilter === 'ALL' || c.status === statusFilter
        return matchesSearch && matchesStatus
    })

    if (loading) {
        return (
            <div className="space-y-6">
                <div className="flex justify-between items-center">
                    <div className="h-8 w-48 bg-slate-800 animate-pulse rounded-lg" />
                    <div className="h-10 w-32 bg-slate-800 animate-pulse rounded-xl" />
                </div>
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
                    <h1 className="text-3xl font-extrabold text-white tracking-tight">Course Management</h1>
                    <p className="text-slate-400">Architect curricula and manage lesson distributions.</p>
                </div>
                <button
                    onClick={() => setShowCreate((prev) => !prev)}
                    className="px-5 py-2.5 bg-primary text-primary-foreground rounded-xl text-sm font-black transition-all flex items-center gap-2 shadow-lg shadow-primary/20 hover:scale-[1.02] active:scale-[0.98]"
                >
                    <Plus size={18} /> Create New Course
                </button>
            </div>

            {showCreate && (
                <form
                    onSubmit={handleCreateCourse}
                    className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl space-y-4"
                >
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="space-y-2">
                            <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">Course Title</label>
                            <input
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                className="w-full bg-slate-950 border border-slate-800 rounded-xl py-2.5 px-4 text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-primary/50"
                                placeholder="Advanced Mathematics"
                                required
                            />
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">Description</label>
                            <input
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                className="w-full bg-slate-950 border border-slate-800 rounded-xl py-2.5 px-4 text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-primary/50"
                                placeholder="Course overview for students"
                            />
                        </div>
                    </div>
                    <div className="flex justify-end">
                        <button
                            type="submit"
                            disabled={creating}
                            className="px-6 py-2.5 bg-primary text-primary-foreground rounded-xl text-xs font-black uppercase tracking-widest hover:opacity-90 disabled:opacity-60"
                        >
                            {creating ? 'Creating...' : 'Create Course'}
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
                        placeholder="Search curricula..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="w-full bg-slate-950 border border-slate-800 rounded-xl py-2 pl-10 pr-4 text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all font-medium"
                    />
                </div>
                <div className="flex items-center gap-2">
                    <div className="flex items-center gap-2 bg-slate-950 border border-slate-800 rounded-xl px-3 py-2">
                        <Filter size={16} className="text-slate-500" />
                        <select
                            value={statusFilter}
                            onChange={(e) => setStatusFilter(e.target.value)}
                            className="bg-transparent text-xs font-bold text-slate-300 uppercase tracking-widest focus:outline-none"
                        >
                            <option value="ALL">All Status</option>
                            <option value="PUBLISHED">Published</option>
                            <option value="DRAFT">Draft</option>
                            <option value="ARCHIVED">Archived</option>
                        </select>
                    </div>
                </div>
            </div>

            {/* Course Table/List */}
            <div className="bg-slate-900/40 border border-slate-800 rounded-3xl overflow-hidden shadow-2xl">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="border-b border-white/5 bg-slate-800/20">
                            <th className="px-6 py-4 text-[10px] font-black text-slate-500 uppercase tracking-widest">Curriculum Identity</th>
                            <th className="px-6 py-4 text-[10px] font-black text-slate-500 uppercase tracking-widest hidden sm:table-cell">Modules</th>
                            <th className="px-6 py-4 text-[10px] font-black text-slate-500 uppercase tracking-widest hidden lg:table-cell">ID Reference</th>
                            <th className="px-6 py-4 text-[10px] font-black text-slate-500 uppercase tracking-widest text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-white/5">
                        {filteredCourses.length === 0 ? (
                            <tr>
                                <td colSpan={4} className="px-6 py-20 text-center text-slate-500 italic">
                                    No curricula found matching your criteria.
                                </td>
                            </tr>
                        ) : (
                            filteredCourses.map((course) => (
                                <tr key={course.id} className="group hover:bg-white/[0.02] transition-colors cursor-pointer">
                                    <td className="px-6 py-5">
                                        <div className="flex items-center gap-4">
                                            <div className="w-10 h-10 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center border border-blue-500/20 group-hover:scale-110 transition-transform">
                                                <BookOpen size={20} />
                                            </div>
                                            <div className="min-w-0">
                                                <p className="text-sm font-bold text-white group-hover:text-primary transition-colors truncate">{course.title}</p>
                                                <p className="text-xs text-slate-500 truncate max-w-[200px]">{course.description || 'No description provided.'}</p>
                                            </div>
                                        </div>
                                    </td>
                                    <td className="px-6 py-5 hidden sm:table-cell">
                                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-800 text-[10px] font-bold text-slate-400 uppercase tracking-tight">
                                            <Layers size={10} /> {course.chaptersCount || 0} Layers
                                        </span>
                                    </td>
                                    <td className="px-6 py-5 hidden lg:table-cell text-[10px] font-mono text-slate-600">
                                        {course.id}
                                    </td>
                                    <td className="px-6 py-5 text-right">
                                        <div className="flex items-center justify-end gap-2">
                                            <Link
                                                to={`/admin/courses/${course.id}`}
                                                className="p-2 rounded-lg bg-slate-800/50 text-slate-500 hover:text-white hover:bg-slate-700 transition-all"
                                            >
                                                <ChevronRight size={18} />
                                            </Link>
                                            <button className="p-2 rounded-lg bg-slate-800/50 text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 transition-all">
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
                <p className="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Showing {filteredCourses.length} Curricula</p>
                <span className="text-[10px] font-bold text-slate-600">Page 1 of 1</span>
            </div>
        </div>
    )
}
