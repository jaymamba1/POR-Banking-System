import type { InputHTMLAttributes } from 'react'

type FormFieldProps = {
  id: string
  label: string
} & Omit<InputHTMLAttributes<HTMLInputElement>, 'id'>

export const inputClassName =
  'w-full rounded-xl border border-slate-300 bg-white px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-blue-600 focus:ring-4 focus:ring-blue-100'

export function FormField({ id, label, type = 'text', ...inputProps }: FormFieldProps) {
  return (
    <div>
      <label className="mb-2 block text-sm font-medium text-slate-700" htmlFor={id}>
        {label}
      </label>
      <input
        className={inputClassName}
        id={id}
        type={type}
        required
        {...inputProps}
      />
    </div>
  )
}
