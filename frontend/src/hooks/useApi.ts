import { useEffect, useState } from 'react'
import { api } from '../api/client'

type Result<T> = {
  path: string
  data?: T
  error?: string
}

export function useApi<T>(path: string | null) {
  const [result, setResult] = useState<Result<T> | null>(null)
  const [reloadCount, setReloadCount] = useState(0)

  useEffect(() => {
    if (!path) {
      return
    }
    let cancelled = false
    api<T>(path)
      .then((data) => {
        if (!cancelled) setResult({ path, data })
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          setResult({ path, error: err instanceof Error ? err.message : 'Something went wrong' })
        }
      })
    return () => {
      cancelled = true
    }
  }, [path, reloadCount])

  const current = result?.path === path ? result : null

  return {
    data: current?.data,
    error: current?.error,
    loading: path !== null && current === null,
    reload: () => setReloadCount((count) => count + 1),
  }
}