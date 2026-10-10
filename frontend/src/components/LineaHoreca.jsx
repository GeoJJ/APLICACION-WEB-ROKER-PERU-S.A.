import React from 'react';

export default function LineaHoreca() {
  const productos = [
    { id: 1, nombre: "Desinfectante de Superficies Horeca", desc: "Higienizante de grado alimentario para cocinas e industrias.", img: "https://images.unsplash.com/photo-1585421514284-efb74c2b69ba?auto=format&fit=crop&q=80&w=400" },
    { id: 2, nombre: "Jabón Antiséptico Espuma", desc: "Limpieza profunda y suave para manipulación de alimentos.", img: "https://images.unsplash.com/photo-1600880292203-757bb62b4baf?auto=format&fit=crop&q=80&w=400" },
  ];

  return (
    <div className="bg-white py-16 px-6 md:px-12">
      <div className="max-w-7xl mx-auto space-y-10">
        <div>
          <span className="text-xs font-bold text-[#00c5d6] uppercase tracking-widest">PRODUCTOS</span>
          <h1 className="text-3xl md:text-4xl font-extrabold text-[#1a2530] mt-1">Línea Horeca</h1>
          <p className="text-xs md:text-sm text-gray-500 mt-2">Higiene e inocuidad especializada para hoteles, restaurantes y catering.</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
          {productos.map((item) => (
            <div key={item.id} className="border border-gray-100 rounded-3xl p-6 shadow-sm hover:shadow-md transition bg-white space-y-4">
              <img src={item.img} alt={item.nombre} className="w-full h-48 object-cover rounded-2xl" />
              <h3 className="text-lg font-bold text-[#1a2530]">{item.nombre}</h3>
              <p className="text-xs text-gray-500 leading-relaxed">{item.desc}</p>
              <button className="bg-[#a3d8f4] text-[#1e282d] font-bold px-5 py-2 rounded-full text-xs hover:opacity-80 transition w-full">
                Ver Detalles
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}