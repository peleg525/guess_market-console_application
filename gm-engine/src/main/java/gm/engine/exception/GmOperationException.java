package gm.engine.exception;

/**
 * Thrown when a requested operation (view an event, buy shares, close an event, ...) cannot be
 * carried out given the engine's current state: unknown event id, invalid option/quantity, event
 * already closed, no file loaded yet, and so on.
 */
public class GmOperationException extends GmException {

    public GmOperationException(String message) {
        super(message);
    }
}
