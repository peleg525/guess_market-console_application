package gm.engine.dto;

/** Result of a share purchase: the payment breakdown plus the event's refreshed status. */
public final class PurchaseResultDto {

    private final double sharesCost;
    private final double commissionPaid;
    private final double totalPaid;
    private final EventStatusDto updatedStatus;

    public PurchaseResultDto(double sharesCost, double commissionPaid, double totalPaid, EventStatusDto updatedStatus) {
        this.sharesCost = sharesCost;
        this.commissionPaid = commissionPaid;
        this.totalPaid = totalPaid;
        this.updatedStatus = updatedStatus;
    }

    public double getSharesCost() {
        return sharesCost;
    }

    public double getCommissionPaid() {
        return commissionPaid;
    }

    public double getTotalPaid() {
        return totalPaid;
    }

    public EventStatusDto getUpdatedStatus() {
        return updatedStatus;
    }
}
