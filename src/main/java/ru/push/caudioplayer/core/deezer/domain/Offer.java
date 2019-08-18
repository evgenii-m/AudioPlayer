package ru.push.caudioplayer.core.deezer.domain;

public class Offer {

    private Integer id;
    private String name;
    private String currency;
    private String displayed_amount;
    private String tc;
    private Integer try_and_buy;
    private Double amount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDisplayed_amount() {
        return displayed_amount;
    }

    public void setDisplayed_amount(String displayed_amount) {
        this.displayed_amount = displayed_amount;
    }

    public String getTc() {
        return tc;
    }

    public void setTc(String tc) {
        this.tc = tc;
    }

    public Integer getTry_and_buy() {
        return try_and_buy;
    }

    public void setTry_and_buy(Integer try_and_buy) {
        this.try_and_buy = try_and_buy;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
