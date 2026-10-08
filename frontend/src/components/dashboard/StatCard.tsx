import type { ReactNode } from 'react'

type StatCardProps = {
  label: string
  value: string
  tone: 'positive' | 'negative'
  icon: ReactNode
  note?: string
}

export default function StatCard({ label, value, tone, icon, note }: StatCardProps) {
  const toneClasses = tone === 'positive' ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center justify-between">
        <span className="text-sm font-medium text-slate-500">{label}</span>
        <span className={`rounded-lg p-2 ${toneClasses}`}>{icon}</span>
      </div>
      <p className="mt-3 text-2xl font-bold text-slate-900">{value}</p>
      {note && <p className="mt-1 text-xs text-slate-500">{note}</p>}
    </div>
  )
}