import React from 'react';
import Servicio1 from '../assets/servicio1.jpg';
import Servicio2 from '../assets/servicio2.jpg';

export default function Servicios() {

  return (
    <div className="bg-white py-6 px-6 md:px-12">
      <div className="max-w-7xl mx-auto space-y-6">
        
        <div>
          <span className="text-[11px] font-bold text-[#00c5d6] uppercase tracking-wider block mb-0.5">
            SERVICIOS
          </span>
          <h1 className="text-3xl md:text-[34px] font-semibold text-[#1d2730] tracking-tight leading-tight">
            Servicio de Maquila
          </h1>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center pt-2">
          
          <div className="lg:col-span-6 space-y-6">
            
            <div className="flex gap-4 items-start">
              <div className="w-11 h-11 bg-gray-100 rounded-2xl flex items-center justify-center flex-shrink-0 text-gray-600 shadow-sm">
                <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"/>
                </svg>
              </div>
              <div>
                <h3 className="text-sm font-semibold text-[#00c5d6] mb-1">
                  Servicio de Fabricación
                </h3>
                <p className="text-xs text-gray-500 leading-relaxed">
                  ROKER ofrece servicio de fabricación de productos farmacéuticos, los mismos que se elaboran con eficacia, eficiencia e integridad, a la vanguardia de la tecnología moderna, acorde con las Normas Internacionales de Calidad vigentes, orientadas a la mejora continua de sus procesos y bajo el marco del cumplimiento de las normas de Buenas Prácticas de Manufactura (BPM), lo que nos ha permitido satisfacer la demanda de importantes empresas nacionales e internacionales.
                </p>
              </div>
            </div>

            <div className="flex gap-4 items-start">
              <div className="w-11 h-11 bg-gray-100 rounded-2xl flex items-center justify-center flex-shrink-0 text-gray-600 shadow-sm">
                <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
                </svg>
              </div>
              <div>
                <h3 className="text-sm font-semibold text-[#00c5d6] mb-1">
                  Servicio de Acondicionado
                </h3>
                <p className="text-xs text-gray-500 leading-relaxed">
                  Productos de uso clínico hospitalario como productos farmacéuticos líquidos y dispositivos médicos de clase I, II y III. Productos de uso público como productos sanitarios, en las líneas de cosméticos líquidos y de higiene doméstica, antisépticos, desinfectantes, esterilizantes en frío, detergentes enzimáticos y geles conductivos.
                </p>
              </div>
            </div>

            <div className="flex items-center gap-3 pt-1">
              <button className="bg-[#00c5d6] text-white font-semibold px-6 py-2.5 rounded-xl text-xs hover:bg-[#00b0c0] transition shadow-sm">
                Contáctanos
              </button>
              <button className="w-10 h-10 rounded-full bg-gray-100 text-[#00c5d6] flex items-center justify-center shadow-sm hover:bg-gray-200 transition text-xs pl-0.5">
                ▶
              </button>
            </div>

          </div>

          <div className="lg:col-span-6 relative flex justify-end items-center">
            <div className="relative w-full max-w-lg h-[360px]">
              <img 
                src={Servicio2} 
                alt="Equipo Roker Planificación" 
                className="absolute right-0 top-0 w-2/3 h-full object-cover rounded-3xl shadow-sm"
              />
              <img 
                src={Servicio1} 
                alt="Asesoría técnica" 
                className="absolute left-0 top-1/2 -translate-y-1/2 w-3/5 h-[280px] object-cover rounded-3xl shadow-xl border-4 border-white z-10"
              />
            </div>
          </div>

        </div>

        <div className="border-t border-gray-100 pt-8 space-y-2">
          <h2 className="text-2xl font-semibold text-[#1d2730]">
            Análisis de Medicamentos y Productos Afines
          </h2>
          <p className="text-xs text-gray-500 max-w-5xl leading-relaxed">
            El laboratorio ROKER tiene como objetivo contribuir con la realización de análisis de control de calidad a los diferentes productos farmacéuticos, dispositivos médicos y productos sanitarios de acuerdo a las BPL y las normativas vigentes.
          </p>
        </div>

      </div>
    </div>
  );
}