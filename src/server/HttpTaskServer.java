package server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

public class HttpTaskServer {

    private static final int PORT = 8080;

    private final HttpServer server;
    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        registerContexts();
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private void registerContexts() {
        Map<String, HttpHandler> contexts = Map.of(
                "/tasks",       new TaskHandler(taskManager),
                "/epics",       new EpicHandler(taskManager),
                "/subtasks",    new SubTaskHandler(taskManager),
                "/history",     new HistoryHandler(taskManager),
                "/prioritized", new PrioritizedHandler(taskManager)
        );

        contexts.forEach(this::createContext);
    }

    private void createContext(String path, HttpHandler handler) {
        server.createContext(path, handler);
    }
}
