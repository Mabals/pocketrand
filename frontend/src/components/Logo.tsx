type LogoProps = {
  light?: boolean
}

export default function Logo({ light = false }: LogoProps) {
  return (
    <span className="inline-flex items-center gap-2">
      <svg viewBox="0 0 32 32" className="h-8 w-8" aria-hidden="true">
        <rect width="32" height="32" rx="9" className="fill-emerald-600" />
        <path
          d="M10 23V9h6a4.5 4.5 0 0 1 0 9h-6M15.5 18l6 5"
          fill="none"
          stroke="white"
          strokeWidth="2.6"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
      <span className={`text-lg font-extrabold tracking-tight ${light ? 'text-white' : 'text-slate-900'}`}>
        Pocket<span className={light ? 'text-emerald-300' : 'text-emerald-600'}>Rand</span>
      </span>
    </span>
  )
}