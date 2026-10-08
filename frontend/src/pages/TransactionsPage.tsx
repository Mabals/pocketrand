import { useState } from 'react'
import { useApi } from '../hooks/useApi'
import type { Transaction } from '../types'
import { currentMonth, formatMonth } from '../utils/format'
import MonthPicker from '../components/MonthPicker'
import UploadCard from '../components/transactions/UploadCard'
import TransactionRow from '../components/transactions/TransactionRow'

export default function TransactionsPage() {
  const [month, setMonth] = useState(currentMonth())
  const transactions = useApi<Transaction[]>(`/transactions?month=${month}`)

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Transactions</h1>
          <p className="mt-1 text-slate-500">Upload a statement, then check or change any category.</p>
        </div>
        <MonthPicker value={month} onChange={setMonth} />
      </div>

      <UploadCard onImported={transactions.reload} />

      <div className="rounded-2xl border border-slate-200 bg-white shadow-sm">
        {transactions.loading && <p className="p-6 text-sm text-slate-500">Loading transactions…</p>}

        {transactions.error && <p className="p-6 text-sm text-red-700">{transactions.error}</p>}

        {transactions.data && transactions.data.length === 0 && (
          <p className="p-10 text-center text-sm text-slate-500">
            No transactions in {formatMonth(month)}. Try another month, or upload a statement.
          </p>
        )}

        {transactions.data && transactions.data.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">Date</th>
                  <th className="px-4 py-3 font-medium">Description</th>
                  <th className="px-4 py-3 font-medium">Category</th>
                  <th className="px-4 py-3 font-medium">Source</th>
                  <th className="px-4 py-3 text-right font-medium">Amount</th>
                  <th className="px-4 py-3">
                    <span className="sr-only">Actions</span>
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {transactions.data.map((transaction) => (
                  <TransactionRow key={transaction.id} transaction={transaction} onChanged={transactions.reload} />
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}