import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { Navigate } from 'react-router-dom'
import { DashboardLayout } from '../components/layout/DashboardLayout'
import { deposit, getBalance, getHistory, transfer, withdraw, type Account, type Transaction } from '../features/banking/api/bankingApi'
import type { LoginResponse } from '../features/auth/api/authApi'

const money = new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' })

export function BankingPage() {
  const user = JSON.parse(sessionStorage.getItem('bankingUser') ?? 'null') as LoginResponse | null
  const [account, setAccount] = useState<Account | null>(null)
  const [history, setHistory] = useState<Transaction[]>([])
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const refresh = useCallback(async () => {
    if (!user?.accountNumber) return
    const [nextAccount, nextHistory] = await Promise.all([
      getBalance(user.accountNumber),
      getHistory(user.accountNumber),
    ])
    setAccount(nextAccount)
    setHistory(nextHistory)
  }, [user?.accountNumber])

  useEffect(() => { refresh().catch((reason: Error) => setError(reason.message)) }, [refresh])
  if (!user?.accountNumber) return <Navigate to="/login" replace />

  async function submit(kind: 'deposit' | 'withdraw' | 'transfer', event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(''); setMessage(''); setBusy(true)
    const formElement = event.currentTarget
    const form = new FormData(formElement)
    const amount = Number(form.get('amount'))
    const remarks = ''
    try {
      if (kind === 'deposit') await deposit(user!.accountNumber!, amount, remarks)
      if (kind === 'withdraw') await withdraw(user!.accountNumber!, amount, remarks)
      if (kind === 'transfer') await transfer(user!.accountNumber!, String(form.get('destinationAccountNumber') ?? ''), amount, remarks)
      setMessage(`${kind[0].toUpperCase()}${kind.slice(1)} completed successfully.`)
      formElement.reset()
      await refresh()
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'Operation failed.') }
    finally { setBusy(false) }
  }

  return <DashboardLayout title="Customer banking">
    <div className="mb-8"><p className="text-sm font-semibold text-blue-700">Welcome, {user.fullName}</p><h1 className="mt-1 text-3xl font-bold">Manage your account</h1></div>
    {message && <p className="mb-6 rounded-xl bg-emerald-50 p-4 text-emerald-700">{message}</p>}
    {error && <p className="mb-6 rounded-xl bg-red-50 p-4 text-red-700">{error}</p>}
    <section className="mb-8 rounded-2xl bg-blue-950 p-7 text-white shadow-sm"><p className="text-sm text-blue-200">Available balance</p><p className="mt-2 text-4xl font-bold">{account ? money.format(account.balance) : 'Loading…'}</p><p className="mt-4 text-sm text-blue-200">{user.accountNumber}</p></section>
    <section className="grid gap-6 lg:grid-cols-3">
      <MoneyForm title="Deposit" action="Add funds" busy={busy} onSubmit={(event) => submit('deposit', event)} />
      <MoneyForm title="Withdraw" action="Withdraw funds" busy={busy} onSubmit={(event) => submit('withdraw', event)} />
      <MoneyForm title="Transfer" action="Send funds" busy={busy} transfer busyLabel="Processing…" onSubmit={(event) => submit('transfer', event)} />
    </section>
    <section className="mt-10 overflow-hidden rounded-2xl border border-slate-200 bg-white"><div className="border-b border-slate-200 px-6 py-5"><h2 className="text-xl font-bold">Transaction history</h2></div><div className="overflow-x-auto"><table className="w-full text-left text-sm"><thead className="bg-slate-50 text-slate-500"><tr><th className="px-6 py-3">Date</th><th>Type</th><th>Reference</th><th>Amount</th><th>Balance</th></tr></thead><tbody>{history.map((item) => <tr className="border-t border-slate-100" key={item.id}><td className="px-6 py-4">{new Date(item.createdAt).toLocaleString()}</td><td>{item.type.replace('_', ' ')}</td><td>{item.referenceNumber}</td><td>{money.format(item.amount)}</td><td>{money.format(item.balanceAfter)}</td></tr>)}</tbody></table></div></section>
  </DashboardLayout>
}

type MoneyFormProps = { title: string; action: string; busy: boolean; transfer?: boolean; busyLabel?: string; onSubmit: (event: FormEvent<HTMLFormElement>) => void }
function MoneyForm({ title, action, busy, transfer: isTransfer = false, busyLabel = 'Processing…', onSubmit }: MoneyFormProps) {
  const field = 'w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100'
  return <form className="space-y-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm" onSubmit={onSubmit}><h2 className="text-xl font-bold">{title}</h2>{isTransfer && <input className={field} name="destinationAccountNumber" placeholder="Destination account number" required />}<input className={field} name="amount" type="number" min="0.01" step="0.01" placeholder="Amount" required /><button className="w-full rounded-xl bg-blue-800 px-4 py-3 font-semibold text-white disabled:opacity-60" disabled={busy}>{busy ? busyLabel : action}</button></form>
}
