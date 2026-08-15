import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { DashboardLayout } from '../components/layout/DashboardLayout'
import { getAccounts, getTransactions, type Account, type Transaction } from '../features/banking/api/bankingApi'

const money = new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' })

export function AdminPage() {
  const user = JSON.parse(sessionStorage.getItem('bankingUser') ?? 'null') as { role?: string } | null
  const [accounts, setAccounts] = useState<Account[]>([])
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [error, setError] = useState('')
  useEffect(() => { Promise.all([getAccounts(), getTransactions()]).then(([a, t]) => { setAccounts(a); setTransactions(t) }).catch((reason: Error) => setError(reason.message)) }, [])
  if (user?.role !== 'ADMIN') return <Navigate to="/banking" replace />
  return <DashboardLayout title="Administration">
    <h1 className="text-3xl font-bold">Administration</h1><p className="mt-2 text-slate-500">Review all bank accounts and ledger activity.</p>
    {error && <p className="mt-6 rounded-xl bg-red-50 p-4 text-red-700">{error}</p>}
    <div className="mt-8 grid gap-4 sm:grid-cols-3"><Stat label="Total accounts" value={String(accounts.length)} /><Stat label="Total balance" value={money.format(accounts.reduce((sum, item) => sum + Number(item.balance), 0))} /><Stat label="Transactions" value={String(transactions.length)} /></div>
    <Table title="Accounts" headers={['Account', 'Customer', 'Email', 'Balance']} rows={accounts.map((a) => [a.accountNumber, a.accountHolder, a.email, money.format(a.balance)])} />
    <Table title="Transactions" headers={['Date', 'Account', 'Type', 'Reference', 'Amount']} rows={transactions.map((t) => [new Date(t.createdAt).toLocaleString(), t.accountNumber, t.type.replace('_', ' '), t.referenceNumber, money.format(t.amount)])} />
  </DashboardLayout>
}

function Stat({ label, value }: { label: string; value: string }) { return <div className="rounded-2xl border border-slate-200 bg-white p-6"><p className="text-sm text-slate-500">{label}</p><p className="mt-2 text-2xl font-bold">{value}</p></div> }
function Table({ title, headers, rows }: { title: string; headers: string[]; rows: string[][] }) { return <section className="mt-8 overflow-hidden rounded-2xl border border-slate-200 bg-white"><h2 className="border-b border-slate-200 px-6 py-5 text-xl font-bold">{title}</h2><div className="overflow-x-auto"><table className="w-full text-left text-sm"><thead className="bg-slate-50 text-slate-500"><tr>{headers.map((h) => <th className="px-6 py-3" key={h}>{h}</th>)}</tr></thead><tbody>{rows.map((row, index) => <tr className="border-t border-slate-100" key={index}>{row.map((cell, cellIndex) => <td className="px-6 py-4" key={cellIndex}>{cell}</td>)}</tr>)}</tbody></table></div></section> }
