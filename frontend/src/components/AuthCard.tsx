import type { ReactNode } from 'react'
import { Link } from 'react-router'
import { FileUp, PiggyBank, Sparkles, type LucideIcon } from 'lucide-react'
import Logo from './Logo'

const FEATURES: { icon: LucideIcon; text: string }[] = [
  { icon: FileUp, text: 'Import PDF or CSV bank statements' },
  { icon: Sparkles, text: 'Automatic AI categorisation' },
  { icon: PiggyBank, text: 'Budgets with early warnings' },
]

type AuthCardProps = {
  title: string
  subtitle: string
  children: ReactNode
}

export default function AuthCard({ title, subtitle, children }: AuthCardProps) {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <aside className="hidden flex-col justify-between bg-emerald-900 p-12 text-white lg:flex">
        <Logo light />
        <div>
          <h2 className="text-4xl font-extrabold leading-tight">Know where every rand goes.</h2>
          <p className="mt-4 max-w-md text-emerald-100">
            Upload your bank statement and PocketRand sorts your spending, tracks your budgets and gives you
            practical tips.
          </p>
          <ul className="mt-8 space-y-4">
            {FEATURES.map(({ icon: Icon, text }) => (
              <li key={text} className="flex items-center gap-3 text-emerald-50">
                <span className="rounded-lg bg-emerald-800 p-2">
                  <Icon size={18} />
                </span>
                {text}
              </li>
            ))}
          </ul>
        </div>
        <p className="text-xs text-emerald-300">Your name and account number never leave PocketRand's server.</p>
      </aside>

      <main className="flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-sm">
          <div className="mb-8 lg:hidden">
            <Logo />
          </div>
          <h1 className="text-2xl font-bold text-slate-900">{title}</h1>
          <p className="mt-1 text-sm text-slate-500">{subtitle}</p>
          <div className="mt-6">{children}</div>
          <p className="mt-8 text-center text-xs text-slate-400">
          <Link to="/privacy" className="hover:text-slate-600 hover:underline">
            Privacy notice
          </Link>
        </p>
        </div>
      </main>
    </div>
  )
}