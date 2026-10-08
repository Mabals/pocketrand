type MonthPickerProps = {
  value: string
  onChange: (month: string) => void
}

export default function MonthPicker({ value, onChange }: MonthPickerProps) {
  return (
    <label className="text-sm">
      <span className="mb-1 block font-medium text-slate-700">Month</span>
      <input
        type="month"
        value={value}
        onChange={(event) => event.target.value && onChange(event.target.value)}
        className="rounded-lg border border-slate-300 bg-white px-3 py-2"
      />
    </label>
  )
}