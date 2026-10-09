import { useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import { api, ApiError } from '../api/client'
import { useDocumentTitle } from '../hooks/useDocumentTitle'
import AuthCard from '../components/AuthCard'
import TextField from '../components/TextField'

export default function ForgotPasswordPage() {
  useDocumentTitle('Forgot password')
  const [email, setEmail] = useState('')
  const [sentMessage, setSentMessage] = useState('')
  const [error, setError] = useState('')
  const [sending, setSending] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSending(true)
    setError('')
    try {
      const result = await api<{ message: string }>('/auth/forgot-password', {
        method: 'POST',
        body: JSON.stringify({ email }),
      })
      setSentMessage(result.message)
    } catch (err) {
      setError(err instanceof ApiError ? (err.fieldErrors.email ?? err.message) : 'Something went wrong')
    } finally {
      setSending(false)
    }
  }

  return (
    <AuthCard title="Forgot your password?" subtitle="Enter your email and we'll send you a reset link.">
      {sentMessage ? (
        <div className="space-y-4">
          <p className="rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-800">
            {sentMessage}
          </p>
          <p className="text-sm text-slate-600">The link works for 30 minutes. Check your spam folder too.</p>
          <Link to="/login" className="inline-block text-sm font-medium text-emerald-700 hover:underline">
            Back to log in
          </Link>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <p className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
          )}
          <TextField label="Email" type="email" value={email} onChange={setEmail} autoComplete="email" />
          <button
            type="submit"
            disabled={sending}
            className="w-full rounded-lg bg-emerald-600 py-2.5 font-medium text-white transition hover:bg-emerald-700 disabled:opacity-60"
          >
            {sending ? 'Sending…' : 'Send reset link'}
          </button>
          <p className="text-center text-sm text-slate-600">
            Remembered it?{' '}
            <Link to="/login" className="font-medium text-emerald-700 hover:underline">
              Log in
            </Link>
          </p>
        </form>
      )}
    </AuthCard>
  )
}