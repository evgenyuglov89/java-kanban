package server;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import com.google.gson.Gson;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import task.Task;

public class PrioritizedHandlerTest {

    private TaskManager taskManager;
    private HttpTaskServer taskServer;
    private Gson gson;

    private final LocalDateTime startTime = LocalDateTime.now();
    private static final String PRIORITIZED_URL = "http://localhost:8080/prioritized";

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
    public void shouldReturnOnlyTasksWithTimeSortedByStartTime() throws IOException, InterruptedException {
        Task taskNoTime = new Task("Task_1", "Description_1", InMemoryTaskManager.getNewId());
        taskManager.createTask(taskNoTime);

        Task taskFuture = new Task("Task_Future", "Task in +1h",
                InMemoryTaskManager.getNewId(), startTime.plusHours(1), 30);
        taskManager.createTask(taskFuture);

        Task taskNow = new Task("Task_Now", "Task now",
                InMemoryTaskManager.getNewId(),  startTime, 30);
        taskManager.createTask(taskNow);

        Task taskPast = new Task("Task_Past", "Task in -1h",
                InMemoryTaskManager.getNewId(), startTime.minusHours(1), 30);
        taskManager.createTask(taskPast);

        HttpResponse<String> response = sendGet(PRIORITIZED_URL);

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {}.getType());

        assertFalse(prioritizedTasks.contains(taskNoTime),
                "Задача без времени не должна быть в приоритезированном списке");

        assertEquals(taskPast, prioritizedTasks.get(0), "Ожидалась задача с наименьшим временем старта");
        assertEquals(taskNow, prioritizedTasks.get(1), "Ожидалась текущая задача второй");
        assertEquals(taskFuture, prioritizedTasks.get(2), "Ожидалась задача из будущего третьей");
    }

    @Test
    public void shouldReturn405ForNotAllowedMethod() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PRIORITIZED_URL))
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.NOT_ALLOWED.getCode(), response.statusCode(),
                "Ожидался код 405 для DELETE запроса");
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

