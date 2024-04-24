package de.thm.mni.pi2.adder;

import io.vertx.core.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SSLOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.TrafficShapingOptions;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

/*
CONFIGURATION
main()-Methode: io.vertx.core.Launcher
Zu übergebendes Argument an die main()-Methode: run de.thm.mni.pi2.adder.MainVerticle
*/

public class MainVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) {
    HttpServer server = vertx.createHttpServer(); // Erstelle einen HttpServer
    Router router = Router.router(vertx); // Erstelle einen Router

    // Sessions
    SessionStore store = LocalSessionStore.create(vertx); // erstelle einen lokalen (auf dem EINEN Server) Session Speicher
    SessionHandler sessionsHandler = SessionHandler.create(store); // erstelle Handler für das Management dieses Speichers
    router.route().handler(sessionsHandler); // Alle Anfragen gehen durch den Session Handler

    // Allow CORS (IMMER erst zu erst vor den !!!)
    router.route().handler( // wähle alle eingehenden Anfragen aus, die über den Router gehen
      CorsHandler.create() // erstelle einen CorsHandler, der die CORS-Konfiguration für die eingehende Anfragen behandelt
        .addOrigin("http://localhost:63342") // lege die Quelle fest, von welcher Anfragen erlaubt sind
        .allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST) // lege die erlaubte(n) http Methode(n) fest, die von der angegebene Quelle verwendet werden können
        .allowCredentials(true) // Erlaubt Cookies und Authentifizierungsinformationen
    );

    // parse den body der http Anfrage
    router.route().handler( // wähle alle eingehden Anfragen aus, die über den Router gehen
      BodyHandler.create() // Erstelle einen BodyHandler, um den RequestBody der http Anfragen zu verarbeiten
    );

    router.route(HttpMethod.POST, "/login").handler(this::login); // Rufe Callback-Fnktn. login auf, wenn POST /login

    router.route(HttpMethod.POST, "/add").handler(this::add); // rufe add() auf, wenn POST /add
    // auch: router.post("/add").handler(this::add);

    // GET Route /cookies deklarieren, Callback = cookies
    router.route(HttpMethod.GET, "/cookies").handler(this::cookies);

    router.route(HttpMethod.GET, "/inspectCookies").handler(this::inspectCookies);

    server.requestHandler(router).listen(3000); // server lauscht auf PORT 3000 und ruft bei jeder http Anfrage den Router auf
  }
  private void add(RoutingContext routingContext) {
    JsonObject jsonObject = routingContext.getBodyAsJson();
    // System.out.println(jsonObject);

    // get values
    int a = jsonObject.getInteger("a");
    int b = jsonObject.getInteger("b");
    int c = jsonObject.getInteger("c");
    // calculate
    int result = a + b + c;

    // baue http response zusammen
    routingContext.response()
      .putHeader("content-type", "application/json") // gebe json zurück
      .setStatusCode(200) // setze den statuscode
      .end(Json.encodePrettily(new JsonObject().put("sum", result))); // setze die JSON Datei zusammen
  }

  // Callback-Methode für die Route /cookies
  private void cookies(RoutingContext routingContext) {
    // https://vertx.io/docs/vertx-web/java/#_manipulating_cookies (Doku)
    routingContext.response()
      .addCookie(
        Cookie.cookie("firstName", "Noel") // setze Cookie firstName=Noel
      )
      .addCookie(
        Cookie.cookie("lastName", "Haeuser") // setze Cookie lastName=Haeuser
      ).setStatusCode(200).end();
  }

  // Callback-Methode für die Route /inspectCookies
  private void inspectCookies(RoutingContext routingContext) {
    HttpServerRequest request = routingContext.request();
    HttpServerResponse response = routingContext.response();

    // selektiere die cookies
    Cookie firstName = request.getCookie("firstName");
    Cookie lastName = request.getCookie("lastName");
    System.out.println("firstName: " + firstName);
    System.out.println("lastName: " + lastName);

    if (firstName != null && lastName != null) { // wenn beide cookies existieren
      String firstNameAusgabe = "Name: " + firstName.getName() + ", Value: " + firstName.getValue(); // baue firstName und lastName Ausgabe zusammen
      String lastNameAusgabe = "Name: " + lastName.getName() + ", Value: " + lastName.getValue();

      response.setStatusCode(200).end(firstNameAusgabe  + "; " + lastNameAusgabe); // setze den Statuscode auf 200 (ok), wenn die Anfrage auf die Ressource erfolgreich war
    } else {
      response.setStatusCode(404).end(); // setze den Statuscode auf 404 (Not found), wenn die cookies nicht existieren
    }
  }

  private void login(RoutingContext routingContext) {
    @Deprecated
    JsonObject jsonObject = routingContext.getBodyAsJson(); // parse routingContext als JSON
    HttpServerResponse response = routingContext.response();
    Session session = routingContext.session(); //Session initialisieren
    String username = jsonObject.getString("username"); // bekomme den username aus dem Formular
    session.put("username", username); // speichere den username in einem Session Objekt ab

    System.out.println("session: " + session.get("username")); // Test

    response.setStatusCode(200).end(Json.encodePrettily(new JsonObject().put("username", username)));
  }
}
