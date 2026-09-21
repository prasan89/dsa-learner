import Navbar from "@/components/layout/Navbar";

export default function ProblemsLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="h-screen bg-gray-950 flex flex-col">
      <Navbar />
      <main className="flex-1 overflow-hidden">{children}</main>
    </div>
  );
}
