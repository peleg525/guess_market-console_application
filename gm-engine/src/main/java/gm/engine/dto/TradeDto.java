package gm.engine.dto;

/** One line of an event's trade history. */
public final class TradeDto {

    private final String optionName;
    private final int quantity;
    private final double pricePaid;

    public TradeDto(String optionName, int quantity, double pricePaid) {
        this.optionName = optionName;
        this.quantity = quantity;
        this.pricePaid = pricePaid;
    }

    public String getOptionName() {
        return optionName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPricePaid() {
        return pricePaid;
    }
}
