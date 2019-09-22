package ru.push.caudioplayer.core.lastfm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class TrackInfo implements Serializable {

	@XmlElement
	private String mbid;

	@XmlElement
	private String name;

	@XmlElement
	private String url;

	@XmlElement
	private Long duration;

	@XmlElement
	private Long listeners;

	@XmlElement
	private Long playcount;

	@XmlElement
	private Long userplaycount;

	@XmlElement
	private Boolean userloved;

	@XmlElement
	private Artist artist;

	@XmlElement
	private Album album;

	@XmlElement(name = "toptags")
	private TopTags topTags;

	@XmlElement
	private TrackInfoWiki wiki;


	public TrackInfo() {
	}

	public String getMbid() {
		return mbid;
	}

	public void setMbid(String mbid) {
		this.mbid = mbid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Long getListeners() {
		return listeners;
	}

	public void setListeners(Long listeners) {
		this.listeners = listeners;
	}

	public Long getPlaycount() {
		return playcount;
	}

	public void setPlaycount(Long playcount) {
		this.playcount = playcount;
	}

	public Long getUserplaycount() {
		return userplaycount;
	}

	public void setUserplaycount(Long userplaycount) {
		this.userplaycount = userplaycount;
	}

	public Boolean getUserloved() {
		return userloved;
	}

	public void setUserloved(Boolean userloved) {
		this.userloved = userloved;
	}

	public Artist getArtist() {
		return artist;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Album album) {
		this.album = album;
	}

	public TopTags getTopTags() {
		return topTags;
	}

	public void setTopTags(TopTags topTags) {
		this.topTags = topTags;
	}

	public TrackInfoWiki getWiki() {
		return wiki;
	}

	public void setWiki(TrackInfoWiki wiki) {
		this.wiki = wiki;
	}

	@Override
	public String toString() {
		return "TrackInfo{" +
				"mbid='" + mbid + '\'' +
				", name='" + name + '\'' +
				", url='" + url + '\'' +
				", duration=" + duration +
				", listeners=" + listeners +
				", playcount=" + playcount +
				", userplaycount=" + userplaycount +
				", userloved=" + userloved +
				", artist=" + artist +
				", album=" + album +
				", topTags=" + topTags +
				", wiki=" + wiki +
				'}';
	}
}
