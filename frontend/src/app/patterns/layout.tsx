import AppShell from "@/components/layout/AppShell";
import DomainGuard from "@/components/layout/DomainGuard";

export default function PatternsLayout({ children }: { children: React.ReactNode }) {
  return (
    <AppShell>
      <DomainGuard required="dsa">{children}</DomainGuard>
    </AppShell>
  );
}
