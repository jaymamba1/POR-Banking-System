import type { ReactNode } from 'react'

type SubmitButtonProps = {
  children: ReactNode
  disabled?: boolean
}

export function SubmitButton({ children, disabled = false }: SubmitButtonProps) {
  return (
    <button
      className="w-full rounded-xl bg-blue-800 px-4 py-3 font-semibold text-white shadow-sm transition hover:bg-blue-900 focus:ring-4 focus:ring-blue-200 focus:outline-none disabled:cursor-not-allowed disabled:opacity-60"
      disabled={disabled}
      type="submit"
    >
      {children}
    </button>
  )
}
