package ru.push.caudioplayer.core.converter.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.config.domain.PlaylistConfig;
import ru.push.caudioplayer.core.config.domain.Track;
import ru.push.caudioplayer.core.converter.ImportExportConverter;
import ru.push.caudioplayer.core.config.domain.PlaylistType;
import ru.push.caudioplayer.core.config.domain.SourceType;
import ru.push.caudioplayer.core.converter.domain.PlaylistExportData;
import ru.push.caudioplayer.core.converter.domain.TrackExportData;
import ru.push.caudioplayer.core.deezer.domain.Playlist;
import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.core.mediaplayer.domain.MediaSourceType;
import ru.push.caudioplayer.core.medialoader.MediaInfoDataLoaderService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImportExportConverterImpl implements ImportExportConverter {

	private static final Logger LOG = LoggerFactory.getLogger(ImportExportConverterImpl.class);

	@Autowired
	private MediaInfoDataLoaderService mediaInfoDataLoaderService;


	@Override
	public PlaylistConfig toPlaylistConfig(PlaylistData playlistData) {
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
	public PlaylistData toPlaylistData(PlaylistConfig playlistConfig) {
		List<AudioTrackData> tracksData = (playlistConfig.getTracks() != null) ?
				playlistConfig.getTracks().stream()
						.map(p -> mediaInfoDataLoaderService.load(p.getTrackPath(), MediaSourceType.valueOf(p.getSourceType().value())))
						.collect(Collectors.toList()) :
				new ArrayList<>();

		return new PlaylistData(
				playlistConfig.getUid(), playlistConfig.getName(),
				ru.push.caudioplayer.core.facades.domain.PlaylistType.fromValue(playlistConfig.getPlaylistType().value()),
				playlistConfig.getLink(), tracksData, true
		);
	}

	@Override
	public PlaylistExportData toPlaylistExportData(PlaylistData playlistData) {
		return new PlaylistExportData(
				playlistData.getName(),
				PlaylistType.valueOf(playlistData.getPlaylistType().value()),
				playlistData.getTracks().stream()
						.map(t -> new TrackExportData(
								MediaSourceType.DEEZER_MEDIA.equals(t.getSourceType()) ? t.getTrackId() : t.getTrackPath(),
								SourceType.fromValue(t.getSourceType().name()))
						)
						.collect(Collectors.toList())
		);
	}

	@Override
	public List<PlaylistExportData> toPlaylistExportData(List<PlaylistData> playlistData) {
		return playlistData.stream()
				.map(this::toPlaylistExportData)
				.collect(Collectors.toList());
	}

	@Override
	public PlaylistData toPlaylistDataFromExportData(PlaylistExportData exportData) {
		return new PlaylistData(
				exportData.getName(),
				ru.push.caudioplayer.core.facades.domain.PlaylistType.fromValue(exportData.getPlaylistType().value()),
				exportData.getTracks().stream()
						.map(p -> mediaInfoDataLoaderService.load(p.getUri(), MediaSourceType.valueOf(p.getSourceType().value())))
						.collect(Collectors.toList())
		);
	}

	@Override
	public PlaylistData toPlaylistDataFromDeezerData(ru.push.caudioplayer.core.deezer.domain.Playlist playlist,
																									 List<ru.push.caudioplayer.core.deezer.domain.Track> tracks) {
		return new PlaylistData(String.valueOf(playlist.getId()), playlist.getTitle(),
				ru.push.caudioplayer.core.facades.domain.PlaylistType.DEEZER, playlist.getLink(),
				toAudioTrackData(tracks), !playlist.getIs_loved_track()
		);
	}

	@Override
	public Playlist toPlaylistDeezerData(PlaylistData playlistData) {
		Playlist playlist = new Playlist();
		playlist.setId(Long.valueOf(playlistData.getUid()));
		playlist.setTitle(playlistData.getName());
		return playlist;
	}

	@Override
	public List<AudioTrackData> toAudioTrackData(List<ru.push.caudioplayer.core.deezer.domain.Track> tracks) {
		return tracks.stream()
				.map(t -> new AudioTrackData.Builder(t.getPreview(), MediaSourceType.DEEZER_MEDIA, t.getArtist().getName(), t.getTitle())
						.trackId(String.valueOf(t.getId()))
						.album(t.getAlbum().getTitle())
						.length(t.getDuration() * 1000) // duration in seconds, length in milliseconds
						.build())
				.collect(Collectors.toList());
	}

}
