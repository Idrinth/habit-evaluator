package de.idrinth.habitevaluator.shared.backup;

/**
 * Exception thrown when backup or restore operations fail.
 */
public class BackupException extends Exception {

    public BackupException(String message) {
        super(message);
    }

    public BackupException(String message, Throwable cause) {
        super(message, cause);
    }
}
