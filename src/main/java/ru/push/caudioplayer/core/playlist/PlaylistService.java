package ru.push.caudioplayer.core.playlist;

import ru.push.caudioplayer.core.playlist.domain.Playlist;
import ru.push.caudioplayer.core.playlist.domain.PlaylistItem;
import ru.push.caudioplayer.core.playlist.domain.PlaylistType;
import ru.push.caudioplayer.core.playlist.dto.TrackData;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface PlaylistService {

	void reloadPlaylists();

	List<Playlist> getPlaylists();

	Optional<Playlist> getActivePlaylist();

	Optional<PlaylistItem> setActivePlaylistTrack(String playlistUid, int trackIndex);

	Optional<PlaylistItem> getActivePlaylistTrack();

	Optional<PlaylistItem> nextActivePlaylistTrack();

	Optional<PlaylistItem> prevActivePlaylistTrack();

	Playlist createPlaylist(PlaylistType type);

	boolean deletePlaylist(String playlistUid);

	Playlist renamePlaylist(String playlistUid, String newTitle);

	boolean exportPlaylistToFile(String playlistUid, String folderPath);

	Playlist addFilesToLocalPlaylist(String playlistUid, List<File> files);

	Playlist addLocationsToLocalPlaylist(String playlistUid, List<String> locations);

	Playlist addTrackToDeezerPlaylist(String playlistUid, TrackData trackData);

	Playlist addTrackToDeezerFavoritesPlaylist(TrackData trackData);

	Playlist deleteItemsFromPlaylist(String playlistUid, List<Integer> itemsIndexes);
}
