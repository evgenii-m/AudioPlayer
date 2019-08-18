package ru.push.caudioplayer.core.deezer.domain.internal.search;

public class SearchUser extends Search {

    public SearchUser(String text) {
        super(text);
    }

    public SearchUser(String text, SearchOrder searchOrder) {
        super(text, searchOrder);
    }

}
