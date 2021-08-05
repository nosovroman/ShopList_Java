package com.example.firstapp;

public class SListStructure {
    private String nameItem;
    private boolean done;

    public SListStructure() {}

    public SListStructure(boolean done, String nameItem) {
        this.nameItem = nameItem;
        this.done = done;
    }

    public String getNameItem() {
        return nameItem;
    }

    public void setNameItem(String nameItem) {
        this.nameItem = nameItem;
    }

    public boolean getDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}