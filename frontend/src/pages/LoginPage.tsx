import { Link } from 'react-router-dom'
import { AuthLayout } from '../components/layout/AuthLayout'
import { LoginForm } from '../features/auth/components/LoginForm'

export function LoginPage() {
  return (
    <AuthLayout
      bannerTitle="Kapag maayos ang pondo, tuloy-tuloy ang trabaho."
      eyebrow="Welcome back"
      title="Sign in to your account"
      description="Enter your credentials to access the banking system."
      footer={
        <p className="mt-7 text-center text-sm text-slate-500">
          Don&apos;t have an account?{' '}
          <Link className="font-semibold text-blue-700 hover:text-blue-900" to="/register">
            Create account
          </Link>
        </p>
      }
    >
      <LoginForm />
    </AuthLayout>
  )
}
