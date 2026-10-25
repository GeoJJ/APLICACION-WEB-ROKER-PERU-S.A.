import { Routes, Route } from 'react-router-dom';
import PublicLayout from '@/components/layout/storefront/PublicLayout';
import AuthLayout from '@/components/layout/auth/AuthLayout';
import HomePage from '@/pages/storefront/HomePage';
import CatalogPage from '@/pages/storefront/CatalogPage';
import ServicesPage from '@/pages/storefront/ServicesPage';
import AboutPage from '@/pages/storefront/AboutPage';
import ContactPage from '@/pages/storefront/ContactPage';
import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';

import BackOfficeLayout from '@/components/layout/backoffice/BackOfficeLayout';
import AdminDashboardPage from '@/pages/backoffice/admin/AdminDashboardPage';
import WarehouseStockPage from '@/pages/backoffice/warehouse/WarehouseStockPage';
import SellerOrdersPage from '@/pages/backoffice/seller/SellerOrdersPage';
import DeliveryPage from '@/pages/backoffice/delivery/DeliveryPage';

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

            {/* Rutas Backoffice */}
            <Route element={<BackOfficeLayout role="admin" />}>
                <Route path="/admin" element={<AdminDashboardPage />} />
            </Route>

            <Route element={<BackOfficeLayout role="warehouse" />}>
                <Route path="/warehouse" element={<WarehouseStockPage />} />
            </Route>

            <Route element={<BackOfficeLayout role="seller" />}>
                <Route path="/seller" element={<SellerOrdersPage />} />
            </Route>

            <Route element={<BackOfficeLayout role="delivery" />}>
                <Route path="/delivery" element={<DeliveryPage />} />
            </Route>
        </Routes>
    );
}