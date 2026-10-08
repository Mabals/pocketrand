import type { Category } from '../types'
import { CATEGORY_STYLES } from '../utils/categoryStyles'

type CategoryIconProps = {
  category: Category
  size?: number
}

export default function CategoryIcon({ category, size = 16 }: CategoryIconProps) {
  const { color, icon: Icon } = CATEGORY_STYLES[category]
  return (
    <span
      className="inline-flex shrink-0 items-center justify-center rounded-lg p-1.5"
      style={{ backgroundColor: `${color}1a`, color }}
    >
      <Icon size={size} />
    </span>
  )
}