import { useEffect, useState } from 'react'

type Health = {
  status: string
  app: string
  checkedAt: string
}

export default function App() {
  const [health, setHealth] = useState<Health | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    fetch('/api/health')
      .then((response) => {
        if (!response.ok) {
          throw new Error(`Server responded with ${response.status}`)
        }
        return response.json() as Promise<Health>
      })
      .then(setHealth)
      .catch(() => setError('Could not reach the PocketRand API. Is the backend running?'))
  }, [])

  return (
    <main className="min-h-screen bg-slate-50 flex items-center justify-center px-4">
      <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="text-2xl font-bold text-slate-900">PocketRand</h1>
        <p className="mt-1 text-sm text-slate-500">Checking the connection to the backend.</p>

        {error && (
          <p className="mt-6 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </p>
        )}

        {!health && !error && <p className="mt-6 text-sm text-slate-500">Checking the API…</p>}

        {health && (
          <dl className="mt-6 space-y-2 text-sm">
            <div className="flex justify-between">
              <dt className="text-slate-500">Status</dt>
              <dd className="font-semibold text-emerald-700">{health.status}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-slate-500">App</dt>
              <dd className="text-slate-900">{health.app}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-slate-500">Checked at</dt>
              <dd className="text-slate-900">{new Date(health.checkedAt).toLocaleTimeString()}</dd>
            </div>
          </dl>
        )}
      </div>
    </main>
  )
}