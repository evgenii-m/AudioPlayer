package ru.push.caudioplayer.core.lastfm.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement
public class Track implements Serializable {

	@XmlAttribute(name = "nowplaying")
	private Boolean nowPlaying;

	@XmlElement(name = "artist")
	private Artist artist;

	@XmlElement(name = "name")
	private String name;

	@XmlElement(name = "mbid")
	private String mbid;

	@XmlElement(name = "album")
	private Album album;

	@XmlElement(name = "url")
	private String url;

	@XmlElement(name = "date")
	private Date date;

	@XmlElement(name = "streamable")
	private String streamable;


	public Track() {
	}

	public Track(Boolean nowPlaying, Artist artist, String name, String mbid, Album album, String url, Date date, String streamable) {
		this.nowPlaying = nowPlaying;
		this.artist = artist;
		this.name = name;
		this.mbid = mbid;
		this.album = album;
		this.url = url;
		this.date = date;
		this.streamable = streamable;
	}

	public Boolean getNowPlaying() {
		return nowPlaying;
	}

	public void setNowPlaying(Boolean nowPlaying) {
		this.nowPlaying = nowPlaying;
	}

	public Artist getArtist() {
		return artist;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMbid() {
		return mbid;
	}

	public void setMbid(String mbid) {
		this.mbid = mbid;
	}

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Album album) {
		this.album = album;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getStreamable() {
		return streamable;
	}

	public void setStreamable(String streamable) {
		this.streamable = streamable;
	}

	@Override
	public String toString() {
		return "Track{" +
				"nowPlaying=" + nowPlaying +
				", artist=" + artist +
				", name='" + name + '\'' +
				", mbid='" + mbid + '\'' +
				", album=" + album +
				", url='" + url + '\'' +
				", date=" + date +
				", streamable='" + streamable + '\'' +
				'}';
	}

}