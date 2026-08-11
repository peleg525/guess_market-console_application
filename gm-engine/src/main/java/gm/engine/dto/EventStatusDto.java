package gm.engine.dto;

import java.util.List;

/** Full trading status of one event, answering "command 3" of the assignment. */
public final class EventStatusDto {

    private final int id;
    private final String name;
    private final String description;
    private final int commissionPercent;
    private final String commissionType;
    private final List<OptionStatusDto> optionStatuses;
    private final double eventAccountBalance;
    private final double totalCommissionCollected;
    private final List<TradeDto> tradeHistoryNewestFirst;
    private final boolean closed;
    private final String winningOptionName;

    public EventStatusDto(int id, String name, String description, int commissionPercent, String commissionType,
                           List<OptionStatusDto> optionStatuses, double eventAccountBalance,
                           double totalCommissionCollected, List<TradeDto> tradeHistoryNewestFirst,
                           boolean closed, String winningOptionName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.commissionPercent = commissionPercent;
        this.commissionType = commissionType;
        this.optionStatuses = List.copyOf(optionStatuses);
        this.eventAccountBalance = eventAccountBalance;
        this.totalCommissionCollected = totalCommissionCollected;
        this.tradeHistoryNewestFirst = List.copyOf(tradeHistoryNewestFirst);
        this.closed = closed;
        this.winningOptionName = winningOptionName;
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

    public String getCommissionType() {
        return commissionType;
    }

    public List<OptionStatusDto> getOptionStatuses() {
        return optionStatuses;
    }

    public double getEventAccountBalance() {
        return eventAccountBalance;
    }

    public double getTotalCommissionCollected() {
        return totalCommissionCollected;
    }

    public List<TradeDto> getTradeHistoryNewestFirst() {
        return tradeHistoryNewestFirst;
    }

    public boolean isClosed() {
        return closed;
    }

    public String getWinningOptionName() {
        return winningOptionName;
    }
}
