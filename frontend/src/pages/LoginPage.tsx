import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import { useAuth } from '../auth/useAuth'
import { ApiError } from '../api/client'
import AuthCard from '../components/AuthCard'
import TextField from '../components/TextField'


export default function LoginPage() {
  const { user, login, loginDemo } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const notice = (location.state as { message?: string } | null)?.message
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [startingDemo, setStartingDemo] = useState(false)

  if (user) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login(email, password)
      navigate('/')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Something went wrong')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDemo() {
    setError('')
    setStartingDemo(true)
    try {
      await loginDemo()
      navigate('/')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not start the demo')
    } finally {
      setStartingDemo(false)
    }
  }

  return (
    <AuthCard title="Welcome back" subtitle="Log in to see where your money goes.">
      <form onSubmit={handleSubmit} className="space-y-4">
        {notice && !error && (
          <p className="rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-800">{notice}</p>
        )}
        {error && (
          <p className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
        )}
        <TextField label="Email" type="email" value={email} onChange={setEmail} autoComplete="email" />
        <TextField
          label="Password"
          type="password"
          value={password}
          onChange={setPassword}
          autoComplete="current-password"
        />
        <div className="-mt-2 text-right">
          <Link to="/forgot-password" className="text-sm font-medium text-emerald-700 hover:underline">
            Forgot password?
          </Link>
        </div>
        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-lg bg-emerald-600 py-2.5 font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
        >
          {submitting ? 'Logging in…' : 'Log in'}
        </button>
        <p className="text-center text-sm text-slate-600">
          No account yet?{' '}
          <Link to="/register" className="font-medium text-emerald-700 hover:underline">
            Create one
          </Link>
        </p>
      </form>
      <div className="my-6 flex items-center gap-3 text-xs text-slate-400">
        <span className="h-px flex-1 bg-slate-200" />
        or
        <span className="h-px flex-1 bg-slate-200" />
      </div>
      <button
        type="button"
        onClick={handleDemo}
        disabled={startingDemo}
        className="w-full rounded-lg border border-emerald-600 py-2.5 font-medium text-emerald-700 transition hover:bg-emerald-50 disabled:opacity-60"
      >
        {startingDemo ? 'Setting up your demo…' : 'Try the demo, no sign-up needed'}
      </button>
            <p className="mt-2 text-center text-xs text-slate-400">
        By trying the demo you accept the{' '}
        <Link to="/privacy" className="underline hover:text-slate-600">
          privacy notice
        </Link>
        .
      </p>
    </AuthCard>
  )
}