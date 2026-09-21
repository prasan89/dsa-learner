'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import api from '@/lib/api/client';

interface Transaction {
  delta: number;
  type: string;
  description: string;
  createdAt: string;
}

interface Wallet {
  freeCredits: number;
  paidCredits: number;
  totalCredits: number;
  lifetimeUsed: number;
  recentTransactions: Transaction[];
}

interface Subscription {
  plan: string;
  isPro: boolean;
  expiresAt: string | null;
}

declare global {
  interface Window {
    Razorpay: any;
  }
}

export default function WalletPage() {
  const router = useRouter();
  const [wallet, setWallet] = useState<Wallet | null>(null);
  const [sub, setSub] = useState<Subscription | null>(null);
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([
      api.get<Wallet>('/api/ai/wallet'),
      api.get<Subscription>('/api/payments/subscription'),
    ])
      .then(([w, s]) => {
        setWallet(w.data);
        setSub(s.data);
      })
      .catch(() => router.push('/login'))
      .finally(() => setLoading(false));
  }, [router]);

  function loadRazorpay(): Promise<boolean> {
    return new Promise((resolve) => {
      if (window.Razorpay) { resolve(true); return; }
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.onload = () => resolve(true);
      script.onerror = () => resolve(false);
      document.body.appendChild(script);
    });
  }

  async function handleUpgrade() {
    setPaying(true);
    setError('');
    try {
      const loaded = await loadRazorpay();
      if (!loaded) { setError('Could not load payment gateway. Try again.'); return; }

      const { data } = await api.post<{ orderId: string; amountPaise: number; currency: string; keyId: string }>(
        '/api/payments/create-order'
      );

      const options = {
        key: data.keyId,
        amount: data.amountPaise,
        currency: data.currency,
        name: 'DSA Learner',
        description: 'PRO Plan — 500 AI credits',
        order_id: data.orderId,
        handler: async (response: any) => {
          await api.post('/api/payments/verify', {
            orderId: response.razorpay_order_id,
            paymentId: response.razorpay_payment_id,
            signature: response.razorpay_signature,
          });
          // Refresh wallet + sub
          const [w, s] = await Promise.all([
            api.get<Wallet>('/api/ai/wallet'),
            api.get<Subscription>('/api/payments/subscription'),
          ]);
          setWallet(w.data);
          setSub(s.data);
        },
        prefill: {},
        theme: { color: '#6366f1' },
      };
      const rzp = new window.Razorpay(options);
      rzp.open();
    } catch (e: any) {
      setError(e?.response?.data?.message ?? 'Payment failed. Please try again.');
    } finally {
      setPaying(false);
    }
  }

  if (loading) return <div className="p-8 text-gray-400">Loading…</div>;

  const txTypeLabel: Record<string, string> = {
    FREE_GRANT: 'Free grant',
    PURCHASE: 'Purchase',
    AI_REVIEW: 'Code review',
    AI_HINT: 'Hint unlock',
    AI_DETECT: 'Pattern detect',
  };

  return (
    <div className="min-h-screen bg-gray-950 text-white p-6 max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold mb-6">My Wallet</h1>

      {/* Plan card */}
      <div className="bg-gray-900 rounded-xl p-5 mb-6 flex items-center justify-between">
        <div>
          <div className="text-sm text-gray-400 mb-1">Current Plan</div>
          <div className="flex items-center gap-2">
            <span className={`text-xl font-bold ${sub?.isPro ? 'text-yellow-400' : 'text-gray-200'}`}>
              {sub?.plan ?? 'FREE'}
            </span>
            {sub?.isPro && sub.expiresAt && (
              <span className="text-xs text-gray-400">
                expires {new Date(sub.expiresAt).toLocaleDateString()}
              </span>
            )}
          </div>
        </div>
        {!sub?.isPro && (
          <button
            onClick={handleUpgrade}
            disabled={paying}
            className="bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white text-sm font-semibold px-5 py-2 rounded-lg transition"
          >
            {paying ? 'Processing…' : 'Upgrade to PRO — ₹499/mo'}
          </button>
        )}
      </div>

      {error && <div className="text-red-400 text-sm mb-4">{error}</div>}

      {/* Credit balance */}
      <div className="grid grid-cols-3 gap-4 mb-6">
        <div className="bg-gray-900 rounded-xl p-4 text-center">
          <div className="text-3xl font-bold text-indigo-400">{wallet?.freeCredits ?? 0}</div>
          <div className="text-xs text-gray-400 mt-1">Free Credits</div>
        </div>
        <div className="bg-gray-900 rounded-xl p-4 text-center">
          <div className="text-3xl font-bold text-yellow-400">{wallet?.paidCredits ?? 0}</div>
          <div className="text-xs text-gray-400 mt-1">Paid Credits</div>
        </div>
        <div className="bg-gray-900 rounded-xl p-4 text-center">
          <div className="text-3xl font-bold text-green-400">{wallet?.totalCredits ?? 0}</div>
          <div className="text-xs text-gray-400 mt-1">Total Available</div>
        </div>
      </div>

      {/* PRO benefits */}
      {!sub?.isPro && (
        <div className="bg-gray-900 border border-indigo-800 rounded-xl p-5 mb-6">
          <div className="font-semibold text-indigo-300 mb-3">PRO Plan — ₹499/month</div>
          <ul className="space-y-1 text-sm text-gray-300">
            <li>✓ 500 AI credits per month</li>
            <li>✓ Unlimited code reviews</li>
            <li>✓ Priority AI hints</li>
            <li>✓ Pattern detection</li>
            <li>✓ Spaced repetition scheduling</li>
          </ul>
        </div>
      )}

      {/* Transaction history */}
      <h2 className="text-lg font-semibold mb-3">Recent Transactions</h2>
      <div className="space-y-2">
        {wallet?.recentTransactions.length === 0 && (
          <div className="text-gray-500 text-sm">No transactions yet.</div>
        )}
        {wallet?.recentTransactions.map((tx, i) => (
          <div key={i} className="bg-gray-900 rounded-lg px-4 py-3 flex items-center justify-between">
            <div>
              <div className="text-sm font-medium">{tx.description}</div>
              <div className="text-xs text-gray-500">
                {txTypeLabel[tx.type] ?? tx.type} · {new Date(tx.createdAt).toLocaleString()}
              </div>
            </div>
            <span className={`text-sm font-bold ${tx.delta > 0 ? 'text-green-400' : 'text-red-400'}`}>
              {tx.delta > 0 ? '+' : ''}{tx.delta}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
