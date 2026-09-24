import AppShell from "@/components/layout/AppShell";
export default function GoLayout({ children }: { children: React.ReactNode }) {
  return <AppShell>{children}</AppShell>;
}
