package gm.engine;

import gm.engine.dto.EventStatusDto;
import gm.engine.dto.EventSummaryDto;
import gm.engine.dto.OptionStatusDto;
import gm.engine.dto.PurchaseResultDto;
import gm.engine.dto.TradeDto;
import gm.engine.exception.GmFileException;
import gm.engine.exception.GmOperationException;
import gm.engine.model.Event;
import gm.engine.model.PurchaseOutcome;
import gm.engine.model.Trade;
import gm.engine.xml.GmFileLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GmEngineImpl implements GmEngine {

    private static final String SAVE_FILE_EXTENSION = ".gmstate";

    private final GmFileLoader fileLoader = new GmFileLoader();
    private List<Event> events = new ArrayList<>();
    private boolean fileLoaded = false;

    @Override
    public int loadEventsFile(String path) {
        List<Event> loaded = fileLoader.load(path);
        this.events = loaded;
        this.fileLoaded = true;
        return events.size();
    }

    @Override
    public boolean isFileLoaded() {
        return fileLoaded;
    }

    @Override
    public List<EventSummaryDto> getAllEvents() {
        return events.stream().map(this::toSummaryDto).collect(Collectors.toList());
    }

    @Override
    public List<EventSummaryDto> getActiveEvents() {
        return events.stream().filter(Event::isActive).map(this::toSummaryDto).collect(Collectors.toList());
    }

    @Override
    public EventStatusDto getEventStatus(int eventId) {
        return toStatusDto(findEvent(eventId));
    }

    @Override
    public PurchaseResultDto buyShares(int eventId, int optionNumber, int quantity) {
        Event event = findEvent(eventId);
        PurchaseOutcome outcome = event.buy(optionNumber - 1, quantity);
        return new PurchaseResultDto(outcome.getSharesCost(), outcome.getCommissionPaid(),
                outcome.getTotalPaid(), toStatusDto(event));
    }

    @Override
    public EventStatusDto closeEvent(int eventId, int winningOptionNumber) {
        Event event = findEvent(eventId);
        event.close(winningOptionNumber - 1);
        return toStatusDto(event);
    }

    @Override
    public void saveState(String pathWithoutExtension) {
        File file = resolveStateFile(pathWithoutExtension);
        File parentDir = file.getAbsoluteFile().getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            throw new GmFileException("The folder \"" + parentDir + "\" does not exist.");
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(new ArrayList<>(events));
            out.writeBoolean(fileLoaded);
        } catch (IOException e) {
            throw new GmFileException("Could not save the application state: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadState(String pathWithoutExtension) {
        File file = resolveStateFile(pathWithoutExtension);
        if (!file.exists() || !file.isFile()) {
            throw new GmFileException("No saved state was found at path: \"" + file.getPath() + "\"");
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            this.events = (List<Event>) in.readObject();
            this.fileLoaded = in.readBoolean();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new GmFileException("Could not load the saved state: " + e.getMessage());
        }
    }

    private File resolveStateFile(String pathWithoutExtension) {
        if (pathWithoutExtension == null || pathWithoutExtension.isBlank()) {
            throw new GmFileException("No file path was entered.");
        }
        return new File(pathWithoutExtension.trim() + SAVE_FILE_EXTENSION);
    }

    private Event findEvent(int eventId) {
        if (!fileLoaded) {
            throw new GmOperationException("No valid events file has been loaded yet. Load one first.");
        }
        return events.stream()
                .filter(e -> e.getId() == eventId)
                .findFirst()
                .orElseThrow(() -> new GmOperationException("No event found with id " + eventId + "."));
    }

    private EventSummaryDto toSummaryDto(Event event) {
        return new EventSummaryDto(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getCommissionPercent(),
                event.getCommissionType().getXmlValue(),
                event.getOptions(),
                event.isActive());
    }

    private EventStatusDto toStatusDto(Event event) {
        List<String> options = event.getOptions();
        List<OptionStatusDto> optionStatuses = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            optionStatuses.add(new OptionStatusDto(options.get(i), event.currentPrice(i), event.totalSharesBought(i)));
        }

        List<Trade> history = event.getTradeHistory();
        List<TradeDto> tradeDtos = new ArrayList<>();
        for (Trade trade : history) {
            tradeDtos.add(new TradeDto(options.get(trade.getOptionIndex()), trade.getQuantity(), trade.getPricePaid()));
        }
        Collections.reverse(tradeDtos);

        boolean closed = !event.isActive();
        String winningOptionName = null;
        if (closed && event.getWinningOptionIndex() != null) {
            winningOptionName = options.get(event.getWinningOptionIndex());
        }

        return new EventStatusDto(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getCommissionPercent(),
                event.getCommissionType().getXmlValue(),
                optionStatuses,
                event.getAccountBalance(),
                event.getTotalCommissionCollected(),
                tradeDtos,
                closed,
                winningOptionName);
    }
}
