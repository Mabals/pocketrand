import { useAuth } from '../auth/useAuth'

export default function DashboardPage() {
  const { user } = useAuth()
  const firstName = user?.fullName.split(' ')[0]

  return (
    <div>
      <h1 className="text-2xl font-bold text-slate-900">Hi {firstName} 👋</h1>
      <p className="mt-1 text-slate-500">Your monthly summary will appear here in Part C.</p>
    </div>
  )
}