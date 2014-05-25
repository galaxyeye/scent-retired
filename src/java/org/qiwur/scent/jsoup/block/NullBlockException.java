package org.qiwur.scent.jsoup.block;

public class NullBlockException extends Exception {
    private static final long serialVersionUID = 1837372828374251L;

    public NullBlockException() {
        super();
    }

    public NullBlockException(String message, Throwable cause) {
        super(message, cause);
    }

    public NullBlockException(String message) {
        super(message);
    }

    public NullBlockException(Throwable cause) {
        super(cause);
    }
}
