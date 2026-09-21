import Link from "next/link";

export default function HomePage() {
  return (
    <main className="min-h-screen bg-gray-950 flex flex-col items-center justify-center px-4">
      <div className="max-w-3xl text-center space-y-6">
        <h1 className="text-5xl font-bold text-white leading-tight">
          Learn DSA Patterns.<br />
          <span className="text-brand-500">Get AI Mentored.</span>
        </h1>
        <p className="text-xl text-gray-400">
          Recognise patterns → practice curated problems → build mastery.
          Built for Java developers targeting top-tier interviews.
        </p>
        <div className="flex gap-4 justify-center">
          <Link
            href="/register"
            className="px-6 py-3 bg-brand-600 hover:bg-brand-700 text-white font-semibold rounded-lg transition-colors"
          >
            Get Started Free
          </Link>
          <Link
            href="/problems"
            className="px-6 py-3 border border-gray-700 hover:border-gray-500 text-gray-300 font-semibold rounded-lg transition-colors"
          >
            Browse Problems
          </Link>
        </div>
      </div>
    </main>
  );
}
