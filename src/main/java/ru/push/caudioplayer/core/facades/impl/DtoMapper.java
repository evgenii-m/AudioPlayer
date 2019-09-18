package ru.push.caudioplayer.core.facades.impl;

import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.facades.dto.LastFmTrackData;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.facades.dto.PlaylistType;
import ru.push.caudioplayer.core.facades.dto.TrackData;
import ru.push.caudioplayer.core.lastfm.model.Track;
import ru.push.caudioplayer.core.playlist.model.Playlist;
import ru.push.caudioplayer.core.playlist.model.PlaylistTrack;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
class DtoMapper {

	PlaylistData mapPlaylistData(Playlist o) {
		return new PlaylistData(o.getUid(), o.getTitle(), o.isReadOnly(), PlaylistType.fromValue(o.getType().value()),
				mapTrackData(o.getItems()));
	}

	List<PlaylistData> mapPlaylistData(List<Playlist> list) {
		return (list != null) ?
				list.stream().map(this::mapPlaylistData).collect(Collectors.toList()) :
				new ArrayList<>();
	}

	TrackData mapTrackData(PlaylistTrack o) {
		return new TrackData(o.getUid(), o.getPlaylist().getUid(), o.getArtist(), o.getAlbum(),
				o.getDate(), o.getTitle(), o.getTrackNumber(), o.getLength(), o.isNowPlaying(),
				o.getPlaylist().getTitle());
	}

	List<TrackData> mapTrackData(List<PlaylistTrack> list) {
		return (list != null) ?
				list.stream().map(this::mapTrackData).collect(Collectors.toList()) :
				new ArrayList<>();
	}

	LastFmTrackData mapLastFmTrackData(Track o) {
		Date scrobbleDate = Optional.ofNullable(o.getDate())
				.map(d -> d.getUts()).map(uts -> new Date(uts * 1000)).orElse(null);
		return new LastFmTrackData(o.getArtist().getName(), o.getAlbum().getName(),
				o.getName(), o.getNowPlaying(), scrobbleDate);
	}

	List<LastFmTrackData> mapLastFmTrackData(List<Track> list) {
		return (list != null) ?
				list.stream().map(this::mapLastFmTrackData).collect(Collectors.toList()) :
				new ArrayList<>();
	}
}
