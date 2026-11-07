import MovementsPage from '@/pages/backoffice/warehouse/MovementsPage';
import CartPage from '@/pages/storefront/CartPage';
import ServiceRequestPage from '@/pages/storefront/ServiceRequestPage';
import ServiceRequestsPage from '@/pages/storefront/ServiceRequestsPage';
import { Routes, Route, Navigate } from 'react-router-dom';
import PublicLayout from '@/components/layout/storefront/PublicLayout';
import AuthLayout from '@/components/layout/auth/AuthLayout';
import HomePage from '@/pages/storefront/HomePage';
import CatalogPage from '@/pages/storefront/CatalogPage';
import ServicesPage from '@/pages/storefront/ServicesPage';
import AboutPage from '@/pages/storefront/AboutPage';
import ContactPage from '@/pages/storefront/ContactPage';
import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';
import ProfilePage from '@/pages/auth/ProfilePage';
import RequireRole from '@/components/auth/RequireRole';
import BackOfficeLayout from '@/components/layout/backoffice/BackOfficeLayout';
import Dashboard from '@/pages/backoffice/admin/AdminDashboardPage';
import Stock from '@/pages/backoffice/warehouse/StockOverviewPage';
import Orders from '@/pages/backoffice/seller/OrdersManagementPage';
import Deliveries from '@/pages/backoffice/delivery/DeliveryManagementPage';
import Management from '@/pages/backoffice/admin/ManagementPage';
import Entries from '@/pages/backoffice/warehouse/StockEntriesPage';
import Reports from '@/pages/backoffice/admin/Reportes';
import StaffPage from '@/pages/backoffice/staff/StaffPage';
export default function AppRouter() {
 return <Routes><Route path="/admin/*" element={<Navigate to="/manager" replace />} />
  <Route element={<PublicLayout />}>
   <Route path="/carrito" element={<CartPage />} /><Route path="/servicios/:id/solicitar" element={<ServiceRequestPage />} /><Route element={<RequireRole role="customer" />}><Route path="/mis-solicitudes" element={<ServiceRequestsPage />} /></Route>
   <Route path="/" element={<HomePage />} /><Route path="/productos" element={<CatalogPage />} /><Route path="/servicios" element={<ServicesPage />} /><Route path="/nosotros" element={<AboutPage />} /><Route path="/contacto" element={<ContactPage />} />
   <Route element={<RequireRole />}><Route path="/perfil" element={<ProfilePage />} /></Route>
   <Route path="*" element={<div className="p-12 text-center">La página que buscas no existe.</div>} />
  </Route>
  <Route element={<AuthLayout />}><Route path="/login" element={<LoginPage />} /><Route path="/register" element={<RegisterPage />} /></Route>
  <Route element={<RequireRole role="manager" />}><Route element={<BackOfficeLayout role="manager" />}>
   <Route path="/manager" element={<Dashboard />} /><Route path="/manager/solicitudes" element={<ServiceRequestsPage management />} /><Route path="/manager/inventario" element={<MovementsPage />} /><Route path="/manager/reportes" element={<Reports />} />
   <Route path="/manager/trabajadores" element={<Management key="workers" kind="workers" />} /><Route path="/manager/productos" element={<Management key="products" kind="products" />} /><Route path="/manager/servicios" element={<Management key="services" kind="services" />} /><Route path="/manager/proveedores" element={<Management key="providers" kind="providers" />} />
  </Route></Route>
  <Route element={<RequireRole role="warehouse" />}><Route element={<BackOfficeLayout role="warehouse" />}>
   <Route path="/warehouse" element={<Stock />} /><Route path="/warehouse/productos" element={<Management kind="products" readonly />} /><Route path="/warehouse/ingresos" element={<Entries />} />
  </Route></Route>
  <Route element={<RequireRole role="seller" />}><Route element={<BackOfficeLayout role="seller" />}>
   <Route path="/seller" element={<Orders home />} /><Route path="/seller/solicitudes" element={<ServiceRequestsPage management />} /><Route path="/seller/pedidos" element={<Orders />} /><Route path="/seller/ventas" element={<Orders sales />} />
  </Route></Route>
  <Route element={<RequireRole role="delivery" />}><Route element={<BackOfficeLayout role="delivery" />}>
   <Route path="/delivery" element={<Deliveries />} /><Route path="/delivery/historial" element={<Deliveries history />} />
  </Route></Route>
  <Route element={<RequireRole role="staff" />}><Route element={<BackOfficeLayout role="staff" />}><Route path="/staff" element={<StaffPage />} /></Route></Route>
 </Routes>;
}
