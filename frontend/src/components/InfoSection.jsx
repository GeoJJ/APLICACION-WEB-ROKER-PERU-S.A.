import React from 'react';
import fotoInicio2 from '../assets/FotoInicio2.jpg';

export default function InfoSection() {
  return (
    <section className="bg-white py-16 px-6 md:px-12">
      <div className="max-w-7xl mx-auto grid md:grid-cols-2 gap-12 items-center">
        
        <div className="relative flex justify-center">
          <img 
            src={fotoInicio2} 
            alt="Laboratorio Roker - Bioseguridad" 
            className="rounded-3xl shadow-md w-full max-w-lg object-cover" 
          />
        </div>

        <div className="space-y-6">
          <div>
            <span className="text-xs font-bold text-gray-500 uppercase tracking-widest">
              SOMOS ROKER
            </span>
            <h2 className="text-3xl md:text-4xl font-bold text-[#1a2530] mt-1">
              Expertos en Bioseguridad
            </h2>
          </div>

          <p className="text-gray-500 leading-relaxed text-sm">
            Con más de 35 años de experiencia, desarrollamos, fabricamos y comercializamos soluciones especializadas en higiene, desinfección y bioseguridad para el sector salud. Combinamos innovación, estándares internacionales y cumplimiento normativo para ofrecer productos confiables que fortalecen la seguridad, optimizan los procesos y generan confianza en cada institución.
          </p>

          <p className="text-gray-500 leading-relaxed text-sm">
            Nuestro propósito es impulsar un mundo más seguro y saludable mediante soluciones innovadoras en bioseguridad.
          </p>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-4">

            <div className="bg-[#a3d8f4] p-5 rounded-2xl flex items-center gap-4">
              <div className="bg-white p-3.5 rounded-xl text-gray-700 flex items-center justify-center shadow-sm">
                <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
                </svg>
              </div>
              <div>
                <div className="text-3xl font-bold text-[#2b3a4a]">+ 500</div>
                <div className="text-[11px] font-bold text-gray-600 tracking-wider uppercase leading-tight">
                  CLIENTES<br/>ATENDIDOS
                </div>
              </div>
            </div>

            <div className="bg-[#a3d8f4] p-5 rounded-2xl flex items-center gap-4">
              <div className="bg-white p-3.5 rounded-xl text-gray-700 flex items-center justify-center shadow-sm">
                <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
                </svg>
              </div>
              <div>
                <div className="text-3xl font-bold text-[#2b3a4a]">+ 200</div>
                <div className="text-[11px] font-bold text-gray-600 tracking-wider uppercase leading-tight">
                  PRODUCTOS<br/>DISPONIBLES
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </section>
  );
}