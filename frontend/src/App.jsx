import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import HeroSlider from './components/HeroSlider';
import InfoSection from './components/InfoSection';
import WhyChoose from './components/WhyChoose';
import Servicios from './components/Servicios';
import Nosotros from './components/Nosotros';
import LineaHospitalaria from './components/LineaHospitalaria';
import LineaHoreca from './components/LineaHoreca';

export default function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          {/* Inicio */}
          <Route path="/" element={
            <>
              <HeroSlider />
              <InfoSection />
              <WhyChoose />
            </>
          } />

          {/* Empresa / Nosotros */}
          <Route path="/nosotros" element={<Nosotros />} />

          {/* Servicios */}
          <Route path="/servicios" element={<Servicios />} />

          {/* Productos */}
          <Route path="/productos/hospitalaria" element={<LineaHospitalaria />} />
          <Route path="/productos/horeca" element={<LineaHoreca />} />
        </Routes>
      </Layout>
    </Router>
  );
}