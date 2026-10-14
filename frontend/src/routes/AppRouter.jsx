import { Routes, Route } from 'react-router-dom';
import PublicLayout from '@/components/layout/storefront/PublicLayout';
import HomePage from '@/pages/storefront/HomePage';

export default function AppRouter() {
    return (
        <Routes>
            <Route element={<PublicLayout />}>
                <Route path="/" element={<HomePage />} />
            </Route>
        </Routes>
    );
}