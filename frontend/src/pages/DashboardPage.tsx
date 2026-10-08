import { useState } from 'react'
import { Link } from 'react-router'
import { Receipt, TrendingDown, TrendingUp, Wallet } from 'lucide-react'
import { useAuth } from '../auth/useAuth'
import { useApi } from '../hooks/useApi'
import type { MonthlySummary, SpendingTips } from '../types'
import { currentMonth, formatMonth, formatRands } from '../utils/format'
import StatCard from '../components/dashboard/StatCard'
import CategoryChart from '../components/dashboard/CategoryChart'
import TipsCard from '../components/dashboard/TipsCard'
import TopExpenses from '../components/dashboard/TopExpenses'
import MonthPicker from '../components/MonthPicker'
import DashboardSkeleton from '../components/dashboard/DashboardSkeleton'

function changeNote(percent: number | null): string | undefined {
  if (percent === null) {
    return undefined
  }
  return `${percent > 0 ? 'Up' : 'Down'} ${Math.abs(percent)}% on last month`
}

export default function DashboardPage() {
  const { user } = useAuth()
  const [month, setMonth] = useState(currentMonth())

  const summary = useApi<MonthlySummary>(`/summary?month=${month}`)
  const hasData = (summary.data?.transactionCount ?? 0) > 0
  const tips = useApi<SpendingTips>(hasData ? `/summary/tips?month=${month}` : null)

  const firstName = user?.fullName.split(' ')[0]

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Hi {firstName} 👋</h1>
          <p className="mt-1 text-slate-500">Here's your money in {formatMonth(month)}.</p>
        </div>
        <MonthPicker value={month} onChange={setMonth} />
      </div>

      {summary.loading && <DashboardSkeleton />}

      {summary.error && (
        <p className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{summary.error}</p>
      )}

      {summary.data && !hasData && (
        <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-10 text-center">
          <Receipt className="mx-auto text-slate-400" size={32} />
          <p className="mt-3 font-medium text-slate-900">No transactions in {formatMonth(month)} yet</p>
          <p className="mt-1 text-sm text-slate-500">Upload a bank statement to see where your money goes.</p>
          <Link
            to="/transactions"
            className="mt-4 inline-block rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
          >
            Upload a statement
          </Link>
        </div>
      )}

      {summary.data && hasData && (
        <>
          <div className="grid gap-4 sm:grid-cols-3">
            <StatCard
              label="Income"
              value={formatRands(summary.data.income)}
              tone="positive"
              icon={<TrendingUp size={18} />}
            />
            <StatCard
              label="Spending"
              value={formatRands(summary.data.expenses)}
              tone="negative"
              icon={<TrendingDown size={18} />}
              note={changeNote(summary.data.expenseChangePercent)}
            />
            <StatCard
              label="Left over"
              value={formatRands(summary.data.net)}
              tone={summary.data.net >= 0 ? 'positive' : 'negative'}
              icon={<Wallet size={18} />}
            />
          </div>

          <div className="grid gap-4 lg:grid-cols-3">
            <div className="lg:col-span-2">
              <CategoryChart categories={summary.data.spendingByCategory} />
            </div>
            <TipsCard tips={tips.data} loading={tips.loading} error={tips.error} />
          </div>

          <TopExpenses expenses={summary.data.topExpenses} />
        </>
      )}
    </div>
  )
}