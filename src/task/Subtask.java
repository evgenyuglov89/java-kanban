package task;

import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, int id, int epicId) {
        super(name, description, id);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int id, int epicId, TaskStatus status) {
        super(name, description, id, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int id, LocalDateTime startTime, int duration, int epicId) {
        super(name, description, id, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (this.getId() != epicId) {
            this.epicId = epicId;
        }
    }

    @Override
    public String toString() {
        return "task.Subtask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status='" + status + '\'' +
                ", epicId=" + epicId +
                '}';
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }
}
