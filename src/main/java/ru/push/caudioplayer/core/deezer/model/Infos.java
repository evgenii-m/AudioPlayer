package ru.push.caudioplayer.core.deezer.model;

import java.util.List;

public class Infos {

    private String country_iso;
    private String country;
    private Boolean open;
    private String pop;
    private String upload_token;
    private List<Offer> offers;

    public String getCountry_iso() {
        return country_iso;
    }

    public void setCountry_iso(String country_iso) {
        this.country_iso = country_iso;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public String getPop() {
        return pop;
    }

    public void setPop(String pop) {
        this.pop = pop;
    }

    public String getUpload_token() {
        return upload_token;
    }

    public void setUpload_token(String upload_token) {
        this.upload_token = upload_token;
    }

    public List<Offer> getOffers() {
        return offers;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }
}
