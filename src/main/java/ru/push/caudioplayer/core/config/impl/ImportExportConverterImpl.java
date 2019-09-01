package ru.push.caudioplayer.core.config.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.config.ImportExportConverter;
import ru.push.caudioplayer.core.config.domain.PlaylistConfig;
import ru.push.caudioplayer.core.config.domain.PlaylistType;
import ru.push.caudioplayer.core.config.domain.SourceType;
import ru.push.caudioplayer.core.config.domain.Track;
import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.core.mediaplayer.domain.MediaSourceType;
import ru.push.caudioplayer.core.services.MediaInfoDataLoaderService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImportExportConverterImpl implements ImportExportConverter {

	private static final Logger LOG = LoggerFactory.getLogger(ImportExportConverterImpl.class);

	@Autowired
	private MediaInfoDataLoaderService mediaInfoDataLoaderService;

	@Override
	public PlaylistConfig convertPlaylist(PlaylistData playlistData) {
		return new PlaylistConfig(
				playlistData.getUid(), playlistData.getName(),
				PlaylistType.valueOf(playlistData.getPlaylistType().value()),
				playlistData.getLink(),
				playlistData.getTracks().stream()
						.map(t -> new Track(SourceType.fromValue(t.getSourceType().name()), t.getTrackPath()))
						.collect(Collectors.toList())
		);
	}

	@Override
	public PlaylistData convertPlaylist(PlaylistConfig playlistConfig) {
		if (PlaylistType.DEEZER.equals(playlistConfig.getPlaylistType())) {
			throw new IllegalStateException("Operation not supported for Deezer playlists");
		}
		return new PlaylistData(
				playlistConfig.getUid(), playlistConfig.getName(),
				ru.push.caudioplayer.core.facades.domain.PlaylistType.fromValue(playlistConfig.getPlaylistType().value()),
				playlistConfig.getLink(), createMediaInfoDataList(playlistConfig.getTracks()));
	}

	@Override
	public PlaylistData convertDeezerPlaylist(ru.push.caudioplayer.core.deezer.domain.Playlist playlist,
																						List<ru.push.caudioplayer.core.deezer.domain.Track> tracks) {
		return new PlaylistData(String.valueOf(playlist.getId()), playlist.getTitle(),
				ru.push.caudioplayer.core.facades.domain.PlaylistType.DEEZER, playlist.getLink(),
				convertDeezerTracks(tracks)
		);
	}

	@Override
	public List<AudioTrackData> convertDeezerTracks(List<ru.push.caudioplayer.core.deezer.domain.Track> tracks) {
		return tracks.stream()
				.map(t -> new AudioTrackData.Builder(t.getPreview(), MediaSourceType.HTTP_STREAM, t.getArtist().getName(), t.getTitle())
						.album(t.getAlbum().getTitle())
						.length(t.getDuration() * 1000) // duration in seconds, length in milliseconds
						.build())
				.collect(Collectors.toList());
	}


	private List<AudioTrackData> createMediaInfoDataList(List<Track> playlistTracks) {
		assert playlistTracks != null;

		return playlistTracks.stream()
				.map(p -> mediaInfoDataLoaderService.load(p.getTrackPath(), MediaSourceType.valueOf(p.getSourceType().value())))
				.collect(Collectors.toList());
	}
}
