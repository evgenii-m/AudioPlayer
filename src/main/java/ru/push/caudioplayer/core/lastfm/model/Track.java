package ru.push.caudioplayer.core.lastfm.model;

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
	private boolean nowPlaying;

	@XmlElement(name = "artist")
	private ArtistShort artist;

	@XmlElement(name = "name")
	private String name;

	@XmlElement(name = "mbid")
	private String mbid;

	@XmlElement(name = "album")
	private AlbumShort album;

	@XmlElement(name = "url")
	private String url;

	@XmlElement(name = "date")
	private Date date;

	@XmlElement(name = "streamable")
	private String streamable;


	public Track() {
	}

	public Track(boolean nowPlaying, ArtistShort artist, String name, String mbid, AlbumShort album, String url, Date date, String streamable) {
		this.nowPlaying = nowPlaying;
		this.artist = artist;
		this.name = name;
		this.mbid = mbid;
		this.album = album;
		this.url = url;
		this.date = date;
		this.streamable = streamable;
	}

	public boolean getNowPlaying() {
		return nowPlaying;
	}

	public void setNowPlaying(boolean nowPlaying) {
		this.nowPlaying = nowPlaying;
	}

	public ArtistShort getArtist() {
		return artist;
	}

	public void setArtist(ArtistShort artist) {
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

	public AlbumShort getAlbum() {
		return album;
	}

	public void setAlbum(AlbumShort album) {
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