"use client";

import { useEffect, useState } from "react";
import { userApi } from "@/lib/api/user";

export function useSubscription() {
  const [pro, setPro]       = useState<boolean | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    userApi.subscription()
      .then((r) => setPro((r.data as any).pro ?? false))
      .catch(() => setPro(false))
      .finally(() => setLoading(false));
  }, []);

  return { pro, loading };
}
