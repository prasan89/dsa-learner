export type LanguageCategory = "indian" | "global";

export interface Language {
  code: string;
  name: string;
  nativeName: string;
  flag: string;
  category: LanguageCategory;
  cefrRange: string;
  learnerCount: string;
  // image placeholder — supply a real image at /images/languages/<code>.jpg
  imagePath: string;
  route: string;
}

export const LANGUAGES: Language[] = [
  // Indian
  { code: "hindi",   name: "Hindi",    nativeName: "हिन्दी",    flag: "🇮🇳", category: "indian", cefrRange: "A1 - C2", learnerCount: "1.2M learners", imagePath: "/images/languages/hindi.jpg",   route: "/languages/hindi"   },
  { code: "tamil",   name: "Tamil",    nativeName: "தமிழ்",     flag: "🇮🇳", category: "indian", cefrRange: "A1 - C2", learnerCount: "980K learners", imagePath: "/images/languages/tamil.jpg",   route: "/languages/tamil"   },
  { code: "telugu",  name: "Telugu",   nativeName: "తెలుగు",    flag: "🇮🇳", category: "indian", cefrRange: "A1 - C2", learnerCount: "850K learners", imagePath: "/images/languages/telugu.jpg",  route: "/languages/telugu"  },
  { code: "marathi", name: "Marathi",  nativeName: "मराठी",     flag: "🇮🇳", category: "indian", cefrRange: "A1 - C2", learnerCount: "620K learners", imagePath: "/images/languages/marathi.jpg", route: "/languages/marathi" },
  { code: "kannada", name: "Kannada",  nativeName: "ಕನ್ನಡ",    flag: "🇮🇳", category: "indian", cefrRange: "A1 - C2", learnerCount: "540K learners", imagePath: "/images/languages/kannada.jpg", route: "/languages/kannada" },
  // Global
  { code: "english",  name: "English",          nativeName: "English",   flag: "🇬🇧", category: "global", cefrRange: "A1 - C2", learnerCount: "2.1M learners", imagePath: "/images/languages/english.jpg",  route: "/languages/english"  },
  { code: "german",   name: "German",           nativeName: "Deutsch",   flag: "🇩🇪", category: "global", cefrRange: "A1 - C2", learnerCount: "1.1M learners", imagePath: "/images/languages/german.jpg",   route: "/languages/german"   },
  { code: "french",   name: "French",           nativeName: "Français",  flag: "🇫🇷", category: "global", cefrRange: "A1 - C2", learnerCount: "980K learners", imagePath: "/images/languages/french.jpg",   route: "/languages/french"   },
  { code: "spanish",  name: "Spanish",          nativeName: "Español",   flag: "🇪🇸", category: "global", cefrRange: "A1 - C2", learnerCount: "870K learners", imagePath: "/images/languages/spanish.jpg",  route: "/languages/spanish"  },
  { code: "italian",  name: "Italian",          nativeName: "Italiano",  flag: "🇮🇹", category: "global", cefrRange: "A1 - C2", learnerCount: "640K learners", imagePath: "/images/languages/italian.jpg",  route: "/languages/italian"  },
  { code: "portuguese", name: "Portuguese",     nativeName: "Português", flag: "🇵🇹", category: "global", cefrRange: "A1 - C2", learnerCount: "590K learners", imagePath: "/images/languages/portuguese.jpg", route: "/languages/portuguese" },
  { code: "japanese", name: "Japanese",         nativeName: "日本語",    flag: "🇯🇵", category: "global", cefrRange: "A1 - C2", learnerCount: "780K learners", imagePath: "/images/languages/japanese.jpg", route: "/languages/japanese" },
  { code: "korean",   name: "Korean",           nativeName: "한국어",    flag: "🇰🇷", category: "global", cefrRange: "A1 - C2", learnerCount: "690K learners", imagePath: "/images/languages/korean.jpg",   route: "/languages/korean"   },
  { code: "mandarin", name: "Mandarin Chinese", nativeName: "普通话",    flag: "🇨🇳", category: "global", cefrRange: "A1 - C2", learnerCount: "920K learners", imagePath: "/images/languages/mandarin.jpg", route: "/languages/mandarin" },
];

export const INDIAN_LANGUAGES  = LANGUAGES.filter((l) => l.category === "indian");
export const GLOBAL_LANGUAGES  = LANGUAGES.filter((l) => l.category === "global");

// Configurable marketing stats — change here, updates everywhere
export const LANGUVA_STATS = {
  totalLanguages: 14,
  indianCount: 5,
  globalCount: 9,
  learnerCount: "500K+",
  cefrRange: "A1 – C2",
  averageRating: "4.8/5",
} as const;
