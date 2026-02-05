package com.penguin.client.settings;

public class NumberSetting extends Setting {
    private double value;
    private double min;
    private double max;
    private double increment;

    public NumberSetting(String name, double defaultValue, double min, double max, double increment) {
        super(name);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
        if (this.value < min) this.value = min;
        if (this.value > max) this.value = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getIncrement() {
        return increment;
    }

    public void increment() {
        setValue(value + increment);
    }

    public void decrement() {
        setValue(value - increment);
    }
}
