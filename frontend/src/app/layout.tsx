import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Spring AI Lab",
  description: "Spring과 Next.js로 배우는 AI 실습",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return <html lang="ko"><body>{children}</body></html>;
}
