const numberFormatter = new Intl.NumberFormat('en-US', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

export function formatRands(amount: number): string {
  const formatted = numberFormatter.format(Math.abs(amount))
  return amount < 0 ? `-R${formatted}` : `R${formatted}`
}

export function formatMonth(month: string): string {
  const [year, monthNumber] = month.split('-').map(Number)
  return new Date(year, monthNumber - 1).toLocaleDateString('en-ZA', { month: 'long', year: 'numeric' })
}

export function formatDate(date: string): string {
  return new Date(date).toLocaleDateString('en-ZA', { day: 'numeric', month: 'short' })
}

export function currentMonth(): string {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}