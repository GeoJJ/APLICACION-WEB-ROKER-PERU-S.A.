import React, { useState } from 'react';
import fotoInicio3 from '../assets/FotoInicio3.jpg';

export default function WhyChoose() {
  const [activeTab, setActiveTab] = useState(0);

  const features = [
    {
      id: "01",
      title: "Innovación con propósito",
      description: "Desarrollamos soluciones de bioseguridad respaldadas por investigación, experiencia y mejora continua, contribuyendo a la protección de las personas en hospitales, instituciones y hogares."
    },
    {
      id: "02",
      title: "Calidad que genera confianza",
      description: "Trabajamos bajo estrictos estándares de calidad y cumplimiento normativo, garantizando productos seguros, eficaces y confiables para nuestros clientes.."
    },
    {
      id: "03",
      title: "Cercanía que acompaña",
      description: "Brindamos asesoría especializada y atención oportuna, construyendo relaciones de largo plazo basadas en la confianza, el compromiso y el servicio."
    }
  ];

  return (
    <section className="bg-white py-16 px-6 md:px-12">
      <div className="max-w-7xl mx-auto">
        

        <div className="text-center mb-12">
          <span className="text-xs font-bold text-gray-400 uppercase tracking-widest">
            ¿PORQUÉ ELEGIRNOS?
          </span>
          <h2 className="text-3xl md:text-4xl font-bold text-[#1a2530] mt-1">
            Impulsamos la innovación en bioseguridad
          </h2>
        </div>


        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
          

          <div className="lg:col-span-5 space-y-4 z-10">
            {features.map((feature, index) => {
              const isActive = activeTab === index;
              return (
                <div
                  key={feature.id}
                  onClick={() => setActiveTab(index)}
                  className={`cursor-pointer rounded-2xl p-6 transition-all duration-300 flex gap-4 items-start shadow-sm border ${
                    isActive
                      ? 'bg-[#00c5d6] text-white border-[#00c5d6] shadow-lg transform scale-[1.02]'
                      : 'bg-white text-gray-700 border-gray-100 hover:border-gray-200 hover:shadow-md'
                  }`}
                >

                  <div
                    className={`text-sm font-bold px-3 py-2 rounded-lg flex items-center justify-center transition-colors ${
                      isActive
                        ? 'bg-white text-[#00c5d6]'
                        : 'bg-gray-100 text-[#00c5d6]'
                    }`}
                  >
                    {feature.id}
                  </div>


                  <div className="flex-1">
                    <h3
                      className={`text-lg font-bold mb-2 ${
                        isActive ? 'text-white' : 'text-[#1a2530]'
                      }`}
                    >
                      {feature.title}
                    </h3>
                    <p
                      className={`text-xs md:text-sm leading-relaxed ${
                        isActive ? 'text-white/90' : 'text-gray-500'
                      }`}
                    >
                      {feature.description}
                    </p>
                  </div>
                </div>
              );
            })}
          </div>


          <div className="lg:col-span-7 relative">
            <div className="relative rounded-3xl overflow-hidden shadow-xl border border-gray-100">
              <img
                src={fotoInicio3}
                alt="Laboratorio de bioseguridad Roker y Multizim P"
                className="w-full h-[450px] md:h-[520px] object-cover"
              />
              

              <div className="absolute inset-0 bg-black/5 pointer-events-none"></div>
            </div>
          </div>

        </div>

      </div>
    </section>
  );
}