export type Account = {
  id: number
  accountNumber: string
  customerId: number
  accountHolder: string
  email: string
  balance: number
  createdAt: string
}

export type Transaction = {
  id: number
  accountNumber: string
  type: 'DEPOSIT' | 'WITHDRAW' | 'TRANSFER_IN' | 'TRANSFER_OUT'
  amount: number
  balanceAfter: number
  referenceNumber: string
  remarks: string | null
  createdAt: string
}

async function api<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options?.headers },
  })
  if (!response.ok) {
    const error = (await response.json().catch(() => ({}))) as { message?: string }
    throw new Error(error.message ?? 'The request could not be completed.')
  }
  return response.json() as Promise<T>
}

const post = <T>(url: string, body: unknown) => api<T>(url, { method: 'POST', body: JSON.stringify(body) })

export const getBalance = (accountNumber: string) => api<Account>(`/api/accounts/${accountNumber}/balance`)
export const getHistory = (accountNumber: string) => api<Transaction[]>(`/api/accounts/${accountNumber}/transactions`)
export const deposit = (accountNumber: string, amount: number, remarks: string) => post<Transaction>(`/api/accounts/${accountNumber}/deposit`, { amount, remarks })
export const withdraw = (accountNumber: string, amount: number, remarks: string) => post<Transaction>(`/api/accounts/${accountNumber}/withdraw`, { amount, remarks })
export const transfer = (accountNumber: string, destinationAccountNumber: string, amount: number, remarks: string) => post<Transaction[]>(`/api/accounts/${accountNumber}/transfer`, { destinationAccountNumber, amount, remarks })
export const getAccounts = () => api<Account[]>('/api/admin/accounts')
export const getTransactions = () => api<Transaction[]>('/api/admin/transactions')
