package ru.push.caudioplayer.core.playlist;

import ru.push.caudioplayer.core.playlist.model.Playlist;
import ru.push.caudioplayer.core.playlist.model.PlaylistTrack;
import ru.push.caudioplayer.core.playlist.model.PlaylistType;
import ru.push.caudioplayer.core.playlist.dto.TrackData;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface LocalPlaylistService extends PlaylistService {

	Optional<Playlist> getActivePlaylist();

	Optional<PlaylistTrack> setActivePlaylistTrack(String playlistUid, String trackUid);

	Optional<PlaylistTrack> getActivePlaylistTrack();

	void resetActivePlaylistTrack();

	Optional<PlaylistTrack> nextActivePlaylistTrack();

	Optional<PlaylistTrack> prevActivePlaylistTrack();

	Playlist addFilesToLocalPlaylist(String playlistUid, List<File> files);

	Playlist addLocationsToLocalPlaylist(String playlistUid, List<String> locations);
}
