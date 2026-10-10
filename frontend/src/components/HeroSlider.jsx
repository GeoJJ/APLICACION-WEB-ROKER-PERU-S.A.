import React from 'react';
import fotoInicio from '../assets/FotoInicio.jpg';


export default function HeroSlider() {
  return (
    <div className="relative min-h-[500px] md:min-h-[600px] flex items-center justify-center overflow-hidden bg-gray-900">
      
      <div className="absolute inset-0 z-0">
        <img 
          src={fotoInicio} 
          alt="Bioseguridad Roker" 
          className="w-full h-full object-cover object-center opacity-85"
        />
        <div className="absolute inset-0 bg-black/25"></div>
      </div>

      <div className="max-w-4xl mx-auto px-6 py-20 text-center relative z-10 text-white flex flex-col items-center">
        
        <span className="text-xs md:text-sm font-semibold tracking-widest uppercase mb-3 text-white/90 drop-shadow">
          EXPERTOS EN BIOSEGURIDAD
        </span>

        <h1 className="text-4xl md:text-6xl font-semibold tracking-tight leading-tight mb-4 drop-shadow-md">
          Protección con respaldo científico
        </h1>

        <p className="text-xs md:text-sm max-w-2xl text-white/90 font-normal leading-relaxed mb-8 drop-shadow">
          Desarrollamos soluciones respaldadas por certificaciones, estudios de eficacia y buenas prácticas de fabricación para fortalecer la bioseguridad en cada centro de salud.
        </p>

        <button className="bg-white text-gray-600 font-bold px-8 py-2.5 rounded-full text-sm hover:bg-gray-100 transition duration-200 shadow-md">
          Ver Más
        </button>
      </div>

      <button className="absolute left-4 top-1/2 -translate-y-1/2 z-10 text-white/70 hover:text-white text-2xl transition p-2">
        ‹
      </button>

      <button className="absolute right-4 top-1/2 -translate-y-1/2 z-10 text-white/70 hover:text-white text-2xl transition p-2">
        ›
      </button>

    </div>
  );
}