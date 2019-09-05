package ru.push.caudioplayer.core.playlist.impl;

import ru.push.caudioplayer.core.mediaplayer.domain.MediaSourceType;
import ru.push.caudioplayer.core.playlist.dao.model.PlaylistEntity;
import ru.push.caudioplayer.core.playlist.dao.model.PlaylistItemEntity;
import ru.push.caudioplayer.core.playlist.domain.Playlist;
import ru.push.caudioplayer.core.playlist.domain.PlaylistItem;
import ru.push.caudioplayer.core.playlist.domain.PlaylistType;

import java.util.List;
import java.util.stream.Collectors;

class PlaylistConverter {

	private static final long DEEZER_DURATION_FACTOR = 1000;

	// Playlist converters

	public Playlist mapPlaylist(PlaylistEntity o) {
		return new Playlist(o.getUid(), o.getTitle(), PlaylistType.fromValue(o.getType()),
				o.getLink(), o.isReadOnly(), mapPlaylistItem(o.getItems()));
	}

	public Playlist mapPlaylistDeezer(ru.push.caudioplayer.core.deezer.domain.Playlist o) {
		return new Playlist(String.valueOf(o.getId()), o.getTitle(), PlaylistType.DEEZER,
				o.getLink(), o.getIs_loved_track(), mapPlaylistItemDeezer(o.getTracks().getData()));
	}

	public PlaylistEntity inverseMapPlaylist(Playlist o) {
		return new PlaylistEntity(o.getUid(), o.getTitle(), o.getType().value(),
				o.getLink(), o.isReadOnly(), inverseMapPlaylistItem(o.getItems()));
	}

	public List<Playlist> mapPlaylist(List<PlaylistEntity> list) {
		return list.stream().map(this::mapPlaylist).collect(Collectors.toList());
	}

	public List<Playlist> mapPlaylistDeezer(List<ru.push.caudioplayer.core.deezer.domain.Playlist> list) {
		return list.stream().map(this::mapPlaylistDeezer).collect(Collectors.toList());
	}

	public List<PlaylistEntity> inverseMapPlaylist(List<Playlist> list) {
		return list.stream().map(this::inverseMapPlaylist).collect(Collectors.toList());
	}


	// PlaylistItem converters

	public PlaylistItem mapPlaylistItem(PlaylistItemEntity o) {
		return new PlaylistItem(MediaSourceType.valueOf(o.getSourceType()),
				o.getArtist(), o.getAlbum(), o.getDate(), o.getTitle(),
				o.getTrackId(), o.getTrackNumber(), o.getLength(), o.getTrackPath()
		);
	}

	public PlaylistItem mapPlaylistItemDeezer(ru.push.caudioplayer.core.deezer.domain.Track o) {
		return new PlaylistItem(MediaSourceType.DEEZER_MEDIA,
				o.getArtist().getName(), o.getAlbum().getTitle(), null, o.getTitle(),
				String.valueOf(o.getId()), null, o.getDuration() * DEEZER_DURATION_FACTOR, o.getPreview()
		);
	}

	public PlaylistItemEntity inverseMapPlaylistItem(PlaylistItem o) {
		return new PlaylistItemEntity(o.getSourceType().value(),
				o.getArtist(), o.getAlbum(), o.getDate(), o.getTitle(),
				o.getTrackId(), o.getTrackNumber(), o.getLength(), o.getTrackPath());
	}

	public List<PlaylistItem> mapPlaylistItem(List<PlaylistItemEntity> list) {
		return list.stream().map(this::mapPlaylistItem).collect(Collectors.toList());
	}

	public List<PlaylistItem> mapPlaylistItemDeezer(List<ru.push.caudioplayer.core.deezer.domain.Track> list) {
		return list.stream().map(this::mapPlaylistItemDeezer).collect(Collectors.toList());
	}

	public List<PlaylistItemEntity> inverseMapPlaylistItem(List<PlaylistItem> list) {
		return list.stream().map(this::inverseMapPlaylistItem).collect(Collectors.toList());
	}

}
