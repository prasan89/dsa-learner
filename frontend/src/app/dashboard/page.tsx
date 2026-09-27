import DsaDashboard from "./_dsa/DsaDashboard";
import LanguageDashboard from "./_language/LanguageDashboard";

const APP_MODE = process.env.NEXT_PUBLIC_APP_MODE ?? "all";

export default function DashboardPage() {
  if (APP_MODE === "language") return <LanguageDashboard />;
  return <DsaDashboard />;
}
