package ru.push.caudioplayer.core.facades.impl;

import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.facades.dto.LastFmTrackData;
import ru.push.caudioplayer.core.facades.dto.LastFmTrackInfoData;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.facades.dto.PlaylistType;
import ru.push.caudioplayer.core.facades.dto.TrackData;
import ru.push.caudioplayer.core.lastfm.model.Album;
import ru.push.caudioplayer.core.lastfm.model.Artist;
import ru.push.caudioplayer.core.lastfm.model.Image;
import ru.push.caudioplayer.core.lastfm.model.Tag;
import ru.push.caudioplayer.core.lastfm.model.Track;
import ru.push.caudioplayer.core.lastfm.model.TrackInfo;
import ru.push.caudioplayer.core.lastfm.model.TrackInfoWiki;
import ru.push.caudioplayer.core.playlist.model.Playlist;
import ru.push.caudioplayer.core.playlist.model.PlaylistTrack;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
		return new LastFmTrackData(o.getMbid(), o.getArtist().getName(), o.getAlbum().getName(),
				o.getName(), o.getNowPlaying(), scrobbleDate);
	}

	List<LastFmTrackData> mapLastFmTrackData(List<Track> list) {
		return (list != null) ?
				list.stream().map(this::mapLastFmTrackData).collect(Collectors.toList()) :
				new ArrayList<>();
	}

	LastFmTrackInfoData mapLastFmTrackInfoData(TrackInfo o) {
		String albumImageUrl = ((o.getAlbum() != null) && (o.getAlbum().getImages() != null)) ?
				o.getAlbum().getImages().stream().findFirst().map(Image::getUrl).orElse(null) :
				null;
		Map<String, String> tagsUrlMap = ((o.getTopTags() != null) && (o.getTopTags().getTags() != null)) ?
				o.getTopTags().getTags().stream().collect(Collectors.toMap(Tag::getName, Tag::getUrl)) :
				null;
		return new LastFmTrackInfoData(o.getMbid(), o.getName(), o.getUrl(), o.getDuration(),
				o.getListeners(), o.getPlaycount(), o.getUserplaycount(), o.getUserloved(),
				o.getArtist().getMbid(), o.getArtist().getName(), o.getArtist().getUrl(),
				Optional.ofNullable(o.getAlbum()).map(Album::getMbid).orElse(null),
				Optional.ofNullable(o.getAlbum()).map(Album::getTitle).orElse(null),
				Optional.ofNullable(o.getAlbum()).map(Album::getUrl).orElse(null),
				albumImageUrl, tagsUrlMap,
				Optional.ofNullable(o.getWiki()).map(TrackInfoWiki::getContent).orElse(null)
		);
	}
}
