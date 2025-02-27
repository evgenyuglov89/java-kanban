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

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status='" + status + '\'' +
                ", epicId=" + epicId +
                '}';
    }
}
