package gm.engine.model;

import java.io.Serializable;

public class Trade implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int optionIndex;
    private final int quantity;
    private final double pricePaid;

    public Trade(int optionIndex, int quantity, double pricePaid) {
        this.optionIndex = optionIndex;
        this.quantity = quantity;
        this.pricePaid = pricePaid;
    }

    public int getOptionIndex() {
        return optionIndex;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPricePaid() {
        return pricePaid;
    }
}
