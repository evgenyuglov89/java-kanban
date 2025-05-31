package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import task.Task;
import task.TaskStatus;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TaskHandlerTest {
    private final TaskManager taskManager;
    private final HttpTaskServer httpTaskServer;
    private final Gson gson;
    private final LocalDateTime startTime;
    private final HttpClient client;

    private Task task;

    public TaskHandlerTest() throws IOException {
        taskManager = Managers.getDefault();
        httpTaskServer = new HttpTaskServer(taskManager);
        gson = new BaseHttpHandler().getGson();
        startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        client = HttpClient.newHttpClient();
    }

    @BeforeEach
    public void setUp() {
        taskManager.deleteAllTasks();
        task = new Task("Task_1", "Description_1",
                InMemoryTaskManager.getNewId(), startTime, 60);
        httpTaskServer.start();
    }

    @AfterEach
    public void tearDown() {
        httpTaskServer.stop();
    }

    private HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080" + path);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPost(String path, String body) throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080" + path);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendDelete(String path) throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080" + path);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    public void shouldReturnAllSingleTasks() throws IOException, InterruptedException {
        taskManager.createTask(task);
        Task secondTask = new Task("Task_2", "Description_2",
                InMemoryTaskManager.getNewId(), startTime.plusHours(2), 60);
        taskManager.createTask(secondTask);

        HttpResponse<String> response = sendGet("/tasks");

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());

        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {}.getType());

        assertEquals(task, tasks.get(0));
        assertEquals(secondTask, tasks.get(1));
    }

    @Test
    public void shouldReturnTaskById() throws IOException, InterruptedException {
        taskManager.createTask(task);
        Task expected = new Task("Task_2", "Description_2",
                InMemoryTaskManager.getNewId(), startTime.plusHours(2), 60);
        expected.setStatus(TaskStatus.DONE);
        taskManager.createTask(expected);

        HttpResponse<String> response = sendGet("/tasks/" + expected.getId());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        Task actual = gson.fromJson(response.body(), Task.class);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldReturnNotFoundForWrongId() throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet("/tasks/9999");
        assertEquals(HttpCodeResponse.NOT_FOUND.getCode(), response.statusCode());
    }

    @Test
    public void shouldCreateNewTask() throws IOException, InterruptedException {
        String json = gson.toJson(task);

        HttpResponse<String> response = sendPost("/tasks", json);

        assertEquals(HttpCodeResponse.MODIFIED.getCode(), response.statusCode());
        assertEquals(1, taskManager.getAllTasks().size());
    }

    @Test
    public void shouldUpdateExistingTask() throws IOException, InterruptedException {
        taskManager.createTask(task);
        Task updated = new Task("Task_1", "Description_1",
                task.getId(), startTime, 60);
        updated.setStatus(TaskStatus.DONE);

        String json = gson.toJson(updated);
        HttpResponse<String> response = sendPost("/tasks", json);

        assertEquals(HttpCodeResponse.MODIFIED.getCode(), response.statusCode());
        assertEquals(TaskStatus.DONE, taskManager.getAllTasks().get(0).getStatus());
    }

    @Test
    public void shouldNotCreateOverlappingTask() throws IOException, InterruptedException {
        taskManager.createTask(task);

        Task overlap = new Task("Task_3", "Description_3",
                InMemoryTaskManager.getNewId(), startTime.plusMinutes(30), 60);
        String json = gson.toJson(overlap);
        HttpResponse<String> response = sendPost("/tasks", json);

        assertEquals(HttpCodeResponse.OVERLAP.getCode(), response.statusCode());
        assertEquals(1, taskManager.getAllTasks().size());
    }

    @Test
    public void shouldRemoveTaskById() throws IOException, InterruptedException {
        taskManager.createTask(task);
        Task toRemove = new Task("Task_1", "Description_1",
                InMemoryTaskManager.getNewId(), startTime.plusMinutes(120), 60);
        taskManager.createTask(toRemove);

        HttpResponse<String> response = sendDelete("/tasks/" + toRemove.getId());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        assertEquals(1, taskManager.getAllTasks().size());
    }

    @Test
    public void shouldRemoveAllTasks() throws IOException, InterruptedException {
        taskManager.createTask(task);
        taskManager.createTask(new Task("Task_2", "Description_2",
                InMemoryTaskManager.getNewId(), startTime.plusHours(1), 30));

        HttpResponse<String> response = sendDelete("/tasks");

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        assertTrue(taskManager.getAllTasks().isEmpty());
    }
}

