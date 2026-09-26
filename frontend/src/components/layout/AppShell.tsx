import Sidebar from "@/components/layout/Sidebar";
import { DomainProvider } from "@/lib/DomainContext";

export default function AppShell({ children }: { children: React.ReactNode }) {
  return (
    <DomainProvider>
      <div className="flex min-h-screen bg-gray-50">
        <Sidebar />
        <main className="flex-1 min-w-0" style={{ marginLeft: "var(--sidebar-width)" }}>
          {children}
        </main>
      </div>
    </DomainProvider>
  );
}
