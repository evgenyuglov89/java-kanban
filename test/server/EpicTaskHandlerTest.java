package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

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
import java.util.ArrayList;
import java.util.List;

public class EpicTaskHandlerTest {
    private final TaskManager taskManager;
    private final HttpTaskServer httpTaskServer;
    private final Gson gson;
    private final LocalDateTime startTime;
    private Epic epic;
    private Subtask subTask1;
    private Subtask subTask2;

    private HttpClient client;

    public EpicTaskHandlerTest() throws IOException {
        this.taskManager = Managers.getDefault();
        this.httpTaskServer = new HttpTaskServer(taskManager);
        this.gson = new BaseHttpHandler().getGson();
        this.startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        this.client = HttpClient.newHttpClient();
    }

    @BeforeEach
    public void setUp() {
        taskManager.getAllEpics();

        epic = new Epic("Epic_1", "Description_1", InMemoryTaskManager.getNewId());
        subTask1 = new Subtask("SubTask1", "Subtask 1",
                InMemoryTaskManager.getNewId(), startTime, 60, epic.getId());

        subTask2 = new Subtask("SubTask2", "Subtask 2",
                InMemoryTaskManager.getNewId(), startTime.plusHours(2), 60, epic.getId());

        epic.setSubTasks(List.of(subTask1, subTask2));

        httpTaskServer.start();
    }

    @AfterEach
    public void tearDown() {
        httpTaskServer.stop();
    }

    @Test
    public void shouldReturnAllEpicTasks() throws IOException, InterruptedException {
        taskManager.createTask(epic);

        Epic epic2 = new Epic("Epic_2", "Description_2", InMemoryTaskManager.getNewId());
        epic2.setSubTasks(List.of(subTask1, subTask2));
        taskManager.createTask(epic2);

        HttpResponse<String> response = sendGet("/epics");

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());

        List<Epic> epics = gson.fromJson(response.body(), new TypeToken<List<Epic>>(){}.getType());

        assertEquals(epic, epics.get(0), "Задачи не совпадают");
        assertEquals(epic2, epics.get(1), "Задачи не совпадают");
    }

    @Test
    public void shouldReturnEpicById() throws IOException, InterruptedException {
        taskManager.createTask(epic);
        Epic epic2 = new Epic("Epic_2", "Description_2", InMemoryTaskManager.getNewId());
        epic2.setSubTasks(List.of(subTask1, subTask2));
        taskManager.createTask(epic2);

        HttpResponse<String> response = sendGet("/epics/" + epic2.getId());

        Epic receivedEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic2, receivedEpic, "Задачи не совпадают");
    }

    @Test
    public void shouldReturnNotFoundForWrongEpicId() throws IOException, InterruptedException {
        taskManager.createTask(epic);
        new Epic("Epic_2", "Description_2", InMemoryTaskManager.getNewId());

        HttpResponse<String> response = sendGet("/epics/9999");
        assertEquals(HttpCodeResponse.NOT_FOUND.getCode(), response.statusCode());
    }

    @Test
    public void shouldReturnSubtasksOfEpic() throws IOException, InterruptedException {
        taskManager.createTask(epic);
        Epic epic2 = new Epic("Epic_2", "Description_2", InMemoryTaskManager.getNewId());
        epic2.setSubTasks(List.of(subTask1, subTask2));
        taskManager.createTask(epic2);

        HttpResponse<String> response = sendGet("/epics/" + epic2.getId() + "/subtasks");

        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {}.getType());

        assertEquals(2, subtasks.size(), "Неверное количество подзадач");
    }

    @Test
    public void shouldCreateEpic() throws IOException, InterruptedException {
        String json = gson.toJson(epic);
        HttpResponse<String> response = sendPost("/epics", json);

        assertEquals(HttpCodeResponse.MODIFIED.getCode(), response.statusCode(), "Ошибка сервера");

        List<Task> epics = taskManager.getAllEpics();
        assertFalse(epics.isEmpty(), "Эпик не создаётся");
    }

    @Test
    public void shouldUpdateEpic() throws IOException, InterruptedException {
        taskManager.createTask(epic);

        Epic updatedEpic = new Epic("Epic_2", "Description_2", epic.getId());
        updatedEpic.setSubTasks(new ArrayList<>(List.of(subTask1, subTask2)));
        subTask1.setStatus(TaskStatus.DONE);

        updatedEpic.modifySubTask(subTask1);

        HttpResponse<String> response = sendPost("/epics", gson.toJson(updatedEpic));

        assertEquals(HttpCodeResponse.MODIFIED.getCode(), response.statusCode(), "Ошибка сервера");

        List<Task> epics = taskManager.getAllEpics();
        assertEquals(1, epics.size(), "Эпик создался заново");
        assertEquals("Epic_2", epics.get(0).getName(), "Ошибка обновления");
    }

    @Test
    public void shouldRemoveEpicById() throws IOException, InterruptedException {
        taskManager.createTask(epic);
        Epic epic2 = new Epic("Epic_2", "Description_2", InMemoryTaskManager.getNewId());
        epic2.setSubTasks(List.of(subTask1, subTask2));

        HttpResponse<String> response = sendDelete("/epics/" + epic2.getId());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        assertEquals(1, taskManager.getAllEpics().size(), "Эпик не удалён");
    }

    @Test
    public void shouldRemoveAllEpics() throws IOException, InterruptedException {
        taskManager.createTask(epic);
        new Epic("Epic_2", "Description_2", InMemoryTaskManager.getNewId());

        HttpResponse<String> response = sendDelete("/epics");

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        assertEquals(0, taskManager.getAllEpics().size(), "Эпики не удалены");
    }

    private HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPost(String path, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .POST(HttpRequest.BodyPublishers.ofString(json))
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
}

