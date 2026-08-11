package gm.engine.dto;

import java.util.List;

/** Read-only summary of an event, as shown by the "list events" command. */
public final class EventSummaryDto {

    private final int id;
    private final String name;
    private final String description;
    private final int commissionPercent;
    private final String commissionType;
    private final List<String> options;
    private final boolean active;

    public EventSummaryDto(int id, String name, String description, int commissionPercent,
                            String commissionType, List<String> options, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.commissionPercent = commissionPercent;
        this.commissionType = commissionType;
        this.options = List.copyOf(options);
        this.active = active;
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

    public List<String> getOptions() {
        return options;
    }

    public boolean isActive() {
        return active;
    }
}
