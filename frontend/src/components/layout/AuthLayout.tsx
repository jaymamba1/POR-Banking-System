import type { ReactNode } from 'react'

type AuthLayoutProps = {
  bannerTitle: string
  eyebrow: string
  title: string
  description: string
  children: ReactNode
  footer: ReactNode
}

export function AuthLayout({
  bannerTitle,
  eyebrow,
  title,
  description,
  children,
  footer,
}: AuthLayoutProps) {
  return (
    <main className="min-h-screen bg-white">
      <div className="grid min-h-screen w-full bg-white lg:grid-cols-2">
        <section className="flex min-h-64 flex-col justify-between bg-white p-8 text-slate-900 sm:p-12 lg:p-16">
          <div className="flex items-center gap-3">
            <div className="grid size-11 place-items-center rounded-xl bg-blue-900 text-lg font-bold text-white">
              POR
            </div>
            <div>
              <p className="font-semibold">POR Banking System</p>
              <p className="text-xs text-slate-500">Palaging Overtime si Rodney</p>
            </div>
          </div>

          <div className="my-12 max-w-md lg:my-0">
            <p className="mb-4 text-sm font-semibold tracking-[0.18em] text-blue-700 uppercase">
              Palaging Overtime si Rodney
            </p>
            <h1 className="text-4xl leading-tight font-bold tracking-tight sm:text-5xl">
              {bannerTitle}
            </h1>
            <p className="mt-6 leading-7 text-slate-600">
              Kung walang resibo, baka drawing lang ang budget.
            </p>
          </div>

          <p className="text-xs text-slate-400">
            © 2026 POR Banking System. All rights reserved.
          </p>
        </section>

        <section className="flex items-center justify-center p-8 sm:p-12 lg:p-16">
          <div className="w-full max-w-md">
            <header className="mb-8">
              <p className="mb-2 text-sm font-semibold text-blue-700">{eyebrow}</p>
              <h2 className="text-3xl font-bold tracking-tight text-slate-900">
                {title}
              </h2>
              <p className="mt-3 text-sm leading-6 text-slate-500">{description}</p>
            </header>

            {children}
            {footer}

            <p className="mt-6 text-center text-xs leading-5 text-slate-400">
              Authorized personnel only. Activity may be monitored for security.
            </p>
          </div>
        </section>
      </div>
    </main>
  )
}
