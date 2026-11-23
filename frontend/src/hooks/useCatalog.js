import { useEffect, useState } from 'react';
import { api } from '@/lib/api';

const PATHS = { products: '/cliente/productos', services: '/cliente/servicios' };

export function useCatalog(kind) {
    const [items, setItems] = useState([]), [loading, setLoading] = useState(true), [error, setError] = useState('');
    useEffect(() => {
        let active = true;
        api(PATHS[kind])
            .then(rows => {
                if (active) setItems(rows.map(r => ({ ...r, category: (r.category || '').toLowerCase() })));
            })
            .catch(e => { if (active) setError(e.message); })
            .finally(() => { if (active) setLoading(false); });
        return () => { active = false; };
    }, [kind]);
    return { items, loading, error };
}