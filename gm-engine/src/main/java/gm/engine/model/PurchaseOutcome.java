package gm.engine.model;

/** Internal result of {@link Event#buy}: how much the shares cost, and how much commission was added. */
public class PurchaseOutcome {

    private final double sharesCost;
    private final double commissionPaid;

    public PurchaseOutcome(double sharesCost, double commissionPaid) {
        this.sharesCost = sharesCost;
        this.commissionPaid = commissionPaid;
    }

    public double getSharesCost() {
        return sharesCost;
    }

    public double getCommissionPaid() {
        return commissionPaid;
    }

    public double getTotalPaid() {
        return sharesCost + commissionPaid;
    }
}
