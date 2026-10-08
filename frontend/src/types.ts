export type User = {
  id: number
  fullName: string
  email: string
}

export type AuthResponse = {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
  user: User
}

export type Category =
  | 'INCOME'
  | 'HOUSING'
  | 'GROCERIES'
  | 'TRANSPORT'
  | 'AIRTIME_DATA'
  | 'EATING_OUT'
  | 'ENTERTAINMENT'
  | 'UTILITIES'
  | 'HEALTH'
  | 'EDUCATION'
  | 'SHOPPING'
  | 'TRANSFERS'
  | 'FEES'
  | 'OTHER'

export type CategorySource = 'RULE' | 'AI' | 'USER'

export type Transaction = {
  id: number
  date: string
  description: string
  amount: number
  category: Category
  categoryLabel: string
  categorySource: CategorySource
}

export type CategoryTotal = {
  category: Category
  label: string
  amount: number
  percentage: number
}

export type MonthlySummary = {
  month: string
  income: number
  expenses: number
  net: number
  transactionCount: number
  spendingByCategory: CategoryTotal[]
  topExpenses: Transaction[]
  previousMonthExpenses: number
  expenseChangePercent: number | null
}

export type SpendingTips = {
  month: string
  tips: string[]
  aiGenerated: boolean
}

export type ImportResult = {
  imported: number
  skipped: number
  duplicates: number
  errors: { line: number; reason: string }[]
}

export type BudgetStatus = 'ON_TRACK' | 'NEAR_LIMIT' | 'OVER_LIMIT'

export type Budget = {
  category: Category
  label: string
  monthlyLimit: number
  spent: number
  remaining: number
  percentUsed: number
  status: BudgetStatus
}

export type SalaryPeriod = 'MONTHLY' | 'ANNUAL'

export type TaxEstimate = {
  taxYear: number
  annualGross: number
  monthlyGross: number
  annualTaxBeforeRebates: number
  rebates: number
  annualTax: number
  monthlyTax: number
  monthlyUif: number
  monthlyTakeHome: number
  annualTakeHome: number
  effectiveRatePercent: number
  marginalRatePercent: number
  note: string
}

