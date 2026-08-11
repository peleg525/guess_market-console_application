package gm.engine;

import gm.engine.dto.EventStatusDto;
import gm.engine.dto.EventSummaryDto;
import gm.engine.dto.PurchaseResultDto;
import gm.engine.exception.GmFileException;
import gm.engine.exception.GmOperationException;

import java.util.List;

/**
 * Everything the UI layer is allowed to know about the Guess Market engine. The UI never talks to
 * concrete engine/model classes directly - only through this interface and the DTOs it returns.
 */
public interface GmEngine {

    /**
     * Loads and validates an XML events file, replacing whatever was loaded before.
     * A failed attempt never touches the previously loaded (valid) data.
     *
     * @return the number of events loaded
     * @throws GmFileException if the path, the XML, or its content is invalid
     */
    int loadEventsFile(String path);

    /** @return true once a valid file has been loaded at least once. */
    boolean isFileLoaded();

    /** All events currently known to the engine, active and closed alike. */
    List<EventSummaryDto> getAllEvents();

    /** Only the events that are still open for trading. */
    List<EventSummaryDto> getActiveEvents();

    /**
     * Full trading status of one event.
     *
     * @throws GmOperationException if no such event exists
     */
    EventStatusDto getEventStatus(int eventId);

    /**
     * Buys shares of one option of an active event.
     *
     * @param optionNumber 1-based index into the event's option list
     * @param quantity     positive number of shares to buy
     * @throws GmOperationException if the event does not exist, is closed, or the arguments are invalid
     */
    PurchaseResultDto buyShares(int eventId, int optionNumber, int quantity);

    /**
     * Closes an active event, settling it in favor of the given winning option.
     *
     * @param winningOptionNumber 1-based index into the event's option list
     * @return the event's final status/summary
     * @throws GmOperationException if the event does not exist, is already closed, or the argument is invalid
     */
    EventStatusDto closeEvent(int eventId, int winningOptionNumber);

    /**
     * Bonus: saves the full current state of the engine (all events, their trading history and
     * account balances included) to a file, so it can be restored later with {@link #loadState}.
     *
     * @param pathWithoutExtension full path including the desired file name, without an extension
     * @throws GmFileException if the path is invalid or the file cannot be written
     */
    void saveState(String pathWithoutExtension);

    /**
     * Bonus: restores a state previously written by {@link #saveState}, replacing whatever is
     * currently loaded. This is separate from {@link #loadEventsFile}, which loads a fresh XML
     * events file rather than a previously saved engine state.
     *
     * @param pathWithoutExtension full path including the file name, without an extension
     * @throws GmFileException if the path is invalid or the saved state cannot be read
     */
    void loadState(String pathWithoutExtension);
}
