package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;

import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SubTaskHandlerTest {

    private final TaskManager taskManager;
    private final HttpTaskServer httpTaskServer;
    private final Gson gson;
    private final LocalDateTime startTime;

    private Epic epic;
    private Subtask subTask;
    private HttpClient client;

    public SubTaskHandlerTest() throws IOException {
        taskManager = Managers.getDefault();
        httpTaskServer = new HttpTaskServer(taskManager);
        gson = new BaseHttpHandler().getGson();
        startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    }

    @BeforeEach
    public void setup() {
        taskManager.deleteAllEpics();

        epic = new Epic("Epic_1", "Description_1", InMemoryTaskManager.getNewId());
        taskManager.createTask(epic);

        subTask = new Subtask("SubTask_1", "Description_1",
                InMemoryTaskManager.getNewId(), startTime, 60, epic.getId());
        client = HttpClient.newHttpClient();
        httpTaskServer.start();
    }

    @AfterEach
    public void tearDown() {
        httpTaskServer.stop();
    }

    private HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080" + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPost(String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendDelete(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    public void getSubTasksTest() throws IOException, InterruptedException {
        taskManager.createTask(subTask);
        Subtask subTask2 = new Subtask("SubTask_2", "Description_2",
                InMemoryTaskManager.getNewId(), startTime.plusHours(2), 60, epic.getId());
        taskManager.createTask(subTask2);

        HttpResponse<String> response = sendGet("/subtasks");

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());

        List<Subtask> subTasksResponse = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {}.getType());

        assertEquals(subTask, subTasksResponse.get(0), "Задачи не совпадают");
        assertEquals(subTask2, subTasksResponse.get(1), "Задачи не совпадают");
    }

    @Test
    public void getSubTaskByIdTest() throws IOException, InterruptedException {
        taskManager.createTask(subTask);
        Subtask subTask2 = new Subtask("SubTask_2", "Description_2",
                InMemoryTaskManager.getNewId(), startTime.plusHours(2), 60, epic.getId());
        taskManager.createTask(subTask2);

        HttpResponse<String> response = sendGet("/subtasks/" + subTask2.getId());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());

        Subtask subTaskResponse = gson.fromJson(response.body(), Subtask.class);

        assertEquals(subTask2, subTaskResponse, "Задачи не совпадают");
    }

    @Test
    public void getSubTaskByWrongIdTest() throws IOException, InterruptedException {
        taskManager.createTask(subTask);
        taskManager.createTask(new Subtask("SubTask_2", "Description_2",
                InMemoryTaskManager.getNewId(), startTime.plusHours(2), 60, epic.getId()));

        HttpResponse<String> response = sendGet("/subtasks/9999");

        assertEquals(HttpCodeResponse.NOT_FOUND.getCode(), response.statusCode());
    }

    @Test
    public void createSubTask() throws IOException, InterruptedException {
        String subTaskJson = gson.toJson(subTask);

        HttpResponse<String> response = sendPost("/subtasks", subTaskJson);

        assertEquals(HttpCodeResponse.MODIFIED.getCode(), response.statusCode(), "Ошибка сервера");

        List<Task> tasksFromManager = taskManager.getAllSubTasks();
        assertNotNull(tasksFromManager, "Задача не создаётся");
    }

    @Test
    public void updateSingleTask() throws IOException, InterruptedException {
        taskManager.createTask(subTask);

        Subtask subTaskChanged = new Subtask("SubTask_1", "Description_1",
                subTask.getId(), startTime, 60, epic.getId());
        subTaskChanged.setStatus(TaskStatus.DONE);

        String json = gson.toJson(subTaskChanged);

        HttpResponse<String> response = sendPost("/subtasks", json);

        assertEquals(HttpCodeResponse.MODIFIED.getCode(), response.statusCode(), "Ошибка сервера");
        List<Task> tasksFromManager = taskManager.getAllSubTasks();
        assertEquals(1, tasksFromManager.size(), "Задача создалась, а не обновилась");
        assertEquals(TaskStatus.DONE, tasksFromManager.get(0).getStatus(), "Ошибка обновления задачи");
    }

    @Test
    public void createOverlapTask() throws IOException, InterruptedException {
        taskManager.createTask(subTask);

        Subtask overlap = new Subtask("SubTask_3", "Description_3",
                InMemoryTaskManager.getNewId(), startTime.plusMinutes(30), 60, epic.getId());
        String json = gson.toJson(overlap);

        HttpResponse<String> response = sendPost("/subtasks", json);

        assertEquals(HttpCodeResponse.OVERLAP.getCode(), response.statusCode(), "Ошибка сервера");
        assertEquals(1, taskManager.getAllSubTasks().size(), "Некорректное количество задач");
    }

    @Test
    public void removeTaskById() throws IOException, InterruptedException {
        taskManager.createTask(subTask);
        Subtask subTask2 = new Subtask("SubTask_2", "Description_2",
                InMemoryTaskManager.getNewId(), startTime.plusHours(2), 60, epic.getId());
        taskManager.createTask(subTask2);

        HttpResponse<String> response = sendDelete("/subtasks/" + subTask2.getId());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        assertEquals(1, taskManager.getAllSubTasks().size(), "Задача не удаляется");
    }

    @Test
    public void removeAllSingleTasks() throws IOException, InterruptedException {
        taskManager.createTask(subTask);
        taskManager.createTask(new Subtask("SubTask_2", "Description_2",
                InMemoryTaskManager.getNewId(), startTime.plusHours(2), 60, epic.getId()));

        HttpResponse<String> response = sendDelete("/subtasks");

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        assertEquals(0, taskManager.getAllSubTasks().size(), "Задачи не удаляются");
    }
}