package ru.push.caudioplayer.core.playlist.dao;

import ru.push.caudioplayer.core.playlist.dao.entity.PlaylistEntity;

import java.util.List;

public interface LocalPlaylistRepository {

	boolean refresh();

	PlaylistEntity findPlaylist(String uid);

	List<PlaylistEntity> findPlaylists(List<String> uidList);

	boolean savePlaylist(PlaylistEntity playlist);

	boolean deletePlaylist(String uid);
}
