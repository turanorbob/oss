package org.jks;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.templ.*;
import org.apache.commons.collections.CollectionUtils;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2017/6/1.
 */
public class ServerVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(ServerVerticle.class);
    public static final int PORT = 8080;

    public void route(Router router, Vertx vertx){
        /*router.route().handler(routingContext -> {

            // This handler will be called for every request
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/plain");

            // Write to the response and end it
            response.end("Hello World from Vert.x-Web!");
            logger.info("Hello World from Vert.x-Web!");
        });*/

        router.route("/some/path/").handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            // enable chunked responses because we will be adding data as
            // we execute over other handlers. This is only required once and
            // only if several handlers do output.
            response.setChunked(true);

            response.write("route1\n");

            logger.info("router1");
            // Call the next matching route after a 5 second delay
            routingContext.vertx().setTimer(1000, tid -> routingContext.next());

        });

        router.route("/some/path/").handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            response.write("route2\n");

            logger.info("router2");
            // Call the next matching route after a 5 second delay
            routingContext.vertx().setTimer(1000, tid -> routingContext.next());
        });

        router.route("/some/path/").handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            response.write("route3");
            logger.info("router3");
            // Now end the response
            routingContext.response().end();
        });

        Route route = router.route(HttpMethod.POST, "/catalogue/products/:producttype/:productid/");

        route.handler(routingContext -> {

            String productType = routingContext.request().getParam("producttype");
            String productID = routingContext.request().getParam("productid");

            logger.info("productType:" + productType);
            logger.info("productID:" + productID);
            routingContext.response().end("ok");
        });

        Route regexRoute = router.route().pathRegex(".*foo");

        regexRoute.handler(routingContext -> {
            routingContext.response().end("regex route");
        });

        Route routeRegex = router.routeWithRegex(".*foo");

        // This regular expression matches paths that start with something like:
        // "/foo/bar" - where the "foo" is captured into param0 and the "bar" is captured into
        // param1
        routeRegex.pathRegex("\\/([^\\/]+)\\/([^\\/]+)").handler(routingContext -> {

            String productType = routingContext.request().getParam("param0");
            String productID = routingContext.request().getParam("param1");

            logger.info("productType:" + productType);
            logger.info("productID:" + productID);
            routingContext.next();
        });
        routeRegex.enable();

        router.get("/re/path").handler(routingContext -> {

            routingContext.put("foo", "bar");
            logger.info("re/path put");
            routingContext.next();
        });

        router.get("/re/path/B").handler(routingContext -> {
            logger.info("re/path/B");
            routingContext.response().end("/re/path/B");
        });

        router.get("/re/path").handler(routingContext -> {
            logger.info("re/path -> /re/path/B");
            routingContext.reroute("/re/path/B");
        });

        router.get("/my-pretty-notfound-handler").handler(ctx -> {
            ctx.response()
                    .setStatusCode(404)
                    .end("NOT FOUND fancy html here!!!");
        });

        router.get().failureHandler(ctx -> {
            if (ctx.statusCode() == 404) {
                ctx.reroute("/my-pretty-notfound-handler");
            } else {
                logger.info("not found");
                ctx.next();
            }
        });

        Router restAPI = Router.router(vertx);

        restAPI.get("/products/:productID").handler(rc -> {
            rc.response().setChunked(true);
            rc.response().write("sss").end();
        });

        restAPI.put("/products/:productID").handler(rc -> {
            rc.response().end();
        });

        restAPI.delete("/products/:productID").handler(rc -> {
            rc.response().end();
        });

        router.mountSubRouter("/productsAPI", restAPI);

        router.get("/localized").handler(rc -> {
            // although it might seem strange by running a loop with a switch we
            // make sure that the locale order of preference is preserved when
            // replying in the users language.
            for (LanguageHeader language : rc.acceptableLanguages()) {
                switch (language.tag()) {
                    case "en":
                        rc.response().end("Hello!");
                        return;
                    case "fr":
                        rc.response().end("Bonjour!");
                        return;
                    case "pt":
                        rc.response().end("Olá!");
                        return;
                    case "es":
                        rc.response().end("Hola!");
                        return;
                }
            }
            // we do not know the user language so lets just inform that back:
            rc.response().end("Sorry we don't speak: " + rc.preferredLocale());
        });

        Route route1 = router.get("/somepath/path1/");

        route1.handler(routingContext -> {

            // Let's say this throws a RuntimeException
            throw new RuntimeException("something happened!");

        });

        Route route2 = router.get("/somepath/path2");

        route2.handler(routingContext -> {

            // This one deliberately fails the request passing in the status code
            // E.g. 403 - Forbidden
            routingContext.fail(403);

        });

        // Define a failure handler
        // This will get called for any failures in the above handlers
        Route route3 = router.get("/somepath/*");

        route3.failureHandler(failureRoutingContext -> {

            int statusCode = failureRoutingContext.statusCode();
            logger.info("status code:" + statusCode);
            // Status code will be 500 for the RuntimeException or 403 for the other failure
            HttpServerResponse response = failureRoutingContext.response();
            response.setStatusCode(statusCode < 0 ? 500 : statusCode).end("Sorry! Not today");

        });

        FreeMarkerTemplateEngine engine = FreeMarkerTemplateEngine.create();
        router.get("/freemarker").handler(ctx -> {
            // we define a hardcoded title for our application
            ctx.put("name", "Vert.x Web");

            // and now delegate to the engine to render it.
            engine.render(ctx, "templates/index.ftl", res -> {
                if (res.succeeded()) {
                    ctx.response().end(res.result());
                } else {
                    ctx.fail(res.cause());
                }
            });
        });

        HandlebarsTemplateEngine handlebarsTemplateEngine = HandlebarsTemplateEngine.create();
        router.get("/handlebars").handler(ctx -> {
            // we define a hardcoded title for our application
            ctx.put("title", "Seasons of the year");
            // we define a hardcoded array of json objects
            JsonArray seasons = new JsonArray();
            seasons.add(new JsonObject().put("name", "Spring"));
            seasons.add(new JsonObject().put("name", "Summer"));
            seasons.add(new JsonObject().put("name", "Autumn"));
            seasons.add(new JsonObject().put("name", "Winter"));

            ctx.put("seasons", seasons);

            // and now delegate to the engine to render it.
            handlebarsTemplateEngine.render(ctx, "templates/index.hbs", res -> {
                if (res.succeeded()) {
                    ctx.response().end(res.result());
                } else {
                    ctx.fail(res.cause());
                }
            });
        });

        // In order to use a template we first need to create an engine
        final JadeTemplateEngine jadeTemplateEngine = JadeTemplateEngine.create();

        // Entry point to the application, this will render a custom template.
        router.get("/jade").handler(ctx -> {
            // we define a hardcoded title for our application
            ctx.put("name", "Vert.x Web");

            // and now delegate to the engine to render it.
            jadeTemplateEngine.render(ctx, "templates/index.jade", res -> {
                if (res.succeeded()) {
                    ctx.response().end(res.result());
                } else {
                    ctx.fail(res.cause());
                }
            });
        });

        MVELTemplateEngine mvelTemplateEngine = MVELTemplateEngine.create();
        router.get("/mvel/").handler(ctx -> {
            // we define a hardcoded title for our application
            ctx.put("name", "Vert.x Web");

            // and now delegate to the engine to render it.
            mvelTemplateEngine.render(ctx, "templates/index.templ", res -> {
                if (res.succeeded()) {
                    ctx.response().end(res.result());
                } else {
                    ctx.fail(res.cause());
                }
            });
        });

        PebbleTemplateEngine pebbleTemplateEngine = PebbleTemplateEngine.create(vertx);
        router.get("/peb").handler(ctx -> {
            // we define a hardcoded title for our application
            ctx.put("name", "Vert.x Web");

            // and now delegate to the engine to render it.
            pebbleTemplateEngine.render(ctx, "templates/index.peb", res -> {
                if (res.succeeded()) {
                    ctx.response().end(res.result());
                } else {
                    ctx.fail(res.cause());
                }
            });
        });

        ThymeleafTemplateEngine thymeleafTemplateEngine = ThymeleafTemplateEngine.create();
        router.get("/thymeleaf").handler(ctx -> {
            // we define a hardcoded title for our application
            ctx.put("welcome", "Vert.x Web");

            // and now delegate to the engine to render it.
            thymeleafTemplateEngine.render(ctx, "templates/thymeleaf.html", res -> {
                if (res.succeeded()) {
                    ctx.response().end(res.result());
                } else {
                    ctx.fail(res.cause());
                }
            });
        });

        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

        router.get("/session").handler(ctx -> {
            Session session = ctx.session();
            Integer cnt = session.get("hitcount");
            if(cnt == null){
                cnt = 1;
            }
            else{
                cnt += 1;
            }
            session.put("hitcount", cnt);

            ctx.response().end("hitcount:"+cnt);
        });

        // Allow events for the designated addresses in/out of the event bus bridge
        BridgeOptions opts = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddress("feed"));
        // Create the event bus bridge and add it to the router.
        SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
        router.route("/eventbus/*").handler(ebHandler);
        // Create a router endpoint for the static content.
        router.route().handler(StaticHandler.create());
        EventBus eb = vertx.eventBus();

        vertx.setPeriodic(1000l, t -> {
            // Create a timestamp string
            String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date.from(Instant.now()));

            eb.send("feed", new JsonObject().put("now", timestamp));
        });

        router.post("/json").handler(ctx ->{
            JsonObject jsonObject = ctx.getBodyAsJson();
            logger.info(jsonObject);
            ctx.response().end("json");
        });

        router.route().handler(BodyHandler.create());
    }

    public Payment createPayment(){
        // Add payer details
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setReturnUrl("http://localhost:"+PORT+"/process");
        redirectUrls.setCancelUrl("http://localhost:"+PORT+"/cancel");

        Details details = new Details();
        details.setShipping("1");
        details.setSubtotal("5");
        details.setTax("1");

        Amount amount = new Amount();
        amount.setCurrency("USD");

        amount.setTotal("7");
        amount.setDetails(details);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("This is the payment description.");

        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        Payment payment = new Payment();
        payment.setIntent("order");
        payment.setPayer(payer);
        payment.setRedirectUrls(redirectUrls);
        payment.setTransactions(transactions);
        return payment;
    }

    @Override
    public void start() throws Exception {
        super.start();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        String clientId = "AeRYgXFqNR7HrzI8i2gGMYEKKllvxargErtSiU5ppBxgJ6_mxSI5oLEnkpuSgVblfazEOI8vwx2wxd_f";
        String clientSecret = "EJirEaA8cmobgR5zpOm-sPfNIMOUk1BwQI9WW7b6NkEhHF2KqtxcWtDca4C-ZbrASSOESTy8SMDopvaP";
        String mode = "sandbox";
        //route(router, vertx);
        router.get("/pay").handler(ctx -> {
            Payment payment = createPayment();

            APIContext apiContext = new APIContext(clientId, clientSecret, mode);
            try{
                Payment createdPayment = payment.create(apiContext);

                List<Links> links = createdPayment.getLinks();
                if(!CollectionUtils.isEmpty(links)){
                    for (Links link : links) {
                        if (link.getRel().equalsIgnoreCase("approval_url")) {
                            //REDIRECT USER TO link.getHref()
                            logger.info(link.getHref());
                        }
                    }
                }
                ctx.response().end(createdPayment.toJSON());
            }
            catch (PayPalRESTException e) {
                System.err.println(e.getDetails());
            }

        });

        router.get("/process").handler(ctx ->{
            // Get payment id from query string following redirect
            String paymentId = ctx.request().getParam("paymentId");
            String playerId = ctx.request().getParam("PayerID");
            Payment payment = new Payment();
            payment.setId(paymentId);
            APIContext apiContext = new APIContext(clientId, clientSecret, mode);
            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(playerId);
            try {
                Payment createdPayment = payment.execute(apiContext, paymentExecution);

                if ("approved".equals(createdPayment.getState())){
                    // Get order id
                    String orderId = createdPayment.getTransactions().get(0)
                            .getRelatedResources().get(0).getOrder().getId();
                    ctx.response().end(orderId);

                    ctx.reroute("/finish?orderId="+orderId);
                }
            } catch (PayPalRESTException e) {
                System.err.println(e.getDetails());
            }
        });

        router.get("/finish").handler(ctx -> {
            String orderId = ctx.request().getParam("orderId");
            APIContext apiContext = new APIContext(clientId, clientSecret, mode);
            try {
                // Set auth amount
                Amount amount = new Amount();
                amount.setCurrency("USD");
                amount.setTotal("4.54");

                // Authorize order
                Order order = new Order();
                order = Order.get(apiContext, orderId);
                order.setAmount(amount);
                Authorization authorization = order.authorize(apiContext);

                // Capture payment
                Capture capture = new Capture();
                capture.setAmount(amount);
                capture.setIsFinalCapture(true);

                Capture responseCapture = authorization.capture(apiContext, capture);

                logger.info("Capture id = " + responseCapture.getId()
                        + " and status = " + responseCapture.getState());
            } catch (PayPalRESTException e) {
                System.err.println(e.getDetails());
                ctx.response().end(e.getDetails().getMessage());
            }
        });

        router.get("/cancel").handler(ctx -> {
            ctx.response().end("cancel");
        });

        server.requestHandler(router::accept).listen(PORT);
        logger.info("listen " + PORT);
    }
}
