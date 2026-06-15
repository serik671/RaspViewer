package ru.sbmpei.serik.raspviewer.controller;

import io.javalin.http.sse.SseClient;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author SLakeev
 */
public class ClientController {

    private static final Logger LOGGER = LogManager.getLogger();

    private final List<SseClient> clients = new CopyOnWriteArrayList<>();

    public void addClient(SseClient client) {
        client.keepAlive();
        client.onClose(() -> {
            clients.remove(client);
            LOGGER.info("Remove client {}", client);
        });
        clients.add(client);
        LOGGER.info("Add client {}", client);
    }

    public void sendMessage(Object msg, String event) {
        clients.forEach(client -> client.sendEvent(event, msg));
    }
}
