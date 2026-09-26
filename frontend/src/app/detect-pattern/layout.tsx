import AppShell from "@/components/layout/AppShell";
import DomainGuard from "@/components/layout/DomainGuard";

export default function DetectPatternLayout({ children }: { children: React.ReactNode }) {
  return (
    <AppShell>
      <DomainGuard required="dsa">{children}</DomainGuard>
    </AppShell>
  );
}
