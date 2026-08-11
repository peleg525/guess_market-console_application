package gm.ui;

import gm.engine.GmEngine;
import gm.engine.dto.EventStatusDto;
import gm.engine.dto.EventSummaryDto;
import gm.engine.dto.OptionStatusDto;
import gm.engine.dto.PurchaseResultDto;
import gm.engine.dto.TradeDto;
import gm.engine.exception.GmException;

import java.util.List;
import java.util.Scanner;

/**
 * The one and only place in this application that prints to the console or reads from the
 * keyboard. Talks to the engine exclusively through the {@link GmEngine} interface and the DTOs
 * it returns.
 */
public class ConsoleApp {

    private final GmEngine engine;
    private final InputReader input;
    private boolean running = true;

    public ConsoleApp(GmEngine engine) {
        this.engine = engine;
        this.input = new InputReader(new Scanner(System.in));
    }

    public void run() {
        System.out.println("Welcome to Guess Market.");
        while (running) {
            printMenu();
            int choice = input.readInt("Enter a command number: ");
            try {
                dispatch(choice);
            } catch (GmException e) {
                System.out.println();
                System.out.println(e.getMessage());
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("===== Guess Market Menu =====");
        System.out.println("1. Load events file");
        System.out.println("2. List events");
        System.out.println("3. View event trading status");
        System.out.println("4. Participate in an event (buy shares)");
        System.out.println("5. Close an event");
        System.out.println("6. Save application state to a file (bonus)");
        System.out.println("7. Load a previously saved application state (bonus)");
        System.out.println("8. Exit");
    }

    private void dispatch(int choice) {
        switch (choice) {
            case 1:
                loadFile();
                break;
            case 2:
                listEvents();
                break;
            case 3:
                viewEventStatus();
                break;
            case 4:
                buyShares();
                break;
            case 5:
                closeEvent();
                break;
            case 6:
                saveState();
                break;
            case 7:
                loadState();
                break;
            case 8:
                running = false;
                System.out.println("Goodbye.");
                break;
            default:
                System.out.println("Unknown command: " + choice + ". Please choose a number from the menu.");
        }
    }

    // ---- Command 1 ----

    private void loadFile() {
        String path = input.readLine("Enter the full path of the XML events file to load: ");
        int count = engine.loadEventsFile(path);
        System.out.println("File loaded successfully. " + count + " event(s) were loaded.");
    }

    // ---- Command 2 ----

    private void listEvents() {
        if (!requireFileLoaded()) {
            return;
        }
        List<EventSummaryDto> events = engine.getAllEvents();
        if (events.isEmpty()) {
            System.out.println("There are no events currently loaded.");
            return;
        }
        System.out.println("Events currently loaded:");
        for (int i = 0; i < events.size(); i++) {
            printEventSummary(i + 1, events.get(i));
        }
    }

    private void printEventSummary(int displayNumber, EventSummaryDto event) {
        System.out.println();
        System.out.println(displayNumber + ") Event #" + event.getId() + " - " + event.getName());
        System.out.println("   Description: " + event.getDescription());
        System.out.println("   Commission: " + event.getCommissionPercent() + "% (" + event.getCommissionType() + ")");
        System.out.print("   Options:");
        List<String> options = event.getOptions();
        for (int i = 0; i < options.size(); i++) {
            System.out.print("  " + (i + 1) + ". " + options.get(i));
        }
        System.out.println();
        System.out.println("   Status: " + (event.isActive() ? "ACTIVE" : "CLOSED"));
    }

    // ---- Command 3 ----

    private void viewEventStatus() {
        if (!requireFileLoaded()) {
            return;
        }
        List<EventSummaryDto> events = engine.getAllEvents();
        if (events.isEmpty()) {
            System.out.println("There are no events currently loaded.");
            return;
        }
        System.out.println("Choose an event to view:");
        for (int i = 0; i < events.size(); i++) {
            printEventSummary(i + 1, events.get(i));
        }
        int selection = input.readSelection("Enter the event number: ", events.size());
        int eventId = events.get(selection - 1).getId();

        EventStatusDto status = engine.getEventStatus(eventId);
        printEventStatus(status);
    }

    private void printEventStatus(EventStatusDto status) {
        System.out.println();
        System.out.println("Event #" + status.getId() + " - " + status.getName());
        System.out.println("Description: " + status.getDescription());
        System.out.println("Commission: " + status.getCommissionPercent() + "% (" + status.getCommissionType() + ")");
        System.out.println("Current market state:");
        for (OptionStatusDto option : status.getOptionStatuses()) {
            System.out.println("   " + option.getOptionName() + " -> price: " + Format.money(option.getCurrentPrice())
                    + "   total shares bought: " + Format.shares(option.getTotalSharesBought()));
        }
        System.out.println("Event account balance: " + Format.money(status.getEventAccountBalance()));
        System.out.println("Total commission collected so far: " + Format.money(status.getTotalCommissionCollected()));

        System.out.println("Trade history (most recent first):");
        List<TradeDto> history = status.getTradeHistoryNewestFirst();
        if (history.isEmpty()) {
            System.out.println("   (no trades yet)");
        } else {
            for (TradeDto trade : history) {
                System.out.println("   " + trade.getOptionName() + " -> quantity: " + trade.getQuantity()
                        + "   price paid: " + Format.money(trade.getPricePaid()));
            }
        }

        if (status.isClosed()) {
            System.out.println("This event is CLOSED. Winning option: " + status.getWinningOptionName());
        } else {
            System.out.println("This event is ACTIVE.");
        }
    }

    // ---- Command 4 ----

    private void buyShares() {
        if (!requireFileLoaded()) {
            return;
        }
        List<EventSummaryDto> activeEvents = engine.getActiveEvents();
        if (activeEvents.isEmpty()) {
            System.out.println("There are no active events to participate in.");
            return;
        }
        System.out.println("Choose an event to participate in:");
        for (int i = 0; i < activeEvents.size(); i++) {
            printEventSummary(i + 1, activeEvents.get(i));
        }
        int selection = input.readSelection("Enter the event number: ", activeEvents.size());
        EventSummaryDto chosenEvent = activeEvents.get(selection - 1);
        int eventId = chosenEvent.getId();

        printEventStatus(engine.getEventStatus(eventId));

        int optionNumber = input.readSelection("Choose an option to buy (1-" + chosenEvent.getOptions().size() + "): ",
                chosenEvent.getOptions().size());
        int quantity = input.readPositiveInt("Enter the quantity of shares to buy: ");

        PurchaseResultDto result = engine.buyShares(eventId, optionNumber, quantity);

        System.out.println();
        System.out.println("Purchase completed.");
        System.out.println("Shares cost: " + Format.money(result.getSharesCost()));
        System.out.println("Commission paid: " + Format.money(result.getCommissionPaid()));
        System.out.println("Total paid: " + Format.money(result.getTotalPaid()));
        printEventStatus(result.getUpdatedStatus());
    }

    // ---- Command 5 ----

    private void closeEvent() {
        if (!requireFileLoaded()) {
            return;
        }
        List<EventSummaryDto> activeEvents = engine.getActiveEvents();
        if (activeEvents.isEmpty()) {
            System.out.println("There are no active events to close.");
            return;
        }
        System.out.println("Choose an event to close:");
        for (int i = 0; i < activeEvents.size(); i++) {
            printEventSummary(i + 1, activeEvents.get(i));
        }
        int selection = input.readSelection("Enter the event number: ", activeEvents.size());
        EventSummaryDto chosenEvent = activeEvents.get(selection - 1);
        int eventId = chosenEvent.getId();

        printEventStatus(engine.getEventStatus(eventId));

        int winningOptionNumber = input.readSelection(
                "Choose the winning option (1-" + chosenEvent.getOptions().size() + "): ",
                chosenEvent.getOptions().size());

        EventStatusDto summary = engine.closeEvent(eventId, winningOptionNumber);

        System.out.println();
        System.out.println("Event closed.");
        printEventStatus(summary);
    }

    // ---- Command 6 (bonus) ----

    private void saveState() {
        String path = input.readLine(
                "Enter the full path, including the file name but without an extension, to save to: ");
        engine.saveState(path);
        System.out.println("Application state saved successfully.");
    }

    // ---- Command 7 (bonus) ----

    private void loadState() {
        String path = input.readLine(
                "Enter the full path, including the file name but without an extension, of a previously saved state: ");
        engine.loadState(path);
        System.out.println("Application state loaded successfully.");
    }

    // ---- shared ----

    private boolean requireFileLoaded() {
        if (!engine.isFileLoaded()) {
            System.out.println("No valid events file has been loaded yet. Use command 1 first.");
            return false;
        }
        return true;
    }
}
