import React from 'react';
import nosotros from '../assets/Nosotros.jpg';

export default function Nosotros() {

  return (
    <div className="bg-white py-6 px-6 md:px-12 font-sans">
      <div className="max-w-5xl mx-auto space-y-8">
        

        <div className="w-full h-[280px] md:h-[360px] rounded-3xl overflow-hidden shadow-sm">
          <img 
            src={nosotros} 
            alt="Laboratorio Roker - Investigación y Bioseguridad" 
            className="w-full h-full object-cover"
          />
        </div>


        <div className="space-y-3">
          <h2 className="text-2xl md:text-3xl font-semibold text-[#00c5d6] tracking-tight">
            Nosotros
          </h2>
          <div className="text-xs md:text-sm text-gray-600 leading-relaxed space-y-3 text-justify">
            <p>
              Somos un Laboratorio dedicado a la fabricación y comercialización de productos especializados en bioseguridad; en el contexto actual nos enfocamos en proporcionar a nuestros clientes una amplia gama de productos elaborados con estándares internacionales y normativa legal vigente. Nuestros <strong className="font-semibold text-gray-800">34 años de experiencia</strong> y las certificaciones internacionales con las que contamos garantizan la calidad de nuestros productos, precios justos y tiempos de entrega eficientes.
            </p>
            <p>
              Desde el 2005, contamos con la Certificación de Buenas Prácticas de Manufactura (<strong className="font-semibold text-gray-800">BPM</strong>) según los requisitos establecidos por el Ministerio de Salud, a través de la Dirección General de Medicamentos, Insumos y Drogas (DIGEMID).
            </p>
            <p>
              Nuestro Sistema Integrado de Gestión cuenta con alcance a nivel nacional e internacional que se aplica eficazmente en nuestros procedimientos operacionales, comerciales y administrativos, lo cual asegura la entrega de productos de calidad. Estamos acreditados en las normas internacionales <strong className="font-semibold text-gray-800">ISO 9001:2015</strong> Gestión de la Calidad, <strong className="font-semibold text-gray-800">ISO 14001:2015</strong> Gestión Ambiental e <strong className="font-semibold text-gray-800">ISO 13485:2016</strong> Gestión de la Calidad en Dispositivos Médicos. ROKER PERÚ también cuenta con la certificación <strong className="font-semibold text-gray-800">ISO 37001:2016</strong> para su Sistema de Gestión Anti-soborno reafirmando su compromiso en la lucha contra el soborno y la corrupción.
            </p>
            <p>
              Estamos comprometidos con la mejora continua de nuestros Sistemas de Gestión manteniendo los niveles de calidad de los productos que ofrecemos, creando relaciones de confianza con nuestros clientes y elevando su nivel de satisfacción.
            </p>
          </div>
        </div>


        <div className="space-y-2 pt-2">
          <h2 className="text-2xl md:text-3xl font-semibold text-[#00c5d6] tracking-tight text-justify">
            Nuestro Propósito
          </h2>
          <p className="text-xs md:text-sm text-gray-600 leading-relaxed">
            Impulsamos un mundo más seguro y saludable a través de la innovación en bioseguridad, llevando nuestra experiencia desde los hospitales hacia cada hogar y comunidad.
          </p>
        </div>


        <div className="space-y-2 pt-2">
          <h2 className="text-2xl md:text-3xl font-semibold text-[#00c5d6] tracking-tight text-justify">
            Nuestra Cultura
          </h2>
          <div className="text-xs md:text-sm text-gray-600 leading-relaxed space-y-2 text-justify">
            <p>
              En Roker creemos que la innovación tiene sentido cuando mejora la vida de las personas. Por ello, promovemos una cultura basada en la excelencia, el desarrollo tecnológico, la mejora continua y el compromiso con la bioseguridad.
            </p>
            <p>
              Trabajamos con integridad, pasión y responsabilidad para desarrollar soluciones que respondan a los desafíos presentes y futuros de la salud, contribuyendo al bienestar de las personas, al fortalecimiento de las instituciones y a la construcción de entornos más seguros y sostenibles.
            </p>
            <p>
              Entendemos que la bioseguridad no termina en un hospital; debe acompañar a las personas en cada espacio donde viven, trabajan y se desarrollan.
            </p>
          </div>
        </div>


        <div className="space-y-3 pt-2 pb-6">
          <h2 className="text-2xl md:text-3xl font-semibold text-[#00c5d6] tracking-tight">
            Principios
          </h2>
          <ul className="list-disc list-inside text-xs md:text-sm text-gray-600 space-y-1.5 pl-2">
            <li>La vida primero.</li>
            <li>Innovación que transforma.</li>
            <li>Tecnología al servicio de las personas.</li>
            <li>Calidad sin concesiones.</li>
            <li>Integridad en cada acción.</li>
            <li>Cercanía con nuestros clientes.</li>
            <li>Bioseguridad que trasciende hospitales.</li>
            <li>Sostenibilidad para el futuro.</li>
          </ul>
        </div>

      </div>
    </div>
  );
}