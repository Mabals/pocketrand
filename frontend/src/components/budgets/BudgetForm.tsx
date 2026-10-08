import { useState, type FormEvent } from 'react'
import { api, ApiError } from '../../api/client'
import type { Budget, Category } from '../../types'
import { CATEGORY_OPTIONS } from '../../utils/categories'

const EXPENSE_CATEGORIES = CATEGORY_OPTIONS.filter(([value]) => value !== 'INCOME')

type BudgetFormProps = {
  onSaved: () => void
}

export default function BudgetForm({ onSaved }: BudgetFormProps) {
  const [category, setCategory] = useState<Category>('GROCERIES')
  const [limit, setLimit] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError('')
    try {
      await api<Budget>(`/budgets/${category}`, {
        method: 'PUT',
        body: JSON.stringify({ monthlyLimit: Number(limit) }),
      })
      setLimit('')
      onSaved()
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.fieldErrors.monthlyLimit ?? err.message)
      } else {
        setError('Could not save the budget')
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="flex flex-wrap items-end gap-3 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
    >
      <div className="w-full">
        <h2 className="font-semibold text-slate-900">Set a monthly budget</h2>
        <p className="mt-1 text-sm text-slate-500">Picking a category that already has a budget updates its limit.</p>
      </div>

      <label className="text-sm">
        <span className="mb-1 block font-medium text-slate-700">Category</span>
        <select
          value={category}
          onChange={(event) => setCategory(event.target.value as Category)}
          className="rounded-lg border border-slate-300 bg-white px-3 py-2"
        >
          {EXPENSE_CATEGORIES.map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
      </label>

      <label className="text-sm">
        <span className="mb-1 block font-medium text-slate-700">Monthly limit (R)</span>
        <input
          type="number"
          min="1"
          step="0.01"
          required
          value={limit}
          onChange={(event) => setLimit(event.target.value)}
          className="w-40 rounded-lg border border-slate-300 px-3 py-2"
        />
      </label>

      <button
        type="submit"
        disabled={saving}
        className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
      >
        {saving ? 'Saving…' : 'Save budget'}
      </button>

      {error && <p className="w-full text-sm text-red-700">{error}</p>}
    </form>
  )
}