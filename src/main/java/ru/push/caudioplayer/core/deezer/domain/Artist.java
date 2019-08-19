package ru.push.caudioplayer.core.deezer.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Artist {

    private Long id;
    private String name;
    private String link;
    private String picture;
    private Integer nb_album;
    private Long nb_fan;
    private Boolean radio;
    private String type;
    private String tracklist;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Integer getNb_album() {
        return nb_album;
    }

    public void setNb_album(Integer nb_album) {
        this.nb_album = nb_album;
    }

    public Long getNb_fan() {
        return nb_fan;
    }

    public void setNb_fan(Long nb_fan) {
        this.nb_fan = nb_fan;
    }

    public Boolean getRadio() {
        return radio;
    }

    public void setRadio(Boolean radio) {
        this.radio = radio;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTracklist() {
        return tracklist;
    }

    public void setTracklist(String tracklist) {
        this.tracklist = tracklist;
    }
}