import { useState, type FormEvent } from 'react'
import { Calculator } from 'lucide-react'
import { api, ApiError } from '../api/client'
import type { SalaryPeriod, TaxEstimate } from '../types'
import { formatRands } from '../utils/format'

export default function TaxPage() {
  const [salary, setSalary] = useState('')
  const [period, setPeriod] = useState<SalaryPeriod>('MONTHLY')
  const [age, setAge] = useState('')
  const [estimate, setEstimate] = useState<TaxEstimate | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [error, setError] = useState('')
  const [calculating, setCalculating] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setCalculating(true)
    setError('')
    setFieldErrors({})
    try {
      const result = await api<TaxEstimate>('/tax/estimate', {
        method: 'POST',
        body: JSON.stringify({ grossSalary: Number(salary), period, age: Number(age) }),
      })
      setEstimate(result)
    } catch (err) {
      if (err instanceof ApiError) {
        setFieldErrors(err.fieldErrors)
        setError(Object.keys(err.fieldErrors).length > 0 ? '' : err.message)
      } else {
        setError('Could not calculate the estimate')
      }
    } finally {
      setCalculating(false)
    }
  }

  const rows = estimate
    ? [
        { label: 'Gross salary', monthly: estimate.monthlyGross, annual: estimate.annualGross },
        { label: 'Income tax (PAYE)', monthly: -estimate.monthlyTax, annual: -estimate.annualTax },
        { label: 'UIF', monthly: -estimate.monthlyUif, annual: -estimate.monthlyUif * 12 },
      ]
    : []

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Tax estimate</h1>
        <p className="mt-1 text-slate-500">See your income tax and take-home pay using the latest SARS tables.</p>
      </div>

      <div className="grid gap-6 lg:grid-cols-5">
        <form
          onSubmit={handleSubmit}
          className="space-y-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm lg:col-span-2"
        >
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Gross salary (R)</span>
            <input
              type="number"
              step="0.01"
              required
              value={salary}
              onChange={(event) => setSalary(event.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2"
            />
            {fieldErrors.grossSalary && (
              <span className="mt-1 block text-xs text-red-600">{fieldErrors.grossSalary}</span>
            )}
          </label>

          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">This salary is</span>
            <select
              value={period}
              onChange={(event) => setPeriod(event.target.value as SalaryPeriod)}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
            >
              <option value="MONTHLY">Per month</option>
              <option value="ANNUAL">Per year</option>
            </select>
          </label>

          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Your age</span>
            <input
              type="number"
              required
              value={age}
              onChange={(event) => setAge(event.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2"
            />
            {fieldErrors.age && <span className="mt-1 block text-xs text-red-600">{fieldErrors.age}</span>}
          </label>

          {error && <p className="text-sm text-red-700">{error}</p>}

          <button
            type="submit"
            disabled={calculating}
            className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-emerald-600 py-2.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
          >
            <Calculator size={16} />
            {calculating ? 'Calculating…' : 'Calculate'}
          </button>
        </form>

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm lg:col-span-3">
          {!estimate ? (
            <p className="text-sm text-slate-500">Enter your salary and age to see your estimate.</p>
          ) : (
            <>
              <p className="text-sm font-medium text-slate-500">Monthly take-home pay</p>
              <p className="mt-1 text-3xl font-bold text-emerald-700">{formatRands(estimate.monthlyTakeHome)}</p>

              <table className="mt-6 w-full text-sm">
                <thead className="text-left text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="pb-2 font-medium" />
                    <th className="pb-2 text-right font-medium">Monthly</th>
                    <th className="pb-2 text-right font-medium">Annual</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {rows.map((row) => (
                    <tr key={row.label}>
                      <td className="py-2 text-slate-600">{row.label}</td>
                      <td className="py-2 text-right text-slate-900">{formatRands(row.monthly)}</td>
                      <td className="py-2 text-right text-slate-900">{formatRands(row.annual)}</td>
                    </tr>
                  ))}
                  <tr className="font-semibold">
                    <td className="py-2 text-slate-900">Take-home</td>
                    <td className="py-2 text-right text-emerald-700">{formatRands(estimate.monthlyTakeHome)}</td>
                    <td className="py-2 text-right text-emerald-700">{formatRands(estimate.annualTakeHome)}</td>
                  </tr>
                </tbody>
              </table>

              <div className="mt-6 grid grid-cols-3 gap-3 text-center">
                <div className="rounded-xl bg-slate-50 p-3">
                  <p className="text-xs text-slate-500">Effective rate</p>
                  <p className="mt-1 font-semibold text-slate-900">{estimate.effectiveRatePercent}%</p>
                </div>
                <div className="rounded-xl bg-slate-50 p-3">
                  <p className="text-xs text-slate-500">Marginal rate</p>
                  <p className="mt-1 font-semibold text-slate-900">{estimate.marginalRatePercent}%</p>
                </div>
                <div className="rounded-xl bg-slate-50 p-3">
                  <p className="text-xs text-slate-500">Rebates</p>
                  <p className="mt-1 font-semibold text-slate-900">{formatRands(estimate.rebates)}</p>
                </div>
              </div>

              <p className="mt-4 text-xs text-slate-400">{estimate.note}</p>
            </>
          )}
        </div>
      </div>
    </div>
  )
}