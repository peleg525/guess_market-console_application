package gm.engine.exception;

/**
 * Base class for every runtime error the engine can raise. Unchecked on purpose: the UI layer
 * catches these at the top of its command loop and prints a friendly message, without forcing
 * every engine call site to declare a throws clause.
 */
public abstract class GmException extends RuntimeException {

    protected GmException(String message) {
        super(message);
    }
}
