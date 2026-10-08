import type { Transaction } from '../../types'
import { formatDate, formatRands } from '../../utils/format'
import CategoryIcon from '../CategoryIcon'

type TopExpensesProps = {
  expenses: Transaction[]
}

export default function TopExpenses({ expenses }: TopExpensesProps) {
  return (
    <div className="h-full rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="font-semibold text-slate-900">Biggest expenses</h2>
      <ul className="mt-2 divide-y divide-slate-100">
        {expenses.map((transaction) => (
          <li key={transaction.id} className="flex items-center justify-between gap-4 py-3">
            <div className="flex items-center gap-3">
              <CategoryIcon category={transaction.category} />
              <div>
                <p className="font-medium text-slate-900">{transaction.description}</p>
                <p className="text-xs text-slate-500">
                  {formatDate(transaction.date)} · {transaction.categoryLabel}
                </p>
              </div>
            </div>
            <span className="shrink-0 font-semibold text-rose-700">{formatRands(transaction.amount)}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}