package net.sentientturtle.html;

/// Exception type for errors during HTML rendering
/// <br>
/// Used to provide more detailed (pseudo-)stacktrace, as the delayed rendering obscures element origin in the normal stacktrace
public class RenderingException extends Exception {
    public RenderingException() {
    }

    public RenderingException(String message) {
        super(message);
    }

    public RenderingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RenderingException(Throwable cause) {
        super(cause);
    }

    public RenderingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
