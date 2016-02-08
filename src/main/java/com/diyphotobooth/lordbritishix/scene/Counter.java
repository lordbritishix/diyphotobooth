package com.diyphotobooth.lordbritishix.scene;

import javafx.scene.Group;
import javafx.scene.text.Text;

public class Counter extends Group {
    private Text value;

    public Counter(int currentValue, int finalValue) {
        this.value = new Text("");
        this.value.setId("counter");
        getChildren().add(this.value);
    }

    private String format(int currentValue, int finalValue) {
        return String.valueOf(currentValue) + " / " + String.valueOf(finalValue);
    }

    public void setCounterValue(int currentValue, int finalValue) {
        this.value.setText(format(currentValue, finalValue));
    }

    public void clear() {
        value.setText("");
    }
}
