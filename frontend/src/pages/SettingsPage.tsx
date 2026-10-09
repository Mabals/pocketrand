import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router'
import { KeyRound, Trash2, UserRound } from 'lucide-react'
import { api, ApiError } from '../api/client'
import { useAuth } from '../auth/useAuth'
import { useDocumentTitle } from '../hooks/useDocumentTitle'
import TextField from '../components/TextField'

type Message = { type: 'success' | 'error'; text: string }

export default function SettingsPage() {
  useDocumentTitle('Settings')
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [passwordErrors, setPasswordErrors] = useState<Record<string, string>>({})
  const [passwordMessage, setPasswordMessage] = useState<Message | null>(null)
  const [savingPassword, setSavingPassword] = useState(false)

  const [deletePassword, setDeletePassword] = useState('')
  const [deleteError, setDeleteError] = useState('')
  const [deleting, setDeleting] = useState(false)

  async function handleChangePassword(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSavingPassword(true)
    setPasswordErrors({})
    setPasswordMessage(null)
    try {
      await api<void>('/account/password', {
        method: 'PUT',
        body: JSON.stringify({ currentPassword, newPassword }),
      })
      setCurrentPassword('')
      setNewPassword('')
      setPasswordMessage({ type: 'success', text: 'Your password has been changed.' })
    } catch (err) {
      if (err instanceof ApiError) {
        setPasswordErrors(err.fieldErrors)
        if (Object.keys(err.fieldErrors).length === 0) {
          setPasswordMessage({ type: 'error', text: err.message })
        }
      } else {
        setPasswordMessage({ type: 'error', text: 'Could not change your password' })
      }
    } finally {
      setSavingPassword(false)
    }
  }

  async function handleDelete(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const confirmed = window.confirm(
      'This permanently deletes your account, all your transactions and all your budgets. This cannot be undone. Continue?',
    )
    if (!confirmed) {
      return
    }
    setDeleting(true)
    setDeleteError('')
    try {
      await api<void>('/account', { method: 'DELETE', body: JSON.stringify({ password: deletePassword }) })
      logout()
      navigate('/login', {
        replace: true,
        state: { message: 'Your account and all your data have been deleted.' },
      })
    } catch (err) {
      setDeleteError(err instanceof ApiError ? err.message : 'Could not delete your account')
      setDeleting(false)
    }
  }

  return (
    <div className="max-w-2xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Settings</h1>
        <p className="mt-1 text-slate-500">Manage your account and your data.</p>
      </div>

      <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h2 className="flex items-center gap-2 font-semibold text-slate-900">
          <UserRound size={18} className="text-emerald-600" /> Your profile
        </h2>
        <dl className="mt-4 space-y-2 text-sm">
          <div className="flex justify-between gap-4">
            <dt className="text-slate-500">Name</dt>
            <dd className="font-medium text-slate-900">{user?.fullName}</dd>
          </div>
          <div className="flex justify-between gap-4">
            <dt className="text-slate-500">Email</dt>
            <dd className="font-medium text-slate-900">{user?.email}</dd>
          </div>
        </dl>
      </section>

      <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h2 className="flex items-center gap-2 font-semibold text-slate-900">
          <KeyRound size={18} className="text-emerald-600" /> Change password
        </h2>
        <form onSubmit={handleChangePassword} className="mt-4 space-y-4">
          {passwordMessage && (
            <p
              className={`rounded-lg border px-3 py-2 text-sm ${
                passwordMessage.type === 'success'
                  ? 'border-emerald-200 bg-emerald-50 text-emerald-800'
                  : 'border-red-200 bg-red-50 text-red-700'
              }`}
            >
              {passwordMessage.text}
            </p>
          )}
          <TextField
            label="Current password"
            type="password"
            value={currentPassword}
            onChange={setCurrentPassword}
            error={passwordErrors.currentPassword}
            autoComplete="current-password"
          />
          <TextField
            label="New password (at least 8 characters)"
            type="password"
            value={newPassword}
            onChange={setNewPassword}
            error={passwordErrors.newPassword}
            autoComplete="new-password"
          />
          <button
            type="submit"
            disabled={savingPassword}
            className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-emerald-700 disabled:opacity-50"
          >
            {savingPassword ? 'Saving…' : 'Change password'}
          </button>
        </form>
      </section>

      <section className="rounded-2xl border border-red-200 bg-white p-5 shadow-sm">
        <h2 className="flex items-center gap-2 font-semibold text-red-700">
          <Trash2 size={18} /> Delete account
        </h2>
        <p className="mt-1 text-sm text-slate-600">
          Permanently deletes your account, transactions and budgets. This cannot be undone.
        </p>
        <form onSubmit={handleDelete} className="mt-4 space-y-4">
          {deleteError && (
            <p className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{deleteError}</p>
          )}
          <TextField
            label="Enter your password to confirm"
            type="password"
            value={deletePassword}
            onChange={setDeletePassword}
            autoComplete="current-password"
          />
          <button
            type="submit"
            disabled={deleting}
            className="rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-red-700 disabled:opacity-50"
          >
            {deleting ? 'Deleting…' : 'Delete my account'}
          </button>
        </form>
      </section>
    </div>
  )
}