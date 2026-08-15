import type { ReactNode } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'

type Props = { title: string; children: ReactNode }

export function DashboardLayout({ title, children }: Props) {
  const navigate = useNavigate()
  const user = JSON.parse(sessionStorage.getItem('bankingUser') ?? 'null') as { role?: string } | null
  function logout() {
    sessionStorage.removeItem('bankingUser')
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <div className="flex items-center gap-3"><span className="grid size-10 place-items-center rounded-xl bg-blue-900 text-sm font-bold text-white">POR</span><div><p className="font-bold">POR Banking System</p><p className="text-xs text-slate-500">{title} · Palaging Overtime si Rodney</p></div></div>
          <nav className="flex items-center gap-5 text-sm font-semibold">
            <NavLink className="text-blue-800 hover:text-blue-950" to="/banking">My banking</NavLink>
            {user?.role === 'ADMIN' && <NavLink className="text-blue-800 hover:text-blue-950" to="/admin">Admin</NavLink>}
            <button className="rounded-lg border border-slate-300 px-3 py-2 hover:bg-slate-50" onClick={logout}>Sign out</button>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-6 py-10">{children}</main>
    </div>
  )
}
