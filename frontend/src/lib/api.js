const base = (import.meta.env.VITE_API_URL || '/api').replace(/\/$/, '');
export async function api(path, options = {}) {
 const method = options.method || 'GET';
 const headers = { ...options.headers };
 if (options.body) headers['Content-Type'] = 'application/json';
 try {
  if (!['GET','HEAD','OPTIONS'].includes(method)) {
   const tokenResponse = await fetch(base + '/auth/csrf', { credentials: 'include' });
   if (!tokenResponse.ok) throw new Error('No se pudo preparar la sesión.');
   const csrf = await tokenResponse.json(); headers[csrf.headerName] = csrf.token;
  }
  const response = await fetch(base + path, { ...options, headers, credentials: 'include' });
  if (!response.ok) {
   const detail = await response.json().catch(() => ({}));
   const error = new Error(detail.message || 'No se pudo completar la operación.');
   error.status = response.status;
   if (response.status === 401 && path !== '/auth/login') window.dispatchEvent(new Event('roker-session-expired'));
   throw error;
  }
  if (options.blob) return response.blob();
  if (response.status === 204 || response.headers.get('content-length') === '0') return null;
  const text = await response.text(); return text ? JSON.parse(text) : null;
 } catch (error) {
  if (error instanceof TypeError) throw new Error('No se pudo conectar con el servidor. Comprueba que el backend esté ejecutándose.', { cause: error });
  throw error;
 }
}
