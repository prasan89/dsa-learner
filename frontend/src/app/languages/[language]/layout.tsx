import AppShell from "@/components/layout/AppShell";
import DomainGuard from "@/components/layout/DomainGuard";

export default function LanguageLayout({ children }: { children: React.ReactNode }) {
  return (
    <AppShell>
      <DomainGuard required="language">{children}</DomainGuard>
    </AppShell>
  );
}
