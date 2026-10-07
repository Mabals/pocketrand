package io.github.mabals.pocketrand.model;

public enum Category {
    INCOME("Income"),
    HOUSING("Housing"),
    GROCERIES("Groceries"),
    TRANSPORT("Transport"),
    AIRTIME_DATA("Airtime and data"),
    EATING_OUT("Eating out"),
    ENTERTAINMENT("Entertainment"),
    UTILITIES("Utilities"),
    HEALTH("Health"),
    EDUCATION("Education"),
    SHOPPING("Shopping"),
    TRANSFERS("Transfers"),
    FEES("Fees"),
    OTHER("Other");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
