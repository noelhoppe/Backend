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

    // Allow CORS
    router.route().handler( // wähle alle eingehenden Anfragen aus, die über den Router gehen
      CorsHandler.create() // erstelle einen Corshandler, der die CORS-Konfiguration für die eingehden Anfragen behandelt
        .addOrigin("http://localhost:63342") // lege die Quelle fest, von welcher Anfragen erlaubt sind
        .allowedMethod(HttpMethod.POST) // lege die erlaubte(n) http Methode(n), die von der angegebene Quelle v
        // erwendet werden können
        .allowedMethod(HttpMethod.GET)
        .allowCredentials(true) // Erlaubt Cookies und Authentifizierungsinformationen
    );

    // parse den body der http Anfrage
    router.route().handler( // wähle alle eingehden Anfragen aus, die über den Router gehen
      BodyHandler.create() // Erstelle einen BodyHandler, um den RequestBody der http Anfragen zu verarbeiten
    );

    router.route(HttpMethod.POST, "/add").handler(this::add); // rufe add() auf, wenn POST /add
    // auch: router.post("/add").handler(this::add);

    // GET Route /cookies deklarieren, Callback = cookies
    router.route(HttpMethod.GET, "/cookies").handler(this::cookies);

    server.requestHandler(router).listen(3000); // server lauscht auf PORT 3000 und ruft bei jeder http Anfrage den Router auf
  }
  private void add(RoutingContext routingContext) {
    JsonObject jsonObject = routingContext.getBodyAsJson();

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
    /*
    HttpServerResponse response = routingContext.response();

    // Lege die beiden Cookies an
    response.putHeader("Set-Cookie", "firstName=Noel; Path=/; Max-Age=3600");
    response.putHeader("Set-Cookie", "lastName=Haeuser; Path=/; Max-Age=3600");
    response.setStatusCode(200).end();
    */
    routingContext.response()
      .addCookie(
        Cookie.cookie("firstName", "Noel")
      )
      .addCookie(
        Cookie.cookie("lastName", "Haeuser")
      ).setStatusCode(200).end();
  }
}
