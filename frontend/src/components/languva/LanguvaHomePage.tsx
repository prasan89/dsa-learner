import LanguvaHeader   from "@/components/languva/LanguvaHeader";
import HeroSection     from "@/components/languva/HeroSection";
import FeatureStrip    from "@/components/languva/FeatureStrip";
import LanguageSelector from "@/components/languva/LanguageSelector";
import WhyLanguva      from "@/components/languva/WhyLanguva";
import JourneyCta      from "@/components/languva/JourneyCta";
import LanguvaFooter   from "@/components/languva/LanguvaFooter";

export default function LanguvaHomePage() {
  return (
    <div className="min-h-screen bg-white text-gray-900">
      <LanguvaHeader />
      <HeroSection />
      <FeatureStrip />
      <LanguageSelector />
      <WhyLanguva />
      <JourneyCta />
      <LanguvaFooter />
    </div>
  );
}
