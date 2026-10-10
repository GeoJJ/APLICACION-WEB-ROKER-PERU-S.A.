import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import logo from '../assets/logo.png';

export default function Header() {
  const [empresaOpen, setEmpresaOpen] = useState(false);
  const [productosOpen, setProductosOpen] = useState(false);

  return (
    <header className="bg-[#a3d8f4] py-3 px-6 md:px-12 shadow-sm sticky top-0 z-50">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        
        <Link to="/" className="flex items-center cursor-pointer">
          <img src={logo} alt="Roker Perú Logo" className="h-10 md:h-12 w-auto object-contain" />
        </Link>

        <nav className="hidden md:flex items-center gap-7 text-sm font-medium text-white">
          <Link to="/" className="hover:text-[#003875] transition">
            Inicio
          </Link>

          <div 
            className="relative cursor-pointer py-2"
            onMouseEnter={() => setEmpresaOpen(true)}
            onMouseLeave={() => setEmpresaOpen(false)}
          >
            <div className="flex items-center gap-1 hover:text-[#003875] transition">
              <span>Empresa</span>
              <svg className="w-3.5 h-3.5 fill-current" viewBox="0 0 20 20">
                <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"/>
              </svg>
            </div>

            {empresaOpen && (
              <div className="absolute top-full left-0 w-64 bg-white text-gray-600 rounded-2xl shadow-xl py-3 z-50 border border-gray-100">
                <Link 
                  to="/nosotros" 
                  className="block px-5 py-2.5 hover:bg-gray-50 text-xs font-semibold leading-snug transition text-[#1a2530]"
                >
                  Propósito, Cultura, Principios
                </Link>
                <div className="px-5 py-2.5 text-xs text-gray-400 cursor-not-allowed">
                  Certificaciones
                </div>
                <div className="px-5 py-2.5 text-xs text-gray-400 cursor-not-allowed flex items-center justify-between">
                  <span>Sistema Integrado de Gestión</span>
                  <span>∨</span>
                </div>
                <div className="px-5 py-2.5 text-xs text-gray-400 cursor-not-allowed flex items-center justify-between">
                  <span>Sistema de Gestión Antisoborno</span>
                  <span>∨</span>
                </div>
                <div className="px-5 py-2.5 text-xs text-gray-400 cursor-not-allowed">
                  S.G.C Dispositivos Médicos
                </div>
              </div>
            )}
          </div>

          <Link to="/servicios" className="hover:text-[#003875] transition">
            Servicios
          </Link>

          <div 
            className="relative cursor-pointer py-2"
            onMouseEnter={() => setProductosOpen(true)}
            onMouseLeave={() => setProductosOpen(false)}
          >
            <div className="flex items-center gap-1 hover:text-[#003875] transition">
              <span>Productos</span>
              <svg className="w-3.5 h-3.5 fill-current" viewBox="0 0 20 20">
                <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"/>
              </svg>
            </div>

            {productosOpen && (
              <div className="absolute top-full left-0 w-56 bg-white text-gray-600 rounded-2xl shadow-xl py-3 z-50 border border-gray-100">
                <Link 
                  to="/productos/hospitalaria" 
                  className="block px-5 py-2.5 hover:bg-gray-50 text-xs font-bold text-[#1a2530] transition"
                >
                  Línea Hospitalaria
                </Link>
                <Link 
                  to="/productos/horeca" 
                  className="block px-5 py-2.5 hover:bg-gray-50 text-xs font-bold text-[#1a2530] transition"
                >
                  Línea Horeca
                </Link>
              </div>
            )}
          </div>
        </nav>

        {/* Acciones */}
        <div className="flex items-center gap-5">
          <button className="text-white hover:text-[#003875] transition text-base">🔍</button>
          <button className="hidden sm:inline-block bg-white text-[#555] font-semibold px-6 py-2 rounded-xl text-sm hover:bg-gray-50 shadow-sm transition">
            Contáctenos
          </button>
        </div>

      </div>
    </header>
  );
}