package com.example.foodorderapp.Domain;

public class Price {

    private String Id;
    private String Value;

    public Price() {
    }

    @Override
    public String toString() {
        return Value;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }
}
