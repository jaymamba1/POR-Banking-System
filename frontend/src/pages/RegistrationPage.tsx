import { Link } from 'react-router-dom'
import { AuthLayout } from '../components/layout/AuthLayout'
import { RegistrationForm } from '../features/auth/components/RegistrationForm'

export function RegistrationPage() {
  return (
    <AuthLayout
      bannerTitle="Kapag maayos ang pondo, tuloy-tuloy ang trabaho."
      eyebrow="Join POR Banking"
      title="Create your account"
      description="Provide your information to create a secure banking profile."
      footer={
        <p className="mt-7 text-center text-sm text-slate-500">
          Already have an account?{' '}
          <Link className="font-semibold text-blue-700 hover:text-blue-900" to="/login">
            Sign in
          </Link>
        </p>
      }
    >
      <RegistrationForm />
    </AuthLayout>
  )
}
