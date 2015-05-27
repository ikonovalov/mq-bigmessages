package ru.codeunited.wmq.mqi;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 27.05.15.
 */
public class NoMessageAvailableException extends Exception {

    public NoMessageAvailableException(String message) {
        super(message);
    }

    public NoMessageAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMessageAvailableException(Throwable cause) {
        super(cause);
    }
}
