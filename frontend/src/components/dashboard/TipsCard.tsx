import { Sparkles } from 'lucide-react'
import type { SpendingTips } from '../../types'

type TipsCardProps = {
  tips?: SpendingTips
  loading: boolean
  error?: string
}

export default function TipsCard({ tips, loading, error }: TipsCardProps) {
  return (
    <div className="h-full rounded-2xl border border-emerald-200 bg-emerald-50 p-5 shadow-sm">
      <div className="flex items-center gap-2">
        <Sparkles size={18} className="text-emerald-600" />
        <h2 className="font-semibold text-slate-900">Insights</h2>
        {tips?.aiGenerated && (
          <span className="ml-auto rounded-full bg-emerald-600 px-2 py-0.5 text-xs font-medium text-white">AI</span>
        )}
      </div>

      {loading && <p className="mt-4 text-sm text-slate-500">Thinking about your month…</p>}
      {error && <p className="mt-4 text-sm text-rose-700">{error}</p>}

      {tips && (
        <ul className="mt-4 space-y-3">
          {tips.tips.map((tip) => (
            <li key={tip} className="flex gap-2 text-sm text-slate-700">
              <span className="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-emerald-500" />
              {tip}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}