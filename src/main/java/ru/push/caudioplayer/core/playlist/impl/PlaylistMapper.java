package ru.push.caudioplayer.core.playlist.impl;

import ru.push.caudioplayer.core.playlist.domain.MediaSourceType;
import ru.push.caudioplayer.core.playlist.dao.model.PlaylistEntity;
import ru.push.caudioplayer.core.playlist.dao.model.PlaylistItemEntity;
import ru.push.caudioplayer.core.playlist.domain.Playlist;
import ru.push.caudioplayer.core.playlist.domain.PlaylistTrack;
import ru.push.caudioplayer.core.playlist.domain.PlaylistType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class PlaylistMapper {

	private static final long DEEZER_DURATION_FACTOR = 1000;

	// Playlist mapping

	Playlist mapPlaylist(PlaylistEntity o) {
		return new Playlist(o.getUid(), o.getTitle(), PlaylistType.fromValue(o.getType()),
				o.getLink(), o.isReadOnly(), mapPlaylistItem(o.getItems()));
	}

	Playlist mapPlaylistDeezer(ru.push.caudioplayer.core.deezer.domain.Playlist o) {
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

	List<Playlist> mapPlaylistDeezer(List<ru.push.caudioplayer.core.deezer.domain.Playlist> list) {
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
		return new PlaylistTrack(MediaSourceType.valueOf(o.getSourceType()),
				o.getArtist(), o.getAlbum(), o.getDate(), o.getTitle(),
				o.getTrackId(), o.getTrackNumber(), o.getLength(), o.getTrackPath()
		);
	}

	PlaylistTrack mapPlaylistItemDeezer(ru.push.caudioplayer.core.deezer.domain.Track o) {
		return new PlaylistTrack(MediaSourceType.DEEZER_MEDIA,
				o.getArtist().getName(), o.getAlbum().getTitle(), null, o.getTitle(),
				String.valueOf(o.getId()), null, o.getDuration() * DEEZER_DURATION_FACTOR, o.getPreview()
		);
	}

	PlaylistItemEntity inverseMapPlaylistItem(PlaylistTrack o) {
		return new PlaylistItemEntity(o.getSourceType().value(),
				o.getArtist(), o.getAlbum(), o.getDate(), o.getTitle(),
				o.getTrackId(), o.getTrackNumber(), o.getLength(), o.getTrackPath());
	}

	List<PlaylistTrack> mapPlaylistItem(List<PlaylistItemEntity> list) {
		return (list != null) ?
				list.stream().map(this::mapPlaylistItem).collect(Collectors.toList()) :
				new ArrayList<>();
	}

	List<PlaylistTrack> mapPlaylistItemDeezer(List<ru.push.caudioplayer.core.deezer.domain.Track> list) {
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
