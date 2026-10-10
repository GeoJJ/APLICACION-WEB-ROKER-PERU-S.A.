import React from 'react';
import { Link } from 'react-router-dom';
import logo from '../assets/logo.png';

export default function Footer() {
  return (
    <footer className="bg-[#1e282d] text-gray-300 pt-14 pb-8 px-6 md:px-12 text-xs font-sans">
      <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-4 gap-10 border-b border-gray-700/60 pb-12">
        
        <div className="space-y-4">
          <Link to="/">
            <img src={logo} alt="Roker Perú" className="h-10 w-auto object-contain brightness-0 invert" />
          </Link>
          <p className="text-gray-400 leading-relaxed text-[11px]">
            Somos un Laboratorio dedicado a la fabricación y comercialización de productos especializados en bioseguridad.
          </p>
          <div className="flex gap-2 pt-2">
            <a href="#" className="w-7 h-7 rounded bg-[#a3d8f4] text-[#1e282d] font-bold flex items-center justify-center hover:opacity-80 transition">f</a>
            <a href="#" className="w-7 h-7 rounded bg-[#a3d8f4] text-[#1e282d] font-bold flex items-center justify-center hover:opacity-80 transition">𝕏</a>
            <a href="#" className="w-7 h-7 rounded bg-[#a3d8f4] text-[#1e282d] font-bold flex items-center justify-center hover:opacity-80 transition">📷</a>
          </div>
        </div>


        <div>
          <h4 className="text-white font-bold text-sm mb-3">Enlaces de Interés</h4>
          <div className="w-6 h-[2px] bg-white/40 mb-4"></div>
          <ul className="space-y-2.5 text-gray-300">
            <li><Link to="/nosotros" className="hover:text-white transition">→ Nosotros</Link></li>
            <li><Link to="/servicios" className="hover:text-white transition">→ Servicios</Link></li>
            <li><Link to="/productos/hospitalaria" className="hover:text-white transition">→ Línea Hospitalaria</Link></li>
            <li><Link to="/productos/horeca" className="hover:text-white transition">→ Línea Horeca</Link></li>
            <li><a href="#" className="hover:text-white transition">→ Certificaciones</a></li>
            <li><a href="#" className="hover:text-white transition">→ Contacto</a></li>
          </ul>
        </div>


        <div>
          <h4 className="text-white font-bold text-sm mb-3">Canal de denuncias</h4>
          <div className="w-6 h-[2px] bg-white/40 mb-4"></div>
          <div className="space-y-4">
            <div className="flex gap-3 items-start">
              <span className="p-1.5 bg-[#a3d8f4] text-[#1e282d] rounded text-xs flex-shrink-0">🛡️</span>
              <div>
                <div className="text-white font-semibold text-[11px]">Oficial de Cumplimiento Antisoborno</div>
              </div>
            </div>


            <div className="flex gap-3 items-start">
              <span className="p-1.5 bg-[#a3d8f4] text-[#1e282d] rounded text-xs flex-shrink-0">✉️</span>
              <div>
                <div className="text-white font-semibold text-[11px]">Email</div>
                <div className="text-gray-400 text-[11px]">denuncias@roker.com.pe</div>
              </div>
            </div>


            <div className="flex gap-3 items-start">
              <span className="p-1.5 bg-[#a3d8f4] text-[#1e282d] rounded text-xs flex-shrink-0">📞</span>
              <div>
                <div className="text-white font-semibold text-[11px]">Teléfono</div>
                <div className="text-gray-400 text-[11px]">+51(1) 715 0191</div>
              </div>
            </div>

            <div className="pt-1 space-y-2">
              <a href="#" className="text-gray-300 hover:text-white block">→ Reporte y Atención de Denuncias</a>
              <a href="#" className="text-gray-300 hover:text-white block">→ Libro de Reclamaciones</a>
            </div>
          </div>
        </div>


        <div>
          <h4 className="text-white font-bold text-sm mb-3">Contáctenos</h4>
          <div className="w-6 h-[2px] bg-white/40 mb-4"></div>
          <div className="space-y-4">
            <div className="flex gap-3 items-start">
              <span className="p-1.5 bg-[#a3d8f4] text-[#1e282d] rounded text-xs flex-shrink-0">📞</span>
              <div>
                <div className="text-white font-semibold text-[11px]">Llámenos</div>
                <div className="text-gray-400 text-[11px]">+51 (1) 719 0707</div>
              </div>
            </div>

            <div className="flex gap-3 items-start">
              <span className="p-1.5 bg-[#a3d8f4] text-[#1e282d] rounded text-xs flex-shrink-0">✉️</span>
              <div>
                <div className="text-white font-semibold text-[11px]">Escríbanos</div>
                <div className="text-gray-400 text-[11px]">info@roker.com.pe</div>
              </div>
            </div>

            <div className="flex gap-3 items-start">
              <span className="p-1.5 bg-[#a3d8f4] text-[#1e282d] rounded text-xs flex-shrink-0">📍</span>
              <div>
                <div className="text-white font-semibold text-[11px]">Ubícanos</div>
                <div className="text-gray-400 text-[11px] leading-tight">
                  Calle La Milla nro. 220 Urb. La Milla, San Martín de Porres, Lima - Perú
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>



      <div className="max-w-7xl mx-auto pt-6 flex flex-col md:flex-row justify-between items-center text-gray-400 text-[11px] gap-4">
        <p>©2026 - Roker Perú. Todos los derechos reservados.</p>
        <div className="flex gap-6">
          <a href="#" className="hover:text-white transition">Políticas de Privacidad</a>
          <a href="#" className="hover:text-white transition">Términos y Condiciones</a>
        </div>
      </div>
    </footer>
  );
}