package RokerPeru.demo;

import RokerPeru.demo.config.InitialData;
import RokerPeru.demo.repository.UsuarioRepository;
import RokerPeru.demo.repository.RolRepository;
import java.net.*;
import java.net.http.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.DefaultApplicationArguments;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTests {
 @LocalServerPort int port;
 @Autowired InitialData initial;
 @Autowired UsuarioRepository users;
 @Autowired RolRepository roles;
 final JsonMapper json=JsonMapper.builder().build();
 HttpClient client(){return HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();}
 HttpResponse<String> call(HttpClient c,String method,String path,String body) throws Exception {
  var b=HttpRequest.newBuilder(URI.create("http://localhost:"+port+path));
  if(!method.equals("GET")) {
   var t=call(c,"GET","/api/auth/csrf",null);var token=json.readTree(t.body());
   b.header(token.get("headerName").asText(),token.get("token").asText()).header("Content-Type","application/json");
  }
  return c.send(b.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
 }
 JsonNode ok(HttpClient c,String method,String path,String body) throws Exception {
  var r=call(c,method,path,body);assertTrue(r.statusCode()>=200&&r.statusCode()<300,()->path+" "+r.statusCode()+" "+r.body());
  return r.body().isBlank()?null:json.readTree(r.body());
 }
 HttpClient login(String name) throws Exception {var c=client();ok(c,"POST","/api/auth/login","{\"email\":\""+name+"1@roker.com\",\"password\":\"Roker123\"}");return c;}
 @Test void authenticationRolesAndDatabaseFlows() throws Exception {
  long count=users.count();initial.run(new DefaultApplicationArguments(new String[0]));assertEquals(count,users.count());assertEquals(6,roles.count());
  assertEquals(401,call(client(),"GET","/api/panel",null).statusCode());
  assertEquals(401,call(client(),"POST","/api/auth/login","{\"email\":\"administrador1@roker.com\",\"password\":\"incorrecta\"}").statusCode());
  for(String name:List.of("administrador","almacenista","vendedor","repartidor","personal","cliente")) {
   var c=login(name);assertNotNull(ok(c,"GET","/api/auth/me",null).get("role"));ok(c,"GET","/api/panel",null);
   assertEquals(name.equals("administrador")?200:403,call(c,"GET","/admin/usuarios",null).statusCode());
  }
  var admin=login("administrador");var seller=login("vendedor");var warehouse=login("almacenista");var delivery=login("repartidor");var staff=login("personal");var customer=login("cliente");
  assertEquals(1,users.findByRol_NombreRolIgnoreCase("ADMINISTRADOR").size());
  assertTrue(users.findByCorreoUsuarioIgnoreCase("gerente1@roker.com").isEmpty());
  var snap=ok(admin,"POST","/api/panel/providers","{\"nombre\":\"Proveedor prueba\",\"ruc\":\"20123456789\",\"telefono\":\"999888777\",\"email\":\"proveedor@example.com\"}");
  long provider=snap.get("providers").get(0).get("id").asLong();
  snap=ok(admin,"POST","/api/panel/products","{\"codigo\":\"TEST-01\",\"producto\":\"Producto prueba\",\"categoria\":\"Guantes\",\"precio\":25,\"umbral\":5,\"estado\":\"Activo\",\"idProveedor\":"+provider+"}");
  long product=snap.get("products").get(0).get("id").asLong();
  ok(warehouse,"POST","/api/panel/movements","{\"productId\":"+product+",\"cantidad\":20,\"fecha\":\""+java.time.LocalDate.now()+"\",\"proveedor\":\"Proveedor prueba\"}");
  var sellerSnap=ok(seller,"GET","/api/panel",null);
  long customerId=sellerSnap.get("customers").get(0).get("id").asLong();
  long deliveryId=users.findByCorreoUsuarioIgnoreCase("repartidor1@roker.com").orElseThrow().getIdUsuario();
  long staffId=users.findByCorreoUsuarioIgnoreCase("personal1@roker.com").orElseThrow().getIdUsuario();
  snap=ok(seller,"POST","/api/panel/orders","{\"tipo\":\"Producto\",\"item\":"+product+",\"cantidad\":2,\"clienteId\":"+customerId+"}");
  String order=snap.get("orders").get(0).get("id").asText();
  assertEquals(18,snap.get("products").get(0).get("stock").asInt());
  ok(seller,"PUT","/api/panel/orders/"+order,"{\"estado\":\"En camino\",\"idDelivery\":"+deliveryId+"}");
  snap=ok(delivery,"GET","/api/panel",null);assertEquals(1,snap.get("deliveries").size());
  assertEquals(403,call(customer,"PUT","/api/panel/deliveries/"+order,"{\"estado\":\"Entregado\"}").statusCode());
  assertEquals(400,call(delivery,"PUT","/api/panel/deliveries/"+order,"{\"estado\":\"Entregado\"}").statusCode());
  ok(delivery,"PUT","/api/panel/deliveries/"+order,"{\"estado\":\"Entregado\",\"evidencia\":\"data:image/png;base64,aGVsbG8=\",\"firma\":\"data:image/png;base64,aGVsbG8=\"}");
  snap=ok(seller,"GET","/api/panel",null);assertEquals("Completado",snap.get("orders").get(0).get("estado").asText());
  snap=ok(admin,"POST","/api/panel/services","{\"nombre\":\"Limpieza prueba\",\"categoria\":\"Oficina\",\"precio\":100,\"personal\":1,\"estado\":\"Activo\"}");
  long service=snap.get("services").get(0).get("id").asLong();
  snap=ok(seller,"POST","/api/panel/orders","{\"tipo\":\"Servicio\",\"item\":"+service+",\"clienteId\":"+customerId+"}");
  String serviceId="";for(var o:snap.get("orders"))if(o.get("tipo").asText().equals("Servicio"))serviceId=o.get("id").asText();
  ok(seller,"PUT","/api/panel/orders/"+serviceId,"{\"estado\":\"En camino\",\"personalId\":"+staffId+"}");
  snap=ok(staff,"GET","/api/panel",null);assertEquals(1,snap.get("orders").size());
  ok(staff,"PUT","/api/panel/orders/"+serviceId,"{\"estado\":\"Completado\",\"observaciones\":\"Finalizado\"}");
  var report=call(admin,"GET","/api/reportes/productos/excel",null);assertEquals(200,report.statusCode());
  assertTrue(report.headers().firstValue("Content-Type").orElse("").contains("spreadsheetml"));
  var publicClient=client();assertEquals(200,call(publicClient,"GET","/api/public/products",null).statusCode());
  var preflight=publicClient.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/auth/login")).header("Origin","http://localhost:5173").header("Access-Control-Request-Method","POST").method("OPTIONS",HttpRequest.BodyPublishers.noBody()).build(),HttpResponse.BodyHandlers.ofString());
  assertEquals("http://localhost:5173",preflight.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
  var forbiddenOrigin=publicClient.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/auth/login")).header("Origin","https://other.example").header("Access-Control-Request-Method","POST").method("OPTIONS",HttpRequest.BodyPublishers.noBody()).build(),HttpResponse.BodyHandlers.ofString());
  assertEquals(403,forbiddenOrigin.statusCode());
  var noCsrf=admin.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/auth/logout")).POST(HttpRequest.BodyPublishers.noBody()).build(),HttpResponse.BodyHandlers.ofString());
  assertEquals(403,noCsrf.statusCode());
  ok(customer,"PUT","/api/auth/profile","{\"nombre\":\"Cliente actualizado\",\"email\":\"cliente1@roker.com\",\"telefono\":\"999111222\",\"direccion\":\"Av. Prueba 123\"}");
  assertEquals("Cliente actualizado",ok(customer,"GET","/api/auth/me",null).get("nombre").asText());
  assertEquals(400,call(customer,"PUT","/api/auth/profile","{\"nombre\":\"Cliente\",\"email\":\"otro@roker.com\",\"telefono\":\"999111222\",\"direccion\":\"Lima\"}").statusCode());
  verifyShoppingAndQuotes(admin,seller,customer,product,service);
  // Editing a worker changes API authorization on their next request, even with an existing session.
  var worker=users.findByCorreoUsuarioIgnoreCase("vendedor1@roker.com").orElseThrow();
  ok(admin,"PUT","/api/panel/workers/"+worker.getIdUsuario(),"{\"nombre\":\"Vendedor actualizado\",\"email\":\"vendedor1@roker.com\",\"documento\":\""+worker.getNumeroDocumentoUsuario()+"\",\"rol\":\"warehouse\",\"estado\":\"Activo\"}");
  assertEquals(403,call(seller,"GET","/vendedor/productos",null).statusCode());
  assertEquals("ALMACENISTA",ok(seller,"GET","/api/auth/me",null).get("role").asText());
  ok(admin,"POST","/api/auth/logout",null);assertEquals(401,call(admin,"GET","/api/auth/me",null).statusCode());
 }
 void verifyShoppingAndQuotes(HttpClient admin,HttpClient seller,HttpClient customer,long product,long service) throws Exception {
  assertEquals(400,call(admin,"POST","/api/panel/workers","{\"nombre\":\"Otro admin\",\"email\":\"otroadmin@roker.com\",\"documento\":\"88881111\",\"rol\":\"admin\",\"estado\":\"Activo\",\"password\":\"Roker123\"}").statusCode());
  String checkout="{\"key\":\"checkout-integration\",\"nombre\":\"Cliente entrega\",\"telefono\":\"999111222\",\"direccion\":\"Av. Uno 123\",\"distrito\":\"Lima\",\"metodoPago\":\"EFECTIVO\",\"items\":[{\"idProducto\":"+product+",\"cantidadProducto\":1}]}";
  assertEquals(403,call(seller,"POST","/api/checkout",checkout).statusCode());
  var receipt=ok(customer,"POST","/api/checkout",checkout);assertEquals(25,receipt.get("total").asDouble());
  var retry=ok(customer,"POST","/api/checkout",checkout);assertEquals(receipt.get("id").asLong(),retry.get("id").asLong());
  assertEquals(17,ok(seller,"GET","/api/panel",null).get("products").get(0).get("stock").asInt());
  String insufficient=checkout.replace("checkout-integration","checkout-too-many").replace("\"cantidadProducto\":1","\"cantidadProducto\":9999");
  assertEquals(400,call(customer,"POST","/api/checkout",insufficient).statusCode());
  assertEquals(17,ok(seller,"GET","/api/panel",null).get("products").get(0).get("stock").asInt());
  var publicServices=ok(client(),"GET","/api/public/services",null);assertFalse(publicServices.get(0).has("price"));
  String scheduled=java.time.LocalDateTime.now().plusDays(2).withSecond(0).withNano(0).toString();
  String request="{\"servicioId\":"+service+",\"ambiente\":\"Oficina\",\"area\":100,\"ambientes\":2,\"direccion\":\"Av. Dos 456\",\"distrito\":\"Lima\",\"programada\":\""+scheduled+"\",\"frecuencia\":\"Semanal\",\"necesidades\":\"Limpieza de pisos y ventanas\",\"nombre\":\"Cliente\",\"telefono\":\"999111222\",\"email\":\"cliente1@roker.com\"}";
  long countBefore=ok(seller,"GET","/api/panel",null).get("orders").size();
  var submitted=ok(customer,"POST","/api/solicitudes",request);long id=submitted.get("id").asLong();assertEquals("PENDIENTE_COTIZACION",submitted.get("estado").asText());
  assertEquals(countBefore,ok(seller,"GET","/api/panel",null).get("orders").size());
  var second=client();ok(second,"POST","/api/auth/register","{\"nombre\":\"Cliente Dos\",\"email\":\"cliente2@roker.com\",\"documento\":\"88882222\",\"password\":\"Roker123\"}");ok(second,"POST","/api/auth/login","{\"email\":\"cliente2@roker.com\",\"password\":\"Roker123\"}");
  assertEquals(0,ok(second,"GET","/api/solicitudes",null).size());
  assertEquals(403,call(second,"PUT","/api/solicitudes/"+id+"/aceptar",null).statusCode());
  String quote="{\"precio\":275,\"programada\":\""+scheduled+"\",\"observaciones\":\"Incluye insumos\"}";
  assertEquals(403,call(customer,"PUT","/api/solicitudes/"+id+"/cotizar",quote).statusCode());
  ok(seller,"PUT","/api/solicitudes/"+id+"/cotizar",quote);
  var accepted=ok(customer,"PUT","/api/solicitudes/"+id+"/aceptar",null);assertEquals("ACEPTADA",accepted.get("estado").asText());
  var acceptedAgain=ok(customer,"PUT","/api/solicitudes/"+id+"/aceptar",null);assertEquals(accepted.get("ordenId"),acceptedAgain.get("ordenId"));
  var orders=ok(seller,"GET","/api/panel",null).get("orders");assertEquals(countBefore+1,orders.size());
  boolean quotedTotal=false;for(var o:orders)if(o.get("id").asText().equals("S-"+accepted.get("ordenId").asLong()))quotedTotal=o.get("total").asDouble()==275;
  assertTrue(quotedTotal);
 }
}
