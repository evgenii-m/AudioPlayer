package ru.push.caudioplayer.core.deezer.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Autocomplete {

    private Integer total;
    private String next;

    private Tracks tracks;
    private Albums albums;
    private Artists artists;

    public Artists getArtists() {
        return artists;
    }

    public void setArtists(Artists artists) {
        this.artists = artists;
    }

    public Albums getAlbums() {
        return albums;
    }

    public void setAlbums(Albums albums) {
        this.albums = albums;
    }

    public Tracks getTracks() {
        return tracks;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public void setTracks(Tracks tracks) {
        this.tracks = tracks;

    }
}
