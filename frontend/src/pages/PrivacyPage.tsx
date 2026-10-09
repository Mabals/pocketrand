import { Link } from 'react-router'
import { useDocumentTitle } from '../hooks/useDocumentTitle'
import Logo from '../components/Logo'

const PRIVACY_CONTACT = 'Siyaballack1@gmail.com'
const LAST_UPDATED = '9 October 2026'

export default function PrivacyPage() {
  useDocumentTitle('Privacy notice')

  return (
    <div className="min-h-screen">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-3xl items-center justify-between px-4 py-4">
          <Link to="/">
            <Logo />
          </Link>
          <Link to="/" className="text-sm font-medium text-emerald-700 hover:underline">
            Back to PocketRand
          </Link>
        </div>
      </header>

      <main className="mx-auto max-w-3xl space-y-8 px-4 py-10 text-slate-700">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Privacy notice</h1>
          <p className="mt-2 text-sm text-slate-500">Last updated: {LAST_UPDATED}</p>
          <p className="mt-4">
            PocketRand is a personal portfolio project built by Thato Khonkhe. This notice explains, in plain
            language, what happens to your information, in line with South Africa's Protection of Personal
            Information Act (POPIA).
          </p>
          <p className="mt-3 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
            While PocketRand is being tested, please use the sample statements or the demo account rather than
            your real bank statements.
          </p>
        </div>

        <section>
          <h2 className="text-xl font-semibold text-slate-900">1. What we collect</h2>
          <ul className="mt-3 list-disc space-y-1 pl-6">
            <li>Your name and email address, to give you an account.</li>
            <li>Your password, stored only as a secure one-way hash. Nobody can read it, including us.</li>
            <li>Transactions you upload or add: date, description, amount and category.</li>
            <li>Budgets you set.</li>
            <li>Your IP address, briefly and only in memory, to block repeated login attempts.</li>
          </ul>
        </section>

        <section>
          <h2 className="text-xl font-semibold text-slate-900">2. What we never collect or keep</h2>
          <ul className="mt-3 list-disc space-y-1 pl-6">
            <li>ID numbers, bank account numbers or card numbers.</li>
            <li>The header of a PDF statement (your name, address and account number) is removed before anything is processed.</li>
            <li>Uploaded statement files are read and then discarded. They are never stored.</li>
          </ul>
        </section>

        <section>
          <h2 className="text-xl font-semibold text-slate-900">3. Why we use it</h2>
          <p className="mt-3">
            Only to provide PocketRand's features: categorising your spending, monthly summaries, budgets, tips and
            tax estimates. Your information is never sold and never used for advertising.
          </p>
        </section>

        <section>
          <h2 className="text-xl font-semibold text-slate-900">4. The AI service we use</h2>
          <p className="mt-3">
            PocketRand uses Google's Gemini service to categorise transactions, read PDF statements and write
            spending tips. Only what's needed is sent:
          </p>
          <ul className="mt-3 list-disc space-y-1 pl-6">
            <li>Transaction descriptions and amounts that our own rules can't categorise.</li>
            <li>For PDFs, only lines that start with a date, with long numbers masked.</li>
            <li>For tips, monthly totals only, never individual transactions.</li>
          </ul>
          <p className="mt-3">
            Your name, email and account details are never sent. This service may process data outside South
            Africa, and on its free tier the provider may use submitted content to improve its products. That's
            another reason to use sample data while testing.
          </p>
        </section>

        <section>
          <h2 className="text-xl font-semibold text-slate-900">5. How long we keep it</h2>
          <ul className="mt-3 list-disc space-y-1 pl-6">
            <li>Your account and data: until you delete your account.</li>
            <li>Demo accounts: deleted automatically after 24 hours.</li>
            <li>Password reset links: expire after 30 minutes and work only once.</li>
          </ul>
        </section>

        <section>
          <h2 className="text-xl font-semibold text-slate-900">6. How we protect it</h2>
          <p className="mt-3">
            Passwords are hashed, every request needs a signed login token, each account can only see its own
            data, secrets are kept out of the code, and repeated login attempts are blocked. No system is
            perfectly secure, but we follow established practices to keep your information safe.
          </p>
        </section>

        <section>
          <h2 className="text-xl font-semibold text-slate-900">7. Your rights</h2>
          <ul className="mt-3 list-disc space-y-1 pl-6">
            <li><strong>Access:</strong> download all your data from Settings, then Download my data.</li>
            <li><strong>Correction:</strong> change any transaction's category, or delete incorrect transactions.</li>
            <li><strong>Deletion:</strong> delete your account and everything in it from Settings, at any time.</li>
            <li><strong>Withdrawing consent:</strong> deleting your account withdraws your consent and removes your data.</li>
            <li>
              <strong>Complaints:</strong> you can contact us below, or complain to South Africa's Information Regulator.
            </li>
          </ul>
        </section>

        <section>
          <h2 className="text-xl font-semibold text-slate-900">8. Contact</h2>
          <p className="mt-3">
            Questions about your privacy? Email{' '}
            <a href={`mailto:${PRIVACY_CONTACT}`} className="font-medium text-emerald-700 hover:underline">
              {PRIVACY_CONTACT}
            </a>
            .
          </p>
        </section>
      </main>
    </div>
  )
}