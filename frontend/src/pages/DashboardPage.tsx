import { useState } from 'react'
import { Link } from 'react-router'
import { Receipt } from 'lucide-react'
import { useAuth } from '../auth/useAuth'
import { useApi } from '../hooks/useApi'
import type { MonthlySummary, SpendingTips, Transaction } from '../types'
import { currentMonth, formatMonth } from '../utils/format'

import MoneyHero from '../components/dashboard/MoneyHero'
import SpendingTrend from '../components/dashboard/SpendingTrend'
import SpendingBreakdown from '../components/dashboard/SpendingBreakdown'

import TipsCard from '../components/dashboard/TipsCard'
import TopExpenses from '../components/dashboard/TopExpenses'
import MonthPicker from '../components/MonthPicker'
import DashboardSkeleton from '../components/dashboard/DashboardSkeleton'



export default function DashboardPage() {
  const { user } = useAuth()
  const [month, setMonth] = useState(currentMonth())

  const summary = useApi<MonthlySummary>(`/summary?month=${month}`)
  const hasData = (summary.data?.transactionCount ?? 0) > 0
  const tips = useApi<SpendingTips>(hasData ? `/summary/tips?month=${month}` : null)
  const transactions = useApi<Transaction[]>(hasData ? `/transactions?month=${month}` : null)

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
          <div className="grid gap-4 lg:grid-cols-3">
            <div className="lg:col-span-2">
              <MoneyHero
                income={summary.data.income}
                expenses={summary.data.expenses}
                net={summary.data.net}
                changePercent={summary.data.expenseChangePercent}
              />
            </div>
            <TipsCard tips={tips.data} loading={tips.loading} error={tips.error} />
          </div>

          {transactions.data && <SpendingTrend month={month} transactions={transactions.data} />}

          <div className="grid gap-4 lg:grid-cols-3">
            <div className="lg:col-span-2">
              <SpendingBreakdown categories={summary.data.spendingByCategory} total={summary.data.expenses} />
            </div>
            <TopExpenses expenses={summary.data.topExpenses} />
          </div>
        </>
      )}
    </div>
  )
}