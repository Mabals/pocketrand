import { Bar, BarChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { CategoryTotal } from '../../types'
import { formatRands } from '../../utils/format'

type CategoryChartProps = {
  categories: CategoryTotal[]
}

export default function CategoryChart({ categories }: CategoryChartProps) {
  return (
    <div className="h-full rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="font-semibold text-slate-900">Spending by category</h2>
      {categories.length === 0 ? (
        <p className="mt-4 text-sm text-slate-500">No spending this month.</p>
      ) : (
        <div className="mt-4">
          <ResponsiveContainer width="100%" height={categories.length * 40 + 10}>
            <BarChart data={categories} layout="vertical" margin={{ top: 0, right: 16, bottom: 0, left: 0 }}>
              <XAxis type="number" hide />
              <YAxis
                type="category"
                dataKey="label"
                width={130}
                tickLine={false}
                axisLine={false}
                tick={{ fontSize: 13, fill: '#475569' }}
              />
              <Tooltip
                formatter={(value, _name, item) => [
                  `${formatRands(Number(value))} (${item.payload.percentage}%)`,
                  'Spent',
                ]}
                cursor={{ fill: '#f1f5f9' }}
              />
              <Bar dataKey="amount" fill="#059669" radius={[0, 6, 6, 0]} barSize={18} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}
    </div>
  )
}