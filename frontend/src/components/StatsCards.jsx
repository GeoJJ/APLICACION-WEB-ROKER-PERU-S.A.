import React from 'react';

export default function StatsCards() {
  const stats = [
    { number: "+ 500", label: "CLIENTES ATENDIDOS", icon: "👥" },
    { number: "+ 200", label: "PRODUCTOS DISPONIBLES", icon: "📋" }
  ];
  
  return (
    <section className="bg-white py-16 px-6 md:px-10">
      <div className="max-w-5xl mx-auto grid md:grid-cols-2 gap-8">
        {stats.map((stat) => (
          <div key={stat.label} className="bg-roker-light-blue p-10 rounded-3xl flex items-center gap-10 shadow-sm border border-gray-100/50">
            <div className="p-6 rounded-2xl bg-white text-3xl text-roker-blue flex-shrink-0 shadow-inner">
              {stat.icon}
            </div>
            <div className="flex flex-col">
              <span className="text-6xl font-extrabold text-roker-blue tracking-tight">{stat.number}</span>
              <span className="text-lg font-semibold text-gray-700 mt-1 tracking-wide">{stat.label}</span>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}