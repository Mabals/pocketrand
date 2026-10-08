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

