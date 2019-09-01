package ru.push.caudioplayer.core.converter;

import ru.push.caudioplayer.core.config.domain.PlaylistConfig;
import ru.push.caudioplayer.core.converter.domain.PlaylistExportData;
import ru.push.caudioplayer.core.deezer.domain.Playlist;
import ru.push.caudioplayer.core.deezer.domain.Track;
import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;

import java.util.List;

public interface ImportExportConverter {

	PlaylistConfig toPlaylistConfig(PlaylistData playlistData);

	PlaylistData toPlaylistData(PlaylistConfig playlistConfig);

	PlaylistExportData toPlaylistExportData(PlaylistData playlistData);

	List<PlaylistExportData> toPlaylistExportData(List<PlaylistData> playlistData);

	PlaylistData toPlaylistDataFromExportData(PlaylistExportData playlistConfig);

	PlaylistData toPlaylistDataFromDeezerData(Playlist playlist, List<Track> tracks);

	List<AudioTrackData> toAudioTrackData(List<Track> tracks);
}
