package ru.push.caudioplayer.core.playlist;

import org.apache.commons.lang3.tuple.Pair;
import ru.push.caudioplayer.core.playlist.model.Playlist;
import ru.push.caudioplayer.core.playlist.model.PlaylistTrack;
import ru.push.caudioplayer.core.playlist.model.PlaylistType;
import ru.push.caudioplayer.core.playlist.dto.TrackData;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface PlaylistService {

	void reloadPlaylists();

	List<Playlist> getPlaylists();

	Optional<Playlist> getActivePlaylist();

	Optional<Playlist> getPlaylist(PlaylistType type, String title);

	Optional<PlaylistTrack> setActivePlaylistTrack(String playlistUid, String trackUid);

	Optional<PlaylistTrack> getActivePlaylistTrack();

	Optional<PlaylistTrack> nextActivePlaylistTrack();

	Optional<PlaylistTrack> prevActivePlaylistTrack();

	void resetActivePlaylistTrack();

	Playlist createPlaylist(PlaylistType type);

	Playlist createPlaylist(PlaylistType type, String playlistTitle);

	Playlist deletePlaylist(String playlistUid);

	Playlist renamePlaylist(String playlistUid, String newTitle);

	boolean exportPlaylistToFile(String playlistUid, String folderPath);

	Playlist addFilesToLocalPlaylist(String playlistUid, List<File> files);

	Playlist addLocationsToLocalPlaylist(String playlistUid, List<String> locations);

	Playlist addTrackToDeezerPlaylist(String playlistUid, TrackData trackData);

	Playlist addTrackToDeezerFavoritesPlaylist(TrackData trackData);

	Playlist deleteItemsFromPlaylist(String playlistUid, List<String> tracksUid);
}
