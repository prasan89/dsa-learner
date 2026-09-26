"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useDomain, type ActiveDomain } from "@/lib/DomainContext";

interface DomainGuardProps {
  required: ActiveDomain;
  children: React.ReactNode;
}

export default function DomainGuard({ required, children }: DomainGuardProps) {
  const { activeDomain, loading } = useDomain();
  const router = useRouter();

  useEffect(() => {
    if (loading) return;
    if (activeDomain === null) {
      router.replace("/login");
    } else if (activeDomain !== required) {
      router.replace("/unauthorized");
    }
  }, [activeDomain, loading, required, router]);

  if (loading) return null;
  if (activeDomain !== required) return null;

  return <>{children}</>;
}
