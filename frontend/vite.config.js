import react from '@vitejs/plugin-react';
import {defineConfig} from 'vite';
import {fileURLToPath,URL} from 'node:url';
import process from 'node:process';
import tailwindcss from '@tailwindcss/vite';
export default defineConfig({
 plugins:[react(),tailwindcss()],
 resolve:{alias:{'@':fileURLToPath(new URL('./src',import.meta.url))}},
 server:{port:5173,strictPort:true,proxy:{'/api':{target:process.env.API_PROXY_TARGET||'http://localhost:8080',changeOrigin:true,rewrite:path=>path.replace(/^\/api(?=\/|$)/,'')}}},
});
