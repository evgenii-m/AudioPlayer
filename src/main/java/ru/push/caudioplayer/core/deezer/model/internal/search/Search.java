package ru.push.caudioplayer.core.deezer.model.internal.search;

public class Search {

    private String text;
    private SearchOrder searchOrder;

    public Search(String text) {
        this.text = text;
    }

    public Search(String text, SearchOrder searchOrder) {
        this.text = text;
        this.searchOrder = searchOrder;
    }

    public String getText() {
        return text;
    }

    public SearchOrder getSearchOrder() {
        return searchOrder;
    }
}
