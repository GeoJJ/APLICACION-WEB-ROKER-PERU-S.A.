import HeroSection from "@/components/features/home/HeroSection";
import FeatureBar from "@/components/features/home/FeatureBar";
import FeaturedCategories from "@/components/features/home/FeaturedCategories";
import CtaBanner from "@/components/features/home/CtaBanner";

export default function HomePage() {
  return (
    <>
      <HeroSection />
      <FeatureBar />
      <FeaturedCategories />
      <CtaBanner />
    </>
  );
}