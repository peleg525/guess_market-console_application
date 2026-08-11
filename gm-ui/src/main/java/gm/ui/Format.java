package gm.ui;

final class Format {

    private Format() {
    }

    /** Formats a decimal amount with exactly 2 digits after the point, as required everywhere in the spec. */
    static String money(double value) {
        double cleaned = (Math.abs(value) < 0.005) ? 0.0 : value;
        return String.format("%.2f", cleaned);
    }

    /** Share quantities are always whole numbers in this exercise; print them without decimals. */
    static String shares(double value) {
        return String.valueOf(Math.round(value));
    }
}
