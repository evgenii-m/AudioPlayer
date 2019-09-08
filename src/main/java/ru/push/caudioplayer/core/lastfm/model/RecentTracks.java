package ru.push.caudioplayer.core.lastfm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement
public class RecentTracks implements Serializable {

	@XmlAttribute(name = "user")
	private String user;

	@XmlAttribute(name = "page")
	private long page;

	@XmlAttribute(name = "perPage")
	private long perPage;

	@XmlAttribute(name = "totalPages")
	private long totalPages;

	@XmlElement(name = "track")
	private List<Track> tracks;


	public RecentTracks() {
	}

	public RecentTracks(String user, long page, long perPage, long totalPages, List<Track> tracks) {
		this.user = user;
		this.page = page;
		this.perPage = perPage;
		this.totalPages = totalPages;
		this.tracks = tracks;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public long getPage() {
		return page;
	}

	public void setPage(long page) {
		this.page = page;
	}

	public long getPerPage() {
		return perPage;
	}

	public void setPerPage(long perPage) {
		this.perPage = perPage;
	}

	public long getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(long totalPages) {
		this.totalPages = totalPages;
	}

	public List<Track> getTracks() {
		return tracks;
	}

	public void setTracks(List<Track> tracks) {
		this.tracks = tracks;
	}

	@Override
	public String toString() {
		return "RecentTracks{" +
				"user='" + user + '\'' +
				", page=" + page +
				", perPage=" + perPage +
				", totalPages=" + totalPages +
				", tracks=" + tracks +
				'}';
	}
}
