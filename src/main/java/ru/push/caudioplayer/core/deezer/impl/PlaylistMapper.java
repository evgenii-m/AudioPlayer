package ru.push.caudioplayer.core.deezer.impl;

import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.playlist.model.MediaSourceType;
import ru.push.caudioplayer.core.playlist.model.Playlist;
import ru.push.caudioplayer.core.playlist.model.PlaylistTrack;
import ru.push.caudioplayer.core.playlist.model.PlaylistType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("deezerPlaylistMapper")
class PlaylistMapper {

	private static final long DEEZER_DURATION_FACTOR = 1000;

	Playlist mapPlaylist(ru.push.caudioplayer.core.deezer.model.Playlist o) {
		return new Playlist(String.valueOf(o.getId()), o.getTitle(), PlaylistType.DEEZER,
				o.getLink(), o.getIs_loved_track(), mapPlaylistItem(o.getTracks().getData()));
	}

	List<Playlist> mapPlaylist(List<ru.push.caudioplayer.core.deezer.model.Playlist> list) {
		return (list != null) ?
				list.stream().map(this::mapPlaylist).collect(Collectors.toList()) :
				new ArrayList<>();
	}

	private PlaylistTrack mapPlaylistItem(ru.push.caudioplayer.core.deezer.model.Track o) {
		return new PlaylistTrack(
				String.valueOf(o.getId()), MediaSourceType.DEEZER_MEDIA, o.getPreview(), o.getArtist().getName(),
				o.getAlbum().getTitle(), null, o.getTitle(), null, o.getDuration() * DEEZER_DURATION_FACTOR
		);
	}

	PlaylistTrack mapPlaylistItem(Playlist playlist, ru.push.caudioplayer.core.deezer.model.Track track) {
		long length = track.getDuration() * DEEZER_DURATION_FACTOR;
		return new PlaylistTrack(
				String.valueOf(track.getId()), MediaSourceType.DEEZER_MEDIA, track.getPreview(), playlist,
				track.getArtist().getName(), track.getAlbum().getTitle(), null, track.getTitle(), null, length
		);
	}

	List<PlaylistTrack> mapPlaylistItem(List<ru.push.caudioplayer.core.deezer.model.Track> list) {
		return (list != null) ?
				list.stream().map(this::mapPlaylistItem).collect(Collectors.toList()) :
				new ArrayList<>();
	}
}
