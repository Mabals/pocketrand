import { formatRands } from '../../utils/format'

type MoneyHeroProps = {
  income: number
  expenses: number
  net: number
  changePercent: number | null
}

export default function MoneyHero({ income, expenses, net, changePercent }: MoneyHeroProps) {
  const spentShare = income > 0 ? Math.min((expenses / income) * 100, 100) : 100

  return (
    <div
      className="relative h-full overflow-hidden rounded-3xl p-6 text-white shadow-lg"
      style={{ background: 'linear-gradient(135deg, #047857 0%, #064e3b 100%)' }}
    >
      <div className="absolute -right-12 -top-12 h-44 w-44 rounded-full bg-white/10" />
      <div className="absolute -bottom-20 right-28 h-48 w-48 rounded-full bg-white/5" />

      <div className="relative">
        <p className="text-sm text-emerald-100">{net >= 0 ? 'Left over this month' : 'Overspent this month'}</p>
        <p className="mt-1 text-4xl font-extrabold tracking-tight">{formatRands(net)}</p>

        <div className="mt-6 grid grid-cols-2 gap-4">
          <div>
            <p className="text-sm text-emerald-200">Money in</p>
            <p className="text-lg font-semibold">{formatRands(income)}</p>
          </div>
          <div>
            <p className="text-sm text-emerald-200">Money out</p>
            <p className="text-lg font-semibold">{formatRands(expenses)}</p>
          </div>
        </div>

        <div className="mt-6">
          <div className="flex flex-wrap justify-between gap-2 text-xs text-emerald-100">
            <span>
              {income > 0 ? `You've spent ${Math.round(spentShare)}% of your income` : 'No income recorded this month'}
            </span>
            {changePercent !== null && (
              <span>
                {changePercent > 0 ? '▲' : '▼'} {Math.abs(changePercent)}% vs last month
              </span>
            )}
          </div>
          <div className="mt-2 h-2 overflow-hidden rounded-full bg-white/20">
            <div className="h-full rounded-full bg-white" style={{ width: `${spentShare}%` }} />
          </div>
        </div>
      </div>
    </div>
  )
}
