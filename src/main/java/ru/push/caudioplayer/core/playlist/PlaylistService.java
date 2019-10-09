package ru.push.caudioplayer.core.playlist;

import ru.push.caudioplayer.core.playlist.model.Playlist;

import java.util.List;
import java.util.Optional;

public interface PlaylistService {

	Optional<Playlist> getPlaylist(String playlistUid);

	List<Playlist> getPlaylistByTitle(String title);

	List<Playlist> getPlaylists();

	Playlist createPlaylist();

	Playlist createPlaylist(String title);

	Playlist deletePlaylist(String playlistUid);

	Playlist renamePlaylist(String playlistUid, String newTitle);

	boolean exportPlaylistToFile(String playlistUid, String folderPath);

	Playlist deleteItemsFromPlaylist(String playlistUid, List<String> tracksUid);
}
