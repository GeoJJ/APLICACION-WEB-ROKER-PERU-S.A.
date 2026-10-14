import PageTitle from "@/components/shared/PageTitle";
import { CATEGORIES } from "@/data/categories";
import CategoryCard from "./CategoryCard";

export default function FeaturedCategories() {
    return (
        <section className="mx-auto max-w-[1280px] px-6 py-14">
            <PageTitle as="h2">Categorías destacadas</PageTitle>
            <div className="mt-8 grid grid-cols-2 gap-6 md:grid-cols-3 lg:grid-cols-5">
                {CATEGORIES.map((category) => (
                    <CategoryCard
                        key={category.id}
                        name={category.name}
                        image={category.image}
                        icon={category.icon}
                        to={`/productos?categoria=${category.id}`}
                    />
                ))}
            </div>
        </section>
    );
}