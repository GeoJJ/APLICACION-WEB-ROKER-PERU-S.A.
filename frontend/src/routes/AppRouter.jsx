import { Routes, Route } from 'react-router-dom';
import PublicLayout from '@/components/layout/storefront/PublicLayout';
import HomePage from '@/pages/storefront/HomePage';
import CatalogPage from '@/pages/storefront/CatalogPage';
import ServicesPage from '@/pages/storefront/ServicesPage';
import AboutPage from '@/pages/storefront/AboutPage';
import ContactPage from '@/pages/storefront/ContactPage';
import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';

export default function AppRouter() {
    return (
        <Routes>
            <Route element={<PublicLayout />}>
                <Route path="/" element={<HomePage />} />
                <Route path="/productos" element={<CatalogPage />} />
                <Route path="/servicios" element={<ServicesPage />} />
                <Route path="/nosotros" element={<AboutPage />} />
                <Route path="/contacto" element={<ContactPage />} />
            </Route>
            <Route element={<AuthLayout />}>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
            </Route>
        </Routes>
    );
}