package org.jks;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


/**
 * Created by Administrator on 2017/5/27.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String args[]){
        Vertx vertx = Vertx.vertx();
        logger.info("start");
        vertx.deployVerticle(new ServerVerticle());
        logger.info("end");
    }

}
