package ru.push.caudioplayer.core.playlist.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.config.dto.PlaylistItemData;
import ru.push.caudioplayer.core.medialoader.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.playlist.model.MediaSourceType;
import ru.push.caudioplayer.core.playlist.dao.entity.PlaylistEntity;
import ru.push.caudioplayer.core.playlist.dao.entity.PlaylistItemEntity;
import ru.push.caudioplayer.core.playlist.model.Playlist;
import ru.push.caudioplayer.core.playlist.model.PlaylistTrack;
import ru.push.caudioplayer.core.playlist.model.PlaylistType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
class PlaylistMapper {

	private static final long DEEZER_DURATION_FACTOR = 1000;

	@Autowired
	private MediaInfoDataLoaderService mediaLoaderService;


	// Playlist mapping

	Playlist mapPlaylist(PlaylistEntity o) {
		return new Playlist(o.getUid(), o.getTitle(), PlaylistType.fromValue(o.getType()),
				o.getLink(), o.isReadOnly(), mapPlaylistItem(o.getItems()));
	}

	Playlist mapPlaylistDeezer(ru.push.caudioplayer.core.deezer.model.Playlist o) {
		return new Playlist(String.valueOf(o.getId()), o.getTitle(), PlaylistType.DEEZER,
				o.getLink(), o.getIs_loved_track(), mapPlaylistItemDeezer(o.getTracks().getData()));
	}

	PlaylistEntity inverseMapPlaylist(Playlist o) {
		return new PlaylistEntity(o.getUid(), o.getTitle(), o.getType().value(),
				o.getLink(), o.isReadOnly(), inverseMapPlaylistItem(o.getItems()));
	}

	List<Playlist> mapPlaylist(List<PlaylistEntity> list) {
		return (list != null) ?
				list.stream().map(this::mapPlaylist).collect(Collectors.toList()) :
				new ArrayList<>();
	}

	List<Playlist> mapPlaylist(List<PlaylistEntity> list, List<PlaylistItemData> itemsData) {
		if (list != null) {
			Map<String, Playlist> playlistsMap = list.stream()
					.map(this::mapPlaylist)
					.collect(Collectors.toMap(Playlist::getUid, o -> o));
			itemsData.forEach(o -> {
				if (playlistsMap.containsKey(o.getPlaylistUid())) {
					playlistsMap.get(o.getPlaylistUid()).setPosition(o.getPosition());
				}
			});
			return new ArrayList<>(playlistsMap.values());
		} else {
			return new ArrayList<>();
		}
	}

	List<Playlist> mapPlaylistDeezer(List<ru.push.caudioplayer.core.deezer.model.Playlist> list) {
		return (list != null) ?
				list.stream().map(this::mapPlaylistDeezer).collect(Collectors.toList()) :
				new ArrayList<>();
	}

	List<PlaylistEntity> inverseMapPlaylist(List<Playlist> list) {
		return (list != null) ?
				list.stream().map(this::inverseMapPlaylist).collect(Collectors.toList()) :
				new ArrayList<>();
	}


	// PlaylistTrack mapping

	PlaylistTrack mapPlaylistItem(PlaylistItemEntity o) {
		MediaSourceType sourceType = MediaSourceType.valueOf(o.getSourceType());
		if (MediaSourceType.HTTP_STREAM.equals(sourceType)) {
			PlaylistTrack track = new PlaylistTrack(o.getUid(), sourceType, o.getTrackPath());
			mediaLoaderService.fillMediaInfoFromHttpStreamByDecoder(track, track.getTrackPath());
			return track;
		} else {
			return new PlaylistTrack(
					o.getUid(), sourceType, o.getTrackPath(), o.getArtist(),
					o.getAlbum(), o.getDate(), o.getTitle(), o.getTrackNumber(), o.getLength()
			);
		}
	}

	private PlaylistTrack mapPlaylistItemDeezer(ru.push.caudioplayer.core.deezer.model.Track o) {
		return new PlaylistTrack(
				String.valueOf(o.getId()), MediaSourceType.DEEZER_MEDIA, o.getPreview(), o.getArtist().getName(),
				o.getAlbum().getTitle(), null, o.getTitle(), null, o.getDuration() * DEEZER_DURATION_FACTOR
		);
	}

	PlaylistTrack mapPlaylistItemDeezer(Playlist playlist, ru.push.caudioplayer.core.deezer.model.Track track) {
		long length = track.getDuration() * DEEZER_DURATION_FACTOR;
		return new PlaylistTrack(
				String.valueOf(track.getId()), MediaSourceType.DEEZER_MEDIA, track.getPreview(), playlist,
				track.getArtist().getName(), track.getAlbum().getTitle(), null, track.getTitle(), null, length
		);
	}

	PlaylistItemEntity inverseMapPlaylistItem(PlaylistTrack o) {
		MediaSourceType sourceType = o.getSourceType();
		if (MediaSourceType.HTTP_STREAM.equals(sourceType)) {
			return new PlaylistItemEntity(o.getUid(), sourceType.value(), o.getTrackPath());
		} else {
			return new PlaylistItemEntity(o.getUid(), sourceType.value(),
					o.getArtist(), o.getAlbum(), o.getDate(), o.getTitle(),
					o.getTrackNumber(), o.getLength(), o.getTrackPath());
		}
	}

	List<PlaylistTrack> mapPlaylistItem(List<PlaylistItemEntity> list) {
		return (list != null) ?
				list.stream().map(this::mapPlaylistItem).collect(Collectors.toList()) :
				new ArrayList<>();
	}

	List<PlaylistTrack> mapPlaylistItemDeezer(List<ru.push.caudioplayer.core.deezer.model.Track> list) {
		return (list != null) ?
				list.stream().map(this::mapPlaylistItemDeezer).collect(Collectors.toList()) :
				new ArrayList<>();
	}

	List<PlaylistItemEntity> inverseMapPlaylistItem(List<PlaylistTrack> list) {
		return (list != null) ?
				list.stream().map(this::inverseMapPlaylistItem).collect(Collectors.toList()) :
				new ArrayList<>();
	}

}
