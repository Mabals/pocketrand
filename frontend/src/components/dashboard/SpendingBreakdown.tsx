import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import type { CategoryTotal } from '../../types'
import { CATEGORY_STYLES } from '../../utils/categoryStyles'
import { formatRands } from '../../utils/format'
import CategoryIcon from '../CategoryIcon'

type SpendingBreakdownProps = {
  categories: CategoryTotal[]
  total: number
}

export default function SpendingBreakdown({ categories, total }: SpendingBreakdownProps) {
  return (
    <div className="h-full rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="font-semibold text-slate-900">Where your money went</h2>

      {categories.length === 0 ? (
        <p className="mt-4 text-sm text-slate-500">No spending this month.</p>
      ) : (
        <div className="mt-4 flex flex-col items-center gap-6 sm:flex-row sm:items-start">
          <div className="relative h-52 w-52 shrink-0">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={categories}
                  dataKey="amount"
                  nameKey="label"
                  innerRadius="64%"
                  outerRadius="92%"
                  paddingAngle={2}
                  stroke="none"
                >
                  {categories.map((category) => (
                    <Cell key={category.category} fill={CATEGORY_STYLES[category.category].color} />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => formatRands(Number(value))} />
              </PieChart>
            </ResponsiveContainer>
            <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
              <span className="text-xs text-slate-500">Spent</span>
              <span className="text-lg font-bold text-slate-900">{formatRands(total)}</span>
            </div>
          </div>

          <ul className="w-full flex-1 space-y-3">
            {categories.map((category) => (
              <li key={category.category}>
                <div className="flex items-center gap-3">
                  <CategoryIcon category={category.category} />
                  <span className="flex-1 text-sm font-medium text-slate-700">{category.label}</span>
                  <span className="text-sm font-semibold text-slate-900">{formatRands(category.amount)}</span>
                  <span className="w-12 text-right text-xs text-slate-500">{category.percentage}%</span>
                </div>
                <div className="ml-10 mt-1.5 h-1.5 overflow-hidden rounded-full bg-slate-100">
                  <div
                    className="h-full rounded-full"
                    style={{
                      width: `${category.percentage}%`,
                      backgroundColor: CATEGORY_STYLES[category.category].color,
                    }}
                  />
                </div>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}