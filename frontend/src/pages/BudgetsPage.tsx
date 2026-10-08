import { useState } from 'react'
import { PiggyBank } from 'lucide-react'
import { useApi } from '../hooks/useApi'
import type { Budget } from '../types'
import { currentMonth, formatMonth } from '../utils/format'
import MonthPicker from '../components/MonthPicker'
import BudgetForm from '../components/budgets/BudgetForm'
import BudgetCard from '../components/budgets/BudgetCard'
import Skeleton from '../components/Skeleton'

export default function BudgetsPage() {
  const [month, setMonth] = useState(currentMonth())
  const budgets = useApi<Budget[]>(`/budgets?month=${month}`)

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Budgets</h1>
          <p className="mt-1 text-slate-500">Set monthly limits and see how {formatMonth(month)} is going.</p>
        </div>
        <MonthPicker value={month} onChange={setMonth} />
      </div>

      <BudgetForm onSaved={budgets.reload} />

      {budgets.loading && (
        <div className="grid gap-4 sm:grid-cols-2">
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
        </div>
        )}
      {budgets.error && <p className="text-sm text-red-700">{budgets.error}</p>}

      {budgets.data && budgets.data.length === 0 && (
        <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-10 text-center">
          <PiggyBank className="mx-auto text-slate-400" size={32} />
          <p className="mt-3 font-medium text-slate-900">No budgets yet</p>
          <p className="mt-1 text-sm text-slate-500">Set a limit for a category above, like Groceries or Transport.</p>
        </div>
      )}

      {budgets.data && budgets.data.length > 0 && (
        <div className="grid gap-4 sm:grid-cols-2">
          {budgets.data.map((budget) => (
            <BudgetCard key={budget.category} budget={budget} onChanged={budgets.reload} />
          ))}
        </div>
      )}
    </div>
  )
}