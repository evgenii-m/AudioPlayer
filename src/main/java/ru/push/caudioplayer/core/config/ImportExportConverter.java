package ru.push.caudioplayer.core.config;

import ru.push.caudioplayer.core.config.domain.PlaylistConfig;
import ru.push.caudioplayer.core.deezer.domain.Playlist;
import ru.push.caudioplayer.core.deezer.domain.Track;
import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;

import java.util.List;

public interface ImportExportConverter {

	PlaylistConfig convertPlaylist(PlaylistData playlistData);

	PlaylistData convertPlaylist(PlaylistConfig playlistConfig);

	PlaylistData convertDeezerPlaylist(Playlist playlist, List<Track> tracks);

	List<AudioTrackData> convertDeezerTracks(List<Track> tracks);
}
