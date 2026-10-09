import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router'
import { api, ApiError } from '../api/client'
import { useDocumentTitle } from '../hooks/useDocumentTitle'
import AuthCard from '../components/AuthCard'
import TextField from '../components/TextField'

export default function ResetPasswordPage() {
  useDocumentTitle('Reset password')
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const navigate = useNavigate()

  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  if (!token) {
    return (
      <AuthCard title="Link incomplete" subtitle="This reset link is missing its code.">
        <Link to="/forgot-password" className="text-sm font-medium text-emerald-700 hover:underline">
          Request a new reset link
        </Link>
      </AuthCard>
    )
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setFieldErrors({})
    if (newPassword !== confirmPassword) {
      setFieldErrors({ confirmPassword: 'The passwords do not match' })
      return
    }
    setSaving(true)
    try {
      await api<void>('/auth/reset-password', {
        method: 'POST',
        body: JSON.stringify({ token, newPassword }),
      })
      navigate('/login', { replace: true, state: { message: 'Your password has been changed. You can log in now.' } })
    } catch (err) {
      if (err instanceof ApiError) {
        setFieldErrors(err.fieldErrors)
        if (Object.keys(err.fieldErrors).length === 0) {
          setError(err.message)
        }
      } else {
        setError('Something went wrong')
      }
      setSaving(false)
    }
  }

  return (
    <AuthCard title="Choose a new password" subtitle="Make it at least 8 characters.">
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}{' '}
            <Link to="/forgot-password" className="font-medium underline">
              Request a new link
            </Link>
          </div>
        )}
        <TextField
          label="New password"
          type="password"
          value={newPassword}
          onChange={setNewPassword}
          error={fieldErrors.newPassword}
          autoComplete="new-password"
        />
        <TextField
          label="Confirm new password"
          type="password"
          value={confirmPassword}
          onChange={setConfirmPassword}
          error={fieldErrors.confirmPassword}
          autoComplete="new-password"
        />
        <button
          type="submit"
          disabled={saving}
          className="w-full rounded-lg bg-emerald-600 py-2.5 font-medium text-white transition hover:bg-emerald-700 disabled:opacity-60"
        >
          {saving ? 'Saving…' : 'Set new password'}
        </button>
      </form>
    </AuthCard>
  )
}