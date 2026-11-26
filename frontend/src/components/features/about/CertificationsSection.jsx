import bpm from '@/assets/about/bpm.png';
import iso37001 from '@/assets/about/iso_37001.png';
import iso13485 from '@/assets/about/iso_13485.png';
import iso9001 from '@/assets/about/iso_9001.png';
import iso14001 from '@/assets/about/iso_14001.png';
import auna from '@/assets/about/cliente_auna.png';
import goodHope from '@/assets/about/cliente_clinica_good_hope.png';
import peruanoJaponesa from '@/assets/about/cliente_clinica_peruano_japonesa.png';
import essalud from '@/assets/about/cliente_essalud.png';
import minsa from '@/assets/about/cliente_minsa.png';
import sanna from '@/assets/about/cliente_sanna.png';

const certifications = [
    { image: bpm, name: 'BPM', description: 'Buenas Prácticas de Manufactura' },
    { image: iso37001, name: 'ISO 37001', description: 'Gestión antisoborno' },
    { image: iso13485, name: 'ISO 13485', description: 'Calidad en dispositivos médicos' },
    { image: iso9001, name: 'ISO 9001', description: 'Gestión de la calidad' },
    { image: iso14001, name: 'ISO 14001', description: 'Gestión ambiental' },
];
const clients = [
    { image: auna, name: 'Auna' },
    { image: goodHope, name: 'Clínica Adventista Good Hope' },
    { image: peruanoJaponesa, name: 'Clínica Centenario Peruano Japonesa' },
    { image: essalud, name: 'EsSalud' },
    { image: minsa, name: 'Ministerio de Salud del Perú' },
    { image: sanna, name: 'SANNA' },
];

export default function CertificationsSection() {
    return <section id="certificaciones" aria-labelledby="certifications-title" className="mt-8 rounded-2xl border border-gray-200 bg-gray-50/80 px-5 py-9 md:p-10">
        <div className="text-center"><p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand">Certificaciones</p><h2 id="certifications-title" className="mx-auto mt-3 max-w-3xl text-2xl font-semibold leading-tight text-gray-900 md:text-3xl">Comprometidos con la mejora continua</h2></div>
        <ul aria-label="Certificaciones de Roker" className="mt-8 grid grid-cols-2 gap-5 sm:grid-cols-3 lg:grid-cols-5">
            {certifications.map(certification => <li key={certification.name} className="rounded-xl p-3 text-center transition hover:bg-white hover:shadow-sm">
                <img src={certification.image} alt={`Certificación ${certification.name}`} width="334" height="334" loading="lazy" className="mx-auto h-28 w-28 object-contain md:h-36 md:w-36" />
                <h3 className="mt-4 text-sm font-semibold text-gray-900">{certification.name}</h3><p className="mt-1 text-xs leading-5 text-gray-500">{certification.description}</p>
            </li>)}
        </ul>
        <div className="my-8 flex items-center gap-5"><span className="h-px flex-1 bg-gray-200" /><h2 className="text-center text-xs font-semibold uppercase tracking-wider text-gray-500">Nuestros clientes</h2><span className="h-px flex-1 bg-gray-200" /></div>
        <ul aria-label="Clientes publicados por Roker" className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
            {clients.map(client => <li key={client.name} className="flex min-h-24 items-center justify-center rounded-xl border border-gray-100 bg-white p-4 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"><img src={client.image} alt={client.name} width="197" height="48" loading="lazy" className="max-h-12 w-full object-contain" /></li>)}
        </ul>
    </section>;
}
