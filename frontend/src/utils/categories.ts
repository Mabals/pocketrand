import type { Category } from '../types'

export const CATEGORY_LABELS: Record<Category, string> = {
  INCOME: 'Income',
  HOUSING: 'Housing',
  GROCERIES: 'Groceries',
  TRANSPORT: 'Transport',
  AIRTIME_DATA: 'Airtime and data',
  EATING_OUT: 'Eating out',
  ENTERTAINMENT: 'Entertainment',
  UTILITIES: 'Utilities',
  HEALTH: 'Health',
  EDUCATION: 'Education',
  SHOPPING: 'Shopping',
  TRANSFERS: 'Transfers',
  FEES: 'Fees',
  OTHER: 'Other',
}

export const CATEGORY_OPTIONS = Object.entries(CATEGORY_LABELS) as [Category, string][]