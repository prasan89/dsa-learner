import AppShell from "@/components/layout/AppShell";

export default function ContentFactoryLayout({ children }: { children: React.ReactNode }) {
  return <AppShell>{children}</AppShell>;
}
