package server;

import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;
import manager.InMemoryTaskManager;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import manager.TaskManager;
import task.Task;
import task.TaskStatus;

public class HistoryHandlerTest {

    private TaskManager taskManager;
    private HttpTaskServer taskServer;
    private Gson gson;

    private static final String HISTORY_URL = "http://localhost:8080/history";

    @BeforeEach
    public void setup() throws IOException {
        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        gson = new BaseHttpHandler().getGson();

        taskManager.deleteAll();
        taskServer.start();
    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }

    @Test
    public void shouldReturnHistoryWithSingleAccessedTask() throws IOException, InterruptedException {
        Task task1 = new Task("Task_1", "Description_1", InMemoryTaskManager.getNewId());
        Task task2 = new Task("Task_2", "Description_2", InMemoryTaskManager.getNewId());

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        Task accessedTask = taskManager.getById(task1.getId());
        accessedTask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(accessedTask);

        HttpResponse<String> response = sendGet(HISTORY_URL);

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());

        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>() {}.getType());

        assertEquals(1, history.size(), "История должна содержать только одну задачу");
        assertEquals(task1, history.get(0), "История содержит неверную задачу");
    }

    @Test
    public void shouldReturn405ForNotAllowedMethod() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HISTORY_URL))
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.NOT_ALLOWED.getCode(), response.statusCode(), "Ожидался код 405");
    }

    private HttpResponse<String> sendGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }
}
