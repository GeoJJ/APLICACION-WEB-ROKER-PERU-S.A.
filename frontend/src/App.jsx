import CartProvider from '@/components/cart/CartProvider';
import CartOverlay from '@/components/cart/CartOverlay';
import ScrollToTop from '@/components/shared/ScrollToTop';
import { BrowserRouter } from 'react-router-dom';
import AppRouter from './routes/AppRouter';
import BackOfficeProvider from '@/components/layout/backoffice/BackOfficeProvider';
import AuthProvider from '@/components/auth/AuthProvider';

export default function App() {
  return (
    <BrowserRouter>
      <ScrollToTop />
      <AuthProvider><BackOfficeProvider><CartProvider><AppRouter /><CartOverlay /></CartProvider></BackOfficeProvider></AuthProvider>
    </BrowserRouter>
  );
}
