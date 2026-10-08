import { useState } from 'react'
import { Trash2 } from 'lucide-react'
import { api, ApiError } from '../../api/client'
import type { Category, CategorySource, Transaction } from '../../types'
import { CATEGORY_OPTIONS } from '../../utils/categories'
import { formatDate, formatRands } from '../../utils/format'
import CategoryIcon from '../CategoryIcon'

const SOURCE_BADGES: Record<CategorySource, { label: string; className: string; title: string }> = {
  RULE: { label: 'Rule', className: 'bg-slate-100 text-slate-600', title: 'Categorised by a keyword rule' },
  AI: { label: 'AI', className: 'bg-violet-100 text-violet-700', title: 'Categorised by AI' },
  USER: { label: 'You', className: 'bg-emerald-100 text-emerald-700', title: 'Chosen by you' },
}

type TransactionRowProps = {
  transaction: Transaction
  onChanged: () => void
}

export default function TransactionRow({ transaction, onChanged }: TransactionRowProps) {
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  async function changeCategory(category: Category) {
    setSaving(true)
    setError('')
    try {
      await api<Transaction>(`/transactions/${transaction.id}`, {
        method: 'PUT',
        body: JSON.stringify({
          date: transaction.date,
          description: transaction.description,
          amount: transaction.amount,
          category,
        }),
      })
      onChanged()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not save')
    } finally {
      setSaving(false)
    }
  }

  async function remove() {
    if (!window.confirm(`Delete "${transaction.description}"?`)) {
      return
    }
    setSaving(true)
    try {
      await api<void>(`/transactions/${transaction.id}`, { method: 'DELETE' })
      onChanged()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not delete')
      setSaving(false)
    }
  }

  const badge = SOURCE_BADGES[transaction.categorySource]
  const isIncome = transaction.amount > 0

  return (
    <tr className={saving ? 'opacity-50' : ''}>
      <td className="whitespace-nowrap px-4 py-3 text-slate-500">{formatDate(transaction.date)}</td>
      <td className="px-4 py-3">
        <div className="flex items-center gap-3">
          <CategoryIcon category={transaction.category} />
          <div>
            <p className="font-medium text-slate-900">{transaction.description}</p>
            {error && <p className="text-xs text-red-600">{error}</p>}
          </div>
        </div>
      </td>
      <td className="px-4 py-3">
        <select
          value={transaction.category}
          disabled={saving}
          onChange={(event) => changeCategory(event.target.value as Category)}
          className="rounded-lg border border-slate-300 bg-white px-2 py-1.5 text-sm"
        >
          {CATEGORY_OPTIONS.map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
      </td>
      <td className="px-4 py-3">
        <span title={badge.title} className={`rounded-full px-2 py-0.5 text-xs font-medium ${badge.className}`}>
          {badge.label}
        </span>
      </td>
      <td
        className={`whitespace-nowrap px-4 py-3 text-right font-semibold ${
          isIncome ? 'text-emerald-700' : 'text-slate-900'
        }`}
      >
        {formatRands(transaction.amount)}
      </td>
      <td className="px-4 py-3 text-right">
        <button
          onClick={remove}
          disabled={saving}
          title="Delete"
          className="rounded-lg p-1.5 text-slate-400 hover:bg-red-50 hover:text-red-600"
        >
          <Trash2 size={16} />
        </button>
      </td>
    </tr>
  )
}