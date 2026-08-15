import { useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { FormField, inputClassName } from '../../../components/forms/FormField'
import { SubmitButton } from '../../../components/forms/SubmitButton'
import { login, type LoginResponse } from '../api/authApi'

export function LoginForm() {
  const location = useLocation()
  const navigate = useNavigate()
  const registrationMessage = (location.state as { message?: string } | null)?.message
  const [error, setError] = useState('')
  const [user, setUser] = useState<LoginResponse | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setUser(null)
    const form = new FormData(event.currentTarget)
    setSubmitting(true)
    try {
      const authenticatedUser = await login({
        email: String(form.get('email') ?? ''),
        password: String(form.get('password') ?? ''),
      })
      setUser(authenticatedUser)
      sessionStorage.setItem('bankingUser', JSON.stringify(authenticatedUser))
      navigate(authenticatedUser.role === 'ADMIN' ? '/admin' : '/banking', { replace: true })
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Sign in failed.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="space-y-5" onSubmit={handleSubmit}>
      {registrationMessage && !user && <p className="rounded-xl bg-emerald-50 p-3 text-sm text-emerald-700" role="status">{registrationMessage}</p>}
      {error && <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
      {user && <p className="rounded-xl bg-emerald-50 p-3 text-sm text-emerald-700" role="status">Welcome, {user.fullName}. Account {user.accountNumber ?? 'pending'} is ready.</p>}
      <FormField id="login-email" label="Email address" name="email" type="email" autoComplete="email" placeholder="name@example.com" />
      <div>
        <div className="mb-2 flex items-center justify-between gap-4">
          <label className="text-sm font-medium text-slate-700" htmlFor="login-password">Password</label>
          <button className="text-sm font-semibold text-blue-700 hover:text-blue-900" type="button">Forgot password?</button>
        </div>
        <input className={inputClassName} id="login-password" name="password" type="password" autoComplete="current-password" placeholder="Enter your password" required />
      </div>
      <label className="flex cursor-pointer items-center gap-3 text-sm text-slate-600">
        <input className="size-4 rounded border-slate-300 accent-blue-700" name="rememberMe" type="checkbox" />
        Keep me signed in
      </label>
      <SubmitButton disabled={submitting}>{submitting ? 'Signing in…' : 'Sign in'}</SubmitButton>
    </form>
  )
}
