import { Routes, Route } from 'react-router-dom';
import PublicLayout from '@/components/layout/storefront/PublicLayout';
import HomePage from '@/pages/storefront/HomePage';
import CatalogPage from '@/pages/storefront/CatalogPage';

export default function AppRouter() {
    return (
        <Routes>
            <Route element={<PublicLayout />}>
                <Route path="/" element={<HomePage />} />
                <Route path="/productos" element={<CatalogPage />} />
                <Route path="/servicios" element={<ServicesPage />} />
            </Route>
        </Routes>
    );
}