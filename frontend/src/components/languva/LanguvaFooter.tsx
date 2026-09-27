import Link from "next/link";
import { BookOpen, Github, Twitter, Linkedin, Youtube } from "lucide-react";

const FOOTER_LINKS = ["About", "Pricing", "Blog", "Contact", "Privacy", "Terms"];

export default function LanguvaFooter() {
  return (
    <footer className="border-t border-gray-100 bg-white">
      {/* Stats bar */}
      <div className="border-b border-gray-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-5 grid grid-cols-2 sm:grid-cols-4 gap-4">
          {[
            { icon: "😊", value: "500K+",   label: "Happy Learners"   },
            { icon: "🌍", value: "14",       label: "Languages"        },
            { icon: "📚", value: "A1 - C2",  label: "All Levels"       },
            { icon: "⭐", value: "4.8/5",    label: "Average Rating"   },
          ].map(({ icon, value, label }) => (
            <div key={label} className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-lg bg-languva-50 flex items-center justify-center text-lg shrink-0">
                {icon}
              </div>
              <div>
                <p className="font-bold text-gray-900 text-sm leading-tight">{value}</p>
                <p className="text-xs text-gray-500">{label}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Bottom bar */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6 flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-lg bg-languva-600 flex items-center justify-center">
            <BookOpen size={13} className="text-white" />
          </div>
          <span className="font-bold text-gray-900 text-sm">Languva</span>
          <span className="text-xs text-gray-400 ml-1">© {new Date().getFullYear()}</span>
        </div>

        <div className="flex flex-wrap justify-center gap-5 text-sm text-gray-500">
          {FOOTER_LINKS.map((item) => (
            <Link key={item} href="#" className="hover:text-gray-700 transition-colors">{item}</Link>
          ))}
        </div>

        <div className="flex items-center gap-3 text-gray-400">
          <a href="#" aria-label="GitHub" className="hover:text-gray-700 transition-colors"><Github size={17} /></a>
          <a href="#" aria-label="LinkedIn" className="hover:text-gray-700 transition-colors"><Linkedin size={17} /></a>
          <a href="#" aria-label="Twitter" className="hover:text-gray-700 transition-colors"><Twitter size={17} /></a>
          <a href="#" aria-label="YouTube" className="hover:text-gray-700 transition-colors"><Youtube size={17} /></a>
        </div>
      </div>
    </footer>
  );
}
