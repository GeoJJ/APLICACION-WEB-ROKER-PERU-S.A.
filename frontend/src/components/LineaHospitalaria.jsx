import React from 'react';

export default function LineaHospitalaria() {
  const productos = [
    { id: 1, nombre: "Hibiclen 4%", desc: "Gluconato de Clorhexidina 4% para lavado quirúrgico de manos.", img: "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&q=80&w=400" },
    { id: 2, nombre: "Multizim P 20%", desc: "Detergente multienzimático bacteriostático concentrado.", img: "https://images.unsplash.com/photo-1584744982491-665216d95f8b?auto=format&fit=crop&q=80&w=400" },
    { id: 3, nombre: "Supersafe D", desc: "Desinfectante de alto nivel para instrumental médico.", img: "https://images.unsplash.com/photo-1579684385127-1ef15d508118?auto=format&fit=crop&q=80&w=400" },
  ];

  return (
    <div className="bg-white py-16 px-6 md:px-12">
      <div className="max-w-7xl mx-auto space-y-10">
        <div>
          <span className="text-xs font-bold text-[#00c5d6] uppercase tracking-widest">PRODUCTOS</span>
          <h1 className="text-3xl md:text-4xl font-extrabold text-[#1a2530] mt-1">Línea Hospitalaria</h1>
          <p className="text-xs md:text-sm text-gray-500 mt-2">Soluciones de bioseguridad formuladas para entornos clínicos de alta exigencia.</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
          {productos.map((item) => (
            <div key={item.id} className="border border-gray-100 rounded-3xl p-6 shadow-sm hover:shadow-md transition bg-white space-y-4">
              <img src={item.img} alt={item.nombre} className="w-full h-48 object-cover rounded-2xl" />
              <h3 className="text-lg font-bold text-[#1a2530]">{item.nombre}</h3>
              <p className="text-xs text-gray-500 leading-relaxed">{item.desc}</p>
              <button className="bg-[#a3d8f4] text-[#1e282d] font-bold px-5 py-2 rounded-full text-xs hover:opacity-80 transition w-full">
                Ver Ficha Técnica
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}