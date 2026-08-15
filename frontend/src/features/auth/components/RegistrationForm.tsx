import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { FormField } from '../../../components/forms/FormField'
import { SubmitButton } from '../../../components/forms/SubmitButton'
import { register } from '../api/authApi'

export function RegistrationForm() {
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  const latestBirthDate = yesterday.toISOString().slice(0, 10)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    const form = new FormData(event.currentTarget)
    const password = String(form.get('password') ?? '')

    if (password !== String(form.get('confirmPassword') ?? '')) {
      setError('Passwords do not match.')
      return
    }

    setSubmitting(true)
    try {
      await register({
        firstName: String(form.get('firstName') ?? ''),
        lastName: String(form.get('lastName') ?? ''),
        email: String(form.get('email') ?? ''),
        phoneNumber: String(form.get('phoneNumber') ?? ''),
        dateOfBirth: String(form.get('dateOfBirth') ?? ''),
        password,
        acceptedTerms: form.get('acceptedTerms') === 'on',
      })
      navigate('/login', {
        replace: true,
        state: { message: 'Account created successfully. You can now sign in.' },
      })
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Registration failed.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="space-y-5" onSubmit={handleSubmit}>
      {error && <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700" role="alert">{error}</p>}
      <div className="grid gap-5 sm:grid-cols-2">
        <FormField id="first-name" label="First name" name="firstName" autoComplete="given-name" placeholder="Juan" />
        <FormField id="last-name" label="Last name" name="lastName" autoComplete="family-name" placeholder="Dela Cruz" />
      </div>
      <FormField id="registration-email" label="Email address" name="email" type="email" autoComplete="email" placeholder="name@example.com" />
      <FormField id="phone-number" label="Phone number" name="phoneNumber" type="tel" autoComplete="tel" placeholder="09XX XXX XXXX" />
      <FormField id="date-of-birth" label="Date of birth" name="dateOfBirth" type="date" autoComplete="bday" max={latestBirthDate} />
      <div className="grid gap-5 sm:grid-cols-2">
        <FormField id="new-password" label="Password" name="password" type="password" autoComplete="new-password" placeholder="At least 8 characters" minLength={8} />
        <FormField id="confirm-password" label="Confirm password" name="confirmPassword" type="password" autoComplete="new-password" placeholder="Repeat password" minLength={8} />
      </div>
      <label className="flex cursor-pointer items-start gap-3 text-sm leading-6 text-slate-600">
        <input className="mt-1 size-4 shrink-0 rounded border-slate-300 accent-blue-700" name="acceptedTerms" type="checkbox" required />
        I agree to the terms of service and privacy policy.
      </label>
      <SubmitButton disabled={submitting}>{submitting ? 'Creating account…' : 'Create account'}</SubmitButton>
    </form>
  )
}
