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

@Component("localPlaylistMapper")
class PlaylistMapper {

	@Autowired
	private MediaInfoDataLoaderService mediaLoaderService;


	// Playlist mapping

	Playlist mapPlaylist(PlaylistEntity o) {
		return new Playlist(o.getUid(), o.getTitle(), PlaylistType.fromValue(o.getType()),
				o.getLink(), o.isReadOnly(), mapPlaylistItem(o.getItems()));
	}

	PlaylistEntity inverseMapPlaylist(Playlist o) {
		PlaylistEntity entity = new PlaylistEntity(o.getUid(), o.getTitle(), o.getType().value(), o.getLink(), o.isReadOnly());
		List<PlaylistItemEntity> items = inverseMapPlaylistItem(o.getItems(), entity);
		entity.setItems(items);
		return entity;
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

	List<PlaylistEntity> inverseMapPlaylist(List<Playlist> list) {
		return (list != null) ?
				list.stream().map(this::inverseMapPlaylist).collect(Collectors.toList()) :
				new ArrayList<>();
	}


	// PlaylistTrack mapping

	PlaylistTrack mapPlaylistItem(Playlist playlist, PlaylistItemEntity o) {
		MediaSourceType sourceType = MediaSourceType.valueOf(o.getSourceType());
		if (MediaSourceType.HTTP_STREAM.equals(sourceType)) {
			PlaylistTrack track = new PlaylistTrack(o.getUid(), sourceType, o.getTrackPath(), playlist);
			mediaLoaderService.fillMediaInfoFromHttpStreamByDecoder(track, track.getTrackPath());
			return track;
		} else {
			return new PlaylistTrack(
					o.getUid(), sourceType, o.getTrackPath(), playlist, o.getArtist(),
					o.getAlbum(), o.getDate(), o.getTitle(), o.getTrackNumber(), o.getLength()
			);
		}
	}

	PlaylistTrack mapPlaylistItem(PlaylistItemEntity o) {
		return mapPlaylistItem(null, o);
	}

	PlaylistItemEntity inverseMapPlaylistItem(PlaylistTrack o, PlaylistEntity playlist) {
		return new PlaylistItemEntity(
				o.getUid(), o.getSourceType().value(), o.getArtist(), o.getAlbum(), o.getDate(), o.getTitle(),
				o.getTrackNumber(), o.getLength(), o.getTrackPath(), playlist
		);
	}

	List<PlaylistTrack> mapPlaylistItem(List<PlaylistItemEntity> list) {
		return (list != null) ?
				list.stream().map(this::mapPlaylistItem).collect(Collectors.toList()) :
				new ArrayList<>();
	}

	List<PlaylistItemEntity> inverseMapPlaylistItem(List<PlaylistTrack> list, PlaylistEntity playlist) {
		return (list != null) ?
				list.stream().map(i -> inverseMapPlaylistItem(i, playlist)).collect(Collectors.toList()) :
				new ArrayList<>();
	}

}
