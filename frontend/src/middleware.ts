import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

const PROTECTED_PATHS = ["/dashboard", "/problems", "/patterns", "/profile", "/wallet", "/system-design", "/java", "/ai-mentor", "/revision", "/progress", "/learn", "/content-factory", "/languages"];

const DSA_ONLY_PATHS = ["/problems", "/patterns", "/ai-mentor", "/revision", "/learn", "/java", "/go", "/rust", "/ai-engineering", "/system-design"];
const LANGUAGE_ONLY_PATHS = ["/languages", "/content-factory"];

export function middleware(request: NextRequest) {
  const accessToken = request.cookies.get("accessToken")?.value;
  const refreshToken = request.cookies.get("refreshToken")?.value;
  const { pathname } = request.nextUrl;

  const isProtected = PROTECTED_PATHS.some((p) => pathname.startsWith(p));

  if (isProtected && !accessToken && !refreshToken) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("redirect", pathname);
    return NextResponse.redirect(loginUrl);
  }

  if ((pathname === "/login" || pathname === "/register") && (accessToken || refreshToken)) {
    return NextResponse.redirect(new URL("/dashboard", request.url));
  }

  const mode = process.env.NEXT_PUBLIC_APP_MODE ?? "all";

  if (mode === "dsa" && LANGUAGE_ONLY_PATHS.some((p) => pathname.startsWith(p))) {
    return NextResponse.redirect(new URL("/dashboard", request.url));
  }
  if (mode === "language" && DSA_ONLY_PATHS.some((p) => pathname.startsWith(p))) {
    return NextResponse.redirect(new URL("/dashboard", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico|api).*)"],
};
