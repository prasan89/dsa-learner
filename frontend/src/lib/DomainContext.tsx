"use client";

import { createContext, useContext, useEffect, useState } from "react";
import api from "@/lib/api/client";

export type ActiveDomain = "dsa" | "language" | null;

interface DomainContextValue {
  activeDomain: ActiveDomain;
  loading: boolean;
}

const DomainContext = createContext<DomainContextValue>({ activeDomain: null, loading: true });

export function DomainProvider({ children }: { children: React.ReactNode }) {
  const [activeDomain, setActiveDomain] = useState<ActiveDomain>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get<{ userId: string; activeDomain: string }>("/v1/me")
      .then((res) => {
        const d = res.data.activeDomain;
        setActiveDomain(d === "dsa" || d === "language" ? d : null);
      })
      .catch(() => setActiveDomain(null))
      .finally(() => setLoading(false));
  }, []);

  return (
    <DomainContext.Provider value={{ activeDomain, loading }}>
      {children}
    </DomainContext.Provider>
  );
}

export function useDomain() {
  return useContext(DomainContext);
}
