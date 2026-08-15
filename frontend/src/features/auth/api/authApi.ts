export type ApiError = {
  message?: string
  fieldErrors?: Record<string, string>
}

export type RegistrationData = {
  firstName: string
  lastName: string
  email: string
  phoneNumber: string
  dateOfBirth: string
  password: string
  acceptedTerms: boolean
}

export type LoginData = {
  email: string
  password: string
}

export type LoginResponse = {
  customerId: number
  fullName: string
  email: string
  role: string
  accountNumber: string | null
}

async function request<T>(url: string, body: unknown): Promise<T> {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })

  if (!response.ok) {
    const error = (await response.json().catch(() => ({}))) as ApiError
    const fieldMessage = error.fieldErrors
      ? Object.entries(error.fieldErrors)
          .map(([field, message]) => `${fieldLabel(field)}: ${message}`)
          .join(' ')
      : ''
    throw new Error(fieldMessage || error.message || 'Unable to complete your request. Please try again.')
  }

  return response.json() as Promise<T>
}

function fieldLabel(field: string) {
  const labels: Record<string, string> = {
    firstName: 'First name',
    lastName: 'Last name',
    email: 'Email address',
    phoneNumber: 'Phone number',
    dateOfBirth: 'Date of birth',
    password: 'Password',
    acceptedTerms: 'Terms',
    openingDeposit: 'Opening deposit',
  }
  return labels[field] ?? field
}

export function register(data: RegistrationData) {
  return request('/api/accounts', data)
}

export function login(data: LoginData) {
  return request<LoginResponse>('/api/auth/login', data)
}
