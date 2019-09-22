package ru.push.caudioplayer.core.facades.dto;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class LastFmTrackInfoData {
	@NotNull
	private String trackMbid;
	@NotNull
	private String trackName;
	@NotNull
	private String trackUrl;
	@NotNull
	private Long duration;
	@NotNull
	private Long listenersCount;
	@NotNull
	private Long playCount;
	private Long userPlayCount;
	@NotNull
	private Boolean lovedTrack;
	@NotNull
	private String artistMbid;
	@NotNull
	private String artistName;
	@NotNull
	private String artistUrl;
	private String albumMbid;
	private String albumName;
	private String albumUrl;
	private Map<ImageSize, String> imagesUrlMap;
	private Map<String, String> tagsUrlMap;
	private String description;

	public LastFmTrackInfoData(String trackMbid, String trackName, String trackUrl, Long duration,
														 Long listenersCount, Long playCount, Long userPlayCount, Boolean lovedTrack,
														 String artistMbid, String artistName, String artistUrl,
														 String albumMbid, String albumName, String albumUrl,
														 Map<ImageSize, String> imagesUrlMap, Map<String, String> tagsUrlMap,
														 String description) {
		this.trackMbid = trackMbid;
		this.trackName = trackName;
		this.trackUrl = trackUrl;
		this.duration = duration;
		this.listenersCount = listenersCount;
		this.playCount = playCount;
		this.userPlayCount = userPlayCount;
		this.lovedTrack = lovedTrack;
		this.artistMbid = artistMbid;
		this.artistName = artistName;
		this.artistUrl = artistUrl;
		this.albumMbid = albumMbid;
		this.albumName = albumName;
		this.albumUrl = albumUrl;
		this.imagesUrlMap = imagesUrlMap;
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

	public Long getUserPlayCount() {
		return userPlayCount;
	}

	public Boolean isLovedTrack() {
		return lovedTrack;
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

	public Map<ImageSize, String> getImagesUrlMap() {
		return imagesUrlMap;
	}

	public String getSmallImageUrl() {
		return imagesUrlMap.get(ImageSize.SMALL);
	}

	public String getMediumImageUrl() {
		return imagesUrlMap.get(ImageSize.MEDIUM);
	}

	public String getLargeImageUrl() {
		return imagesUrlMap.get(ImageSize.LARGE);
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
				", userPlayCount=" + userPlayCount +
				", lovedTrack=" + lovedTrack +
				", artistMbid='" + artistMbid + '\'' +
				", artistName='" + artistName + '\'' +
				", artistUrl='" + artistUrl + '\'' +
				", albumMbid='" + albumMbid + '\'' +
				", albumName='" + albumName + '\'' +
				", albumUrl='" + albumUrl + '\'' +
				", imagesUrlMap=" + imagesUrlMap +
				", tagsUrlMap=" + tagsUrlMap +
				", description='" + description + '\'' +
				'}';
	}
}
