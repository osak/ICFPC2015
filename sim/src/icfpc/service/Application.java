package icfpc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import icfpc.common.Command;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * @author tomoyuki
 */


public class Application implements HttpHandler {
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        try {
            final State state = OBJECT_MAPPER.readValue(httpExchange.getRequestBody(), State.class);
            //state.board.debug();
            final boolean result = state.board.operate(Command.valueOf(state.command));
            final Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.add("content-type", "application/json");

            final String content = OBJECT_MAPPER.writeValueAsString(ImmutableMap.of("Result", result, "Board", state.board));
            httpExchange.sendResponseHeaders(200, content.length());

            final OutputStream responseBody = httpExchange.getResponseBody();
            responseBody.write(content.getBytes());
            responseBody.close();
        } catch(Exception e) {
            throw e;
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(4685), 0);
        server.createContext("/miichan", new Application());
        server.setExecutor(null);
        server.start();
    }
}
