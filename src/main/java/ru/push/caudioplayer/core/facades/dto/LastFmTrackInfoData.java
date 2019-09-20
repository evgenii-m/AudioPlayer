package ru.push.caudioplayer.core.facades.dto;

import java.util.Map;

public class LastFmTrackInfoData {
	private String trackMbid;
	private String trackName;
	private String trackUrl;
	private Long duration;
	private Long listenersCount;
	private Long playCount;
	private String artistMbid;
	private String artistName;
	private String artistUrl;
	private String albumMbid;
	private String albumName;
	private String albumUrl;
	private String albumImageUrl;
	private Map<String, String> tagsUrlMap;
	private String description;

	public LastFmTrackInfoData(String trackMbid, String trackName, String trackUrl,
														 Long duration, Long listenersCount, Long playCount,
														 String artistMbid, String artistName, String artistUrl,
														 String albumMbid, String albumName, String albumUrl,
														 String albumImageUrl, Map<String, String> tagsUrlMap,
														 String description) {
		this.trackMbid = trackMbid;
		this.trackName = trackName;
		this.trackUrl = trackUrl;
		this.duration = duration;
		this.listenersCount = listenersCount;
		this.playCount = playCount;
		this.artistMbid = artistMbid;
		this.artistName = artistName;
		this.artistUrl = artistUrl;
		this.albumMbid = albumMbid;
		this.albumName = albumName;
		this.albumUrl = albumUrl;
		this.albumImageUrl = albumImageUrl;
		this.tagsUrlMap = tagsUrlMap;
		this.description = description;
	}

	public String getTrackMbid() {
		return trackMbid;
	}

	public String getTrackName() {
		return trackName;
	}

	public String getTrackUrl() {
		return trackUrl;
	}

	public Long getDuration() {
		return duration;
	}

	public Long getListenersCount() {
		return listenersCount;
	}

	public Long getPlayCount() {
		return playCount;
	}

	public String getArtistMbid() {
		return artistMbid;
	}

	public String getArtistName() {
		return artistName;
	}

	public String getArtistUrl() {
		return artistUrl;
	}

	public String getAlbumMbid() {
		return albumMbid;
	}

	public String getAlbumName() {
		return albumName;
	}

	public String getAlbumUrl() {
		return albumUrl;
	}

	public String getAlbumImageUrl() {
		return albumImageUrl;
	}

	public Map<String, String> getTagsUrlMap() {
		return tagsUrlMap;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "LastFmTrackInfoData{" +
				"trackMbid='" + trackMbid + '\'' +
				", trackName='" + trackName + '\'' +
				", trackUrl='" + trackUrl + '\'' +
				", duration=" + duration +
				", listenersCount=" + listenersCount +
				", playCount=" + playCount +
				", artistMbid='" + artistMbid + '\'' +
				", artistName='" + artistName + '\'' +
				", artistUrl='" + artistUrl + '\'' +
				", albumMbid='" + albumMbid + '\'' +
				", albumName='" + albumName + '\'' +
				", albumUrl='" + albumUrl + '\'' +
				", albumImageUrl='" + albumImageUrl + '\'' +
				", tagsUrlMap=" + tagsUrlMap +
				", description='" + description + '\'' +
				'}';
	}
}
