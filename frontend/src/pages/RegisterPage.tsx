import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router'
import { useAuth } from '../auth/useAuth'
import { ApiError } from '../api/client'
import AuthCard from '../components/AuthCard'
import TextField from '../components/TextField'

export default function RegisterPage() {
  const { user, register } = useAuth()
  const navigate = useNavigate()
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)

  if (user) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setFieldErrors({})
    setSubmitting(true)
    try {
      await register(fullName, email, password)
      navigate('/')
    } catch (err) {
      if (err instanceof ApiError) {
        const hasFieldErrors = Object.keys(err.fieldErrors).length > 0
        setError(hasFieldErrors ? 'Please fix the highlighted fields.' : err.message)
        setFieldErrors(err.fieldErrors)
      } else {
        setError('Something went wrong')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthCard title="Create your account" subtitle="Start tracking your spending in minutes.">
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <p className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
        )}
        <TextField
          label="Full name"
          value={fullName}
          onChange={setFullName}
          error={fieldErrors.fullName}
          autoComplete="name"
        />
        <TextField
          label="Email"
          type="email"
          value={email}
          onChange={setEmail}
          error={fieldErrors.email}
          autoComplete="email"
        />
        <TextField
          label="Password (at least 8 characters)"
          type="password"
          value={password}
          onChange={setPassword}
          error={fieldErrors.password}
          autoComplete="new-password"
        />
        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-lg bg-emerald-600 py-2.5 font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
        >
          {submitting ? 'Creating account…' : 'Create account'}
        </button>
        <p className="text-center text-sm text-slate-600">
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-emerald-700 hover:underline">
            Log in
          </Link>
        </p>
      </form>
    </AuthCard>
  )
}