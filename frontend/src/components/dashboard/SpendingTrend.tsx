import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { Transaction } from '../../types'
import { formatRands } from '../../utils/format'

type SpendingTrendProps = {
  month: string
  transactions: Transaction[]
}

function buildDailySpending(transactions: Transaction[], month: string) {
  const [year, monthNumber] = month.split('-').map(Number)
  const daysInMonth = new Date(year, monthNumber, 0).getDate()
  const spentPerDay: number[] = new Array(daysInMonth).fill(0)

  for (const transaction of transactions) {
    if (transaction.amount < 0) {
      const day = Number(transaction.date.slice(8, 10))
      spentPerDay[day - 1] += Math.abs(transaction.amount)
    }
  }

  let runningTotal = 0
  return spentPerDay.map((spent, index) => {
    runningTotal += spent
    return { day: index + 1, total: Math.round(runningTotal * 100) / 100 }
  })
}

function shortRands(value: number): string {
  return value >= 1000 ? `R${Math.round(value / 1000)}k` : `R${Math.round(value)}`
}

export default function SpendingTrend({ month, transactions }: SpendingTrendProps) {
  const data = buildDailySpending(transactions, month)

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="font-semibold text-slate-900">Spending through the month</h2>
      <p className="text-sm text-slate-500">Running total, day by day</p>
      <div className="mt-4">
        <ResponsiveContainer width="100%" height={220}>
          <AreaChart data={data} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
            <defs>
              <linearGradient id="spendFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#059669" stopOpacity={0.35} />
                <stop offset="100%" stopColor="#059669" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid vertical={false} stroke="#e2e8f0" strokeDasharray="3 3" />
            <XAxis
              dataKey="day"
              tickLine={false}
              axisLine={false}
              interval="preserveStartEnd"
              tick={{ fontSize: 12, fill: '#64748b' }}
            />
            <YAxis
              tickLine={false}
              axisLine={false}
              width={56}
              tick={{ fontSize: 12, fill: '#64748b' }}
              tickFormatter={(value) => shortRands(Number(value))}
            />
            <Tooltip
              formatter={(value) => [formatRands(Number(value)), 'Spent so far']}
              labelFormatter={(day) => `Day ${day}`}
            />
            <Area type="monotone" dataKey="total" stroke="#059669" strokeWidth={2.5} fill="url(#spendFill)" />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}