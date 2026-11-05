// Stable identifiers for future authorization. Editing workers does not authenticate a session.
export const ROLES = [
  { id: 'admin', code: 'ADMINISTRADOR', label: 'Administrador', home: '/admin', color: 'bg-blue-600' },
  { id: 'warehouse', code: 'ALMACENISTA', label: 'Almacenista', home: '/warehouse', color: 'bg-green-600' },
  { id: 'seller', code: 'VENDEDOR', label: 'Vendedor', home: '/seller', color: 'bg-purple-600' },
  { id: 'delivery', code: 'REPARTIDOR', label: 'Repartidor', home: '/delivery', color: 'bg-orange-600' },
  { id: 'staff', code: 'PERSONAL', label: 'Personal de servicio', home: '/staff', color: 'bg-teal-600' },
  { id: 'customer', code: 'CLIENTE', label: 'Cliente', home: '/productos', color: 'bg-slate-600' },
];
