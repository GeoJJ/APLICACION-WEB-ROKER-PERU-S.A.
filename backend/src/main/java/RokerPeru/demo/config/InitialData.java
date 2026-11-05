package RokerPeru.demo.config;
import RokerPeru.demo.entities.*;
import RokerPeru.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @RequiredArgsConstructor
public class InitialData implements ApplicationRunner {
 private final RolRepository roles; private final UsuarioRepository users;
 private final EmpleadosServicioRepository employees; private final PasswordEncoder encoder;
 private final SecuenciaComprobanteRepository sequences;
 private final ProveedoresRepository providers; private final ServicioRepository services;
 private final org.springframework.jdbc.core.JdbcTemplate jdbc;
 @Value("${app.seed.enabled:true}") private boolean enabled;
 @Override @Transactional public void run(ApplicationArguments args) {
  for(var type:RokerPeru.demo.enums.TipoComprobanteEnum.values())
   if(sequences.findBySerie(type.getSerie()).isEmpty()) sequences.save(new SecuenciaComprobante(type.getSerie(),0));
  String[][] entries={{"ADMINISTRADOR","administrador1","Administrador"},{"ALMACENISTA","almacenista1","Almacenista"},{"VENDEDOR","vendedor1","Vendedor"},{"REPARTIDOR","repartidor1","Repartidor"},{"PERSONAL","personal1","Personal de servicio"},{"CLIENTE","cliente1","Cliente"}};
  for(int i=0;i<entries.length;i++) {
   var entry=entries[i];
   var role=roles.findByNombreRolIgnoreCase(entry[0]).orElseGet(()->{var r=new Rol();r.setNombreRol(entry[0]);return roles.save(r);});
   if(!enabled || users.existsByCorreoUsuarioIgnoreCase(entry[1]+"@roker.com"))continue;
   if("ADMINISTRADOR".equals(entry[0])&&!users.findByRol_NombreRolIgnoreCase("ADMINISTRADOR").isEmpty())continue;
   int document=90000001+i;
   while(users.existsByNumeroDocumentoUsuario(String.valueOf(document)))document+=10;
   var u=new Usuario();u.setNombreUsuario(entry[2]);u.setCorreoUsuario(entry[1]+"@roker.com");
   u.setNumeroDocumentoUsuario(String.valueOf(document));u.setTipoPersonaUsuario("NATURAL");u.setRol(role);
   u.setPasswordUsuario(encoder.encode("Roker123"));u.setActivo(true);users.save(u);
   if("PERSONAL".equals(entry[0])){var e=new EmpleadosServicio();e.setNombreEmpleadoServicio("Personal");e.setApellidoEmpleadoServicio("de servicio");e.setUsuario(u);employees.save(e);}
  }
  // Migrate existing accounts without deleting them or changing their passwords.
  var admin=roles.findByNombreRolIgnoreCase("ADMINISTRADOR").orElseThrow();
  var oldRole=roles.findByNombreRolIgnoreCase("GERENTE");
  for(var user:users.findAllByOrderByNombreUsuarioAsc()) {
   boolean wasManager="GERENTE".equalsIgnoreCase(user.getRol().getNombreRol());
   if(wasManager)user.setRol(admin);
   String name=user.getNombreUsuario().replaceAll("(?i)\\s+de prueba\\b","").trim();
   if(wasManager && name.equalsIgnoreCase("GERENTE"))name="Administrador";
   if(name.equals("ADMINISTRADOR"))name="Administrador";
   user.setNombreUsuario(name);
  }
  users.flush();
  mergeLegacyAdministrator();
  oldRole.ifPresent(roles::delete);
  for(var employee:employees.findAll()){
   employee.setNombreEmpleadoServicio(employee.getNombreEmpleadoServicio().replaceAll("(?i)\\s*de prueba\\b","").trim());
   String surname=employee.getApellidoEmpleadoServicio().replaceAll("(?i)\\s*de prueba\\b","").trim();
   employee.setApellidoEmpleadoServicio(surname.isBlank()?"de servicio":surname);
  }
  if(enabled){repairCatalogSequences();seedCatalog();}
 }
 private void mergeLegacyAdministrator(){
  var primary=users.findByCorreoUsuarioIgnoreCase("administrador1@roker.com");var legacy=users.findByCorreoUsuarioIgnoreCase("gerente1@roker.com");
  if(primary.isEmpty()||legacy.isEmpty())return;
  Long target=primary.get().getIdUsuario(),source=legacy.get().getIdUsuario();
  for(String column:new String[]{"id_cliente","id_vendedor","id_delivery","id_auxiliar"})jdbc.update("UPDATE pedidos SET "+column+"=? WHERE "+column+"=?",target,source);
  for(String column:new String[]{"id_cliente","id_vendedor","id_auxiliar"})jdbc.update("UPDATE ordenes_servicio SET "+column+"=? WHERE "+column+"=?",target,source);
  jdbc.update("UPDATE solicitudes_servicio SET id_cliente=? WHERE id_cliente=?",target,source);
  var list=employees.findAll();boolean linked=list.stream().anyMatch(e->e.getUsuario()!=null&&e.getUsuario().getIdUsuario().equals(target));
  for(var employee:list)if(employee.getUsuario()!=null&&employee.getUsuario().getIdUsuario().equals(source)){employee.setUsuario(linked?null:primary.get());linked=true;}
  employees.flush();users.delete(legacy.get());users.flush();
 }
 private void repairCatalogSequences(){
  // Existing databases may contain manually inserted IDs ahead of their identity sequence.
  jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) connection -> {
   if(!connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL"))return null;
   for(String[] entry:new String[][]{{"proveedores","id_proveedor"},{"servicios","id_servicio"}}){
    try(var statement=connection.createStatement()){
     statement.execute("LOCK TABLE "+entry[0]+" IN SHARE ROW EXCLUSIVE MODE");
     String sequence="pg_get_serial_sequence('"+entry[0]+"','"+entry[1]+"')";
     statement.execute("SELECT setval("+sequence+", GREATEST(COALESCE((SELECT MAX("+entry[1]+") FROM "+entry[0]+"),0),COALESCE(pg_sequence_last_value("+sequence+"::regclass),0),1),true) WHERE "+sequence+" IS NOT NULL");
    }
   }
   return null;
  });
 }
 private void seedCatalog(){
  String[][] entries={{"20300000001","Suministros Integrales Roker","987111222","ventas@suministros.example.com"},{"20300000002","Distribuciones de Higiene del Perú","987333444","ventas@higiene.example.com"}};
  for(var entry:entries)if(!providers.existsByRucProveedor(entry[0])){
   var p=new Proveedores();p.setRucProveedor(entry[0]);p.setNombreProveedor(entry[1]);p.setTelefonoProveedor(entry[2]);p.setCorreoProveedor(entry[3]);providers.save(p);
  }
  addService("Limpieza de Oficinas","oficina","Limpieza integral de escritorios, pisos y áreas comunes de oficinas.",150.0,"3 horas");
  addService("Desinfección de Ambientes","desinfeccion","Desinfección de ambientes y superficies de alto contacto.",180.0,"2 horas");
 }
 private void addService(String name,String category,String description,double price,String duration){
  if(services.findAll().stream().anyMatch(s->s.getNombreServicio().equalsIgnoreCase(name)))return;
  var s=new Servicio();s.setNombreServicio(name);s.setCategoriaServicio(category);s.setTipoServicio("LIMPIEZA");
  s.setDescripcionServicio(description);s.setPrecioServicio(price);s.setDuracionEstimadaServicio(duration);s.setCantidadPersonalServicio("1");s.setActivo(true);services.save(s);
 }
}
