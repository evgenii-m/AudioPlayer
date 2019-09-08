package ru.push.caudioplayer.core.deezer.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RadioGenresItem {

    private Long id;
    private String title;
    private List<RadioItem> radios;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<RadioItem> getRadios() {
        return radios;
    }

    public void setRadios(List<RadioItem> radios) {
        this.radios = radios;
    }
}
