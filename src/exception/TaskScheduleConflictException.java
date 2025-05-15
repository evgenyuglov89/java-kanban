package exception;

public class TaskScheduleConflictException extends RuntimeException {
    public TaskScheduleConflictException(String message) {
        super(message);
    }
}