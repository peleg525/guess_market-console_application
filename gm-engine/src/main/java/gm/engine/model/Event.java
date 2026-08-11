package gm.engine.model;

import gm.engine.exception.GmOperationException;
import gm.engine.lmsr.LmsrCalculator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A single binary Guess Market event, traded through LMSR. Owns its own money and share state and
 * validates every action against it - callers (the engine facade) never poke at raw fields.
 */
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String description;
    private final int commissionPercent;
    private final CommissionType commissionType;
    private final List<String> options;
    private final int liquidityB;

    private final double[] quantities;
    private double accountBalance;
    private double totalCommissionCollected;
    private final List<Trade> tradeHistory = new ArrayList<>();
    private EventStatus status = EventStatus.ACTIVE;
    private Integer winningOptionIndex = null;

    public Event(int id, String name, String description, int commissionPercent,
                 CommissionType commissionType, List<String> options, int liquidityB) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.commissionPercent = commissionPercent;
        this.commissionType = commissionType;
        this.options = List.copyOf(options);
        this.liquidityB = liquidityB;
        this.quantities = new double[options.size()];
        // Initial LMSR subsidy: the cost of the market when no shares have been bought yet.
        this.accountBalance = LmsrCalculator.cost(quantities, liquidityB);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCommissionPercent() {
        return commissionPercent;
    }

    public CommissionType getCommissionType() {
        return commissionType;
    }

    public List<String> getOptions() {
        return options;
    }

    public EventStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == EventStatus.ACTIVE;
    }

    public Integer getWinningOptionIndex() {
        return winningOptionIndex;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public double getTotalCommissionCollected() {
        return totalCommissionCollected;
    }

    public List<Trade> getTradeHistory() {
        return Collections.unmodifiableList(tradeHistory);
    }

    public double currentPrice(int optionIndex) {
        return LmsrCalculator.price(quantities, liquidityB, optionIndex);
    }

    public double totalSharesBought(int optionIndex) {
        return quantities[optionIndex];
    }

    private void requireValidOptionIndex(int optionIndex) {
        if (optionIndex < 0 || optionIndex >= options.size()) {
            throw new GmOperationException("Invalid option number for event '" + name + "'. "
                    + "Choose a number between 1 and " + options.size() + ".");
        }
    }

    public PurchaseOutcome buy(int optionIndex, int quantity) {
        if (status != EventStatus.ACTIVE) {
            throw new GmOperationException("Event '" + name + "' is closed. Trading is no longer possible.");
        }
        requireValidOptionIndex(optionIndex);
        if (quantity <= 0) {
            throw new GmOperationException("Quantity must be a positive whole number of shares.");
        }

        double before = LmsrCalculator.cost(quantities, liquidityB);
        quantities[optionIndex] += quantity;
        double after = LmsrCalculator.cost(quantities, liquidityB);
        double sharesCost = after - before;

        double commission = commissionType == CommissionType.ON_PURCHASE
                ? sharesCost * commissionPercent / 100.0
                : 0.0;

        accountBalance += sharesCost + commission;
        totalCommissionCollected += commission;
        tradeHistory.add(new Trade(optionIndex, quantity, sharesCost));

        return new PurchaseOutcome(sharesCost, commission);
    }

    public void close(int winningOptionIndex) {
        if (status != EventStatus.ACTIVE) {
            throw new GmOperationException("Event '" + name + "' is already closed.");
        }
        requireValidOptionIndex(winningOptionIndex);

        double totalInvestmentInWinningOption = tradeHistory.stream()
                .filter(trade -> trade.getOptionIndex() == winningOptionIndex)
                .mapToDouble(Trade::getPricePaid)
                .sum();

        double fee = commissionType == CommissionType.ON_CLOSE
                ? totalInvestmentInWinningOption * commissionPercent / 100.0
                : 0.0;

        totalCommissionCollected += fee;
        double amountPaidToWinners = totalInvestmentInWinningOption - fee;
        accountBalance -= amountPaidToWinners;

        this.status = EventStatus.CLOSED;
        this.winningOptionIndex = winningOptionIndex;
    }
}
