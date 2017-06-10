package org.jks.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Administrator on 2017/6/10.
 */
public class Recv {
    public static final String QUEUE_NAME = "hello";

    public static void main(String args[]) throws IOException, TimeoutException {
        boolean autoAsk = false;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.10.118");
        factory.setUsername("user");
        factory.setPassword("password");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.basicConsume(QUEUE_NAME, autoAsk, "myConsumeTag", new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String routingKey = envelope.getRoutingKey();
                String contentType = properties.getContentType();
                long deviveryTag = envelope.getDeliveryTag();
                String message = new String(body);
                System.out.println("routingKey:"+routingKey+",contentType:"+contentType +",message:"+message);
                channel.basicAck(deviveryTag, false);
            }
        });

    }
}
