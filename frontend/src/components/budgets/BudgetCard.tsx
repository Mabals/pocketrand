import { useState } from 'react'
import { Trash2 } from 'lucide-react'
import { api, ApiError } from '../../api/client'
import type { Budget, BudgetStatus } from '../../types'
import { formatRands } from '../../utils/format'

const STATUS_STYLES: Record<BudgetStatus, { bar: string; badge: string; label: string }> = {
  ON_TRACK: { bar: 'bg-emerald-500', badge: 'bg-emerald-100 text-emerald-700', label: 'On track' },
  NEAR_LIMIT: { bar: 'bg-amber-500', badge: 'bg-amber-100 text-amber-700', label: 'Near limit' },
  OVER_LIMIT: { bar: 'bg-rose-500', badge: 'bg-rose-100 text-rose-700', label: 'Over limit' },
}

type BudgetCardProps = {
  budget: Budget
  onChanged: () => void
}

export default function BudgetCard({ budget, onChanged }: BudgetCardProps) {
  const [deleting, setDeleting] = useState(false)
  const [error, setError] = useState('')

  async function remove() {
    if (!window.confirm(`Remove your ${budget.label} budget?`)) {
      return
    }
    setDeleting(true)
    try {
      await api<void>(`/budgets/${budget.category}`, { method: 'DELETE' })
      onChanged()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not remove the budget')
      setDeleting(false)
    }
  }

  const style = STATUS_STYLES[budget.status]
  const isOver = budget.remaining < 0

  return (
    <div className={`rounded-2xl border border-slate-200 bg-white p-5 shadow-sm ${deleting ? 'opacity-50' : ''}`}>
      <div className="flex items-center justify-between gap-3">
        <h3 className="font-semibold text-slate-900">{budget.label}</h3>
        <div className="flex items-center gap-2">
          <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${style.badge}`}>{style.label}</span>
          <button
            onClick={remove}
            disabled={deleting}
            title="Remove budget"
            className="rounded-lg p-1.5 text-slate-400 hover:bg-red-50 hover:text-red-600"
          >
            <Trash2 size={16} />
          </button>
        </div>
      </div>

      <div className="mt-4 h-2.5 w-full overflow-hidden rounded-full bg-slate-100">
        <div
          className={`h-full rounded-full ${style.bar}`}
          style={{ width: `${Math.min(budget.percentUsed, 100)}%` }}
        />
      </div>

      <div className="mt-3 flex justify-between gap-3 text-sm">
        <span className="text-slate-600">
          {formatRands(budget.spent)} of {formatRands(budget.monthlyLimit)}
        </span>
        <span className={isOver ? 'font-medium text-rose-700' : 'text-slate-500'}>
          {isOver ? `${formatRands(Math.abs(budget.remaining))} over` : `${formatRands(budget.remaining)} left`}
        </span>
      </div>
      <p className="mt-1 text-xs text-slate-400">{budget.percentUsed}% used</p>
      {error && <p className="mt-2 text-xs text-red-600">{error}</p>}
    </div>
  )
}