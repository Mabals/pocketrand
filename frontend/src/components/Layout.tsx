import { Link, NavLink, Outlet } from 'react-router'
import { Calculator, LayoutDashboard, LogOut, PiggyBank, ReceiptText, type LucideIcon } from 'lucide-react'
import { useAuth } from '../auth/useAuth'
import Logo from './Logo'

const LINKS: { to: string; label: string; icon: LucideIcon }[] = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/transactions', label: 'Transactions', icon: ReceiptText },
  { to: '/budgets', label: 'Budgets', icon: PiggyBank },
  { to: '/tax', label: 'Tax estimate', icon: Calculator },
]

export default function Layout() {
  const { user, logout } = useAuth()
  const initials = (user?.fullName ?? '')
    .split(' ')
    .map((part) => part[0])
    .slice(0, 2)
    .join('')
    .toUpperCase()
  const isDemo = user?.email.endsWith('@demo.invalid') ?? false

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-10 border-b border-slate-200 bg-white/80 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3">
          <Logo />
          <div className="flex items-center gap-2">
            <NavLink
              to="/settings"
              title="Settings"
              className={({ isActive }) =>
                `flex h-9 w-9 items-center justify-center rounded-full text-sm font-semibold transition ${
                  isActive ? 'bg-emerald-600 text-white' : 'bg-emerald-100 text-emerald-700 hover:bg-emerald-200'
                }`
              }
            >
              {initials}
            </NavLink>
            <button
              onClick={logout}
              className="inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm text-slate-600 transition hover:bg-slate-100"
            >
              <LogOut size={16} />
              <span className="hidden sm:inline">Log out</span>
            </button>
          </div>
        </div>
        <nav className="mx-auto flex max-w-6xl gap-1 overflow-x-auto px-4 pb-2">
          {LINKS.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end
              className={({ isActive }) =>
                `inline-flex shrink-0 items-center gap-2 rounded-lg px-3 py-1.5 text-sm font-medium transition ${
                  isActive ? 'bg-emerald-50 text-emerald-700' : 'text-slate-600 hover:bg-slate-100'
                }`
              }
            >
              <Icon size={16} />
              {label}
            </NavLink>
          ))}
        </nav>
      </header>
      {isDemo && (
        <div className="border-b border-amber-200 bg-amber-50 px-4 py-2 text-center text-sm text-amber-800">
          You're exploring a demo account with sample data. It's deleted automatically after 24 hours.
        </div>
      )}

      <main className="mx-auto max-w-6xl px-4 py-8">
        <Outlet />
      </main>

      <footer className="mx-auto max-w-6xl px-4 pb-8 text-xs text-slate-400">
        <Link to="/privacy" className="hover:text-slate-600 hover:underline">
          Privacy notice
        </Link>{' '}
        · PocketRand is a portfolio project. Please use sample statements while testing.
      </footer>
    </div>
  )
}