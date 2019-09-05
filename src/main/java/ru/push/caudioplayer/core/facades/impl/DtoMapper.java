package ru.push.caudioplayer.core.facades.impl;

import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.facades.dto.PlaylistType;
import ru.push.caudioplayer.core.facades.dto.TrackData;
import ru.push.caudioplayer.core.playlist.domain.Playlist;
import ru.push.caudioplayer.core.playlist.domain.PlaylistItem;

import java.util.List;
import java.util.stream.Collectors;

@Component
class DtoMapper {

	PlaylistData mapPlaylistData(Playlist o) {
		return new PlaylistData(o.getUid(), o.getTitle(), o.isReadOnly(), PlaylistType.fromValue(o.getType().value()),
				mapTrackData(o.getItems()));
	}

	List<PlaylistData> mapPlaylistData(List<Playlist> list) {
		return list.stream().map(this::mapPlaylistData).collect(Collectors.toList());
	}

	TrackData mapTrackData(PlaylistItem o) {
		return new TrackData(o.getArtist(), o.getAlbum(), o.getDate(), o.getTitle(), o.getTrackNumber(), o.getLength());
	}

	List<TrackData> mapTrackData(List<PlaylistItem> list) {
		return list.stream().map(this::mapTrackData).collect(Collectors.toList());
	}
}
