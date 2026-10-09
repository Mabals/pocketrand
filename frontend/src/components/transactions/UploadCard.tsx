import { useState, type FormEvent } from 'react'
import { CheckCircle2, Upload } from 'lucide-react'
import { api, ApiError } from '../../api/client'
import type { ImportResult } from '../../types'
import { Link } from 'react-router'

type UploadCardProps = {
  onImported: () => void
}

export default function UploadCard({ onImported }: UploadCardProps) {
  const [file, setFile] = useState<File | null>(null)
  const [inputKey, setInputKey] = useState(0)
  const [uploading, setUploading] = useState(false)
  const [result, setResult] = useState<ImportResult | null>(null)
  const [error, setError] = useState('')

  async function handleUpload(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!file) {
      return
    }
    setUploading(true)
    setError('')
    setResult(null)

    const formData = new FormData()
    formData.append('file', file)

    try {
      const report = await api<ImportResult>('/transactions/import', { method: 'POST', body: formData })
      setResult(report)
      setFile(null)
      setInputKey((key) => key + 1)
      onImported()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Upload failed')
    } finally {
      setUploading(false)
    }
  }

  const isPdf = file?.name.toLowerCase().endsWith('.pdf')

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="font-semibold text-slate-900">Upload a statement</h2>
      <p className="mt-1 text-sm text-slate-500">
        PDF or CSV from your bank. Only transaction descriptions and amounts are sent to an AI service to categorise
        them. Your name and account number never are.{' '}
        <Link to="/privacy" className="font-medium text-emerald-700 hover:underline">
          Learn more
        </Link>
      </p>

      <form onSubmit={handleUpload} className="mt-4 flex flex-wrap items-center gap-3">
        <input
          key={inputKey}
          type="file"
          accept=".csv,.pdf"
          onChange={(event) => setFile(event.target.files?.[0] ?? null)}
          className="text-sm file:mr-3 file:rounded-lg file:border-0 file:bg-slate-100 file:px-3 file:py-2 file:text-sm file:font-medium hover:file:bg-slate-200"
        />
        <button
          type="submit"
          disabled={!file || uploading}
          className="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
        >
          <Upload size={16} />
          {uploading ? 'Uploading…' : 'Upload'}
        </button>
      </form>

      {uploading && isPdf && (
        <p className="mt-3 text-sm text-slate-500">Reading your PDF statement. This can take a few seconds…</p>
      )}

      {error && (
        <p className="mt-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
      )}

      {result && (
        <div className="mt-4 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm">
          <p className="flex items-center gap-2 font-medium text-emerald-800">
            <CheckCircle2 size={16} />
            {result.imported} imported · {result.duplicates} duplicates skipped · {result.skipped} with problems
          </p>
          {result.errors.length > 0 && (
            <ul className="mt-2 space-y-1 text-slate-600">
              {result.errors.map((rowError) => (
                <li key={`${rowError.line}-${rowError.reason}`}>
                  Row {rowError.line}: {rowError.reason}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  )
}