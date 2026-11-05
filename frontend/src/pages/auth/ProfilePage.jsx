import { useState } from 'react';
import { UserRound } from 'lucide-react';
import { useAuth } from '@/components/auth/AuthProvider';
import { SectionHeader, Panel, Field } from '@/components/shared/ManagementUI';
import Button from '@/components/ui/Button';
export default function ProfilePage() {
 const {user,role,profile}=useAuth();const [form,setForm]=useState({...user,currentPassword:'',password:''});const [message,setMessage]=useState('');const [busy,setBusy]=useState(false);
 async function save(e){e.preventDefault();setBusy(true);setMessage('');try{await profile(form);setForm({...form,password:'',currentPassword:''});setMessage('Perfil actualizado correctamente.');}catch(e){setMessage(e.message);}finally{setBusy(false);}}
 return <div className="mx-auto max-w-3xl px-6 py-12"><SectionHeader icon={UserRound} title="Mi perfil" description={role?.label} /><Panel><form onSubmit={save} className="grid gap-5 sm:grid-cols-2">{[['nombre','Nombre completo','text'],['email','Correo electrónico','email'],['telefono','Teléfono','tel'],['direccion','Dirección','text'],['currentPassword','Contraseña actual','password'],['password','Nueva contraseña (opcional)','password']].map(([key,label,type])=><Field key={key} label={label}><input className="bo-input" type={type} autoComplete={key==='currentPassword'?'current-password':key==='password'?'new-password':undefined} required={['nombre','email'].includes(key)} minLength={key==='password'?8:undefined} pattern={key==='telefono'?'[0-9]{9}':undefined} value={form[key]||''} onChange={e=>setForm({...form,[key]:e.target.value})} /></Field>)}<p className="text-xs text-gray-500 sm:col-span-2">Para cambiar el correo o la contraseña, confirma tu contraseña actual.</p>{message&&<p role="status" className="text-sm text-brand sm:col-span-2">{message}</p>}<Button type="submit" disabled={busy}>{busy?'Guardando...':'Guardar cambios'}</Button></form></Panel></div>;
}
