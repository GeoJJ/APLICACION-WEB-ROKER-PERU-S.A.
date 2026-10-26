const API_URL = 'http://localhost:8080';

export const obtenerProductos = async () => {
  try {
    const response = await fetch(`${API_URL}/productos/listar`);
    if (!response.ok) throw new Error('Error al obtener los productos');
    return await response.json();
  } catch (error) {
    console.error('Error:', error);
    return [];
  }
};
