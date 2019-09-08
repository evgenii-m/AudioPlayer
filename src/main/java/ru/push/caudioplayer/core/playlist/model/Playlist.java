package ru.push.caudioplayer.core.playlist.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Playlist {

	private String uid;
	private String title;
	private PlaylistType type;
	private String link;
	private boolean readOnly;
	private List<PlaylistTrack> items;
	private Long position;

//	private Date createDate;
//	private Date lastUpdateDate;

	public Playlist() {
	}

	public Playlist(String uid, String title, PlaylistType type, String link) {
		this.uid = uid;
		this.title = title;
		this.type = type;
		this.link = link;
		this.readOnly = false;
		this.items = new ArrayList<>();
	}

	public Playlist(String uid, String title, PlaylistType type, String link, boolean readOnly, List<PlaylistTrack> items) {
		this.uid = uid;
		this.title = title;
		this.type = type;
		this.link = link;
		this.readOnly = readOnly;
		this.items = items;
		this.items.forEach(o -> o.setPlaylist(this));
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public PlaylistType getType() {
		return type;
	}

	public boolean isLocal() {
		return PlaylistType.LOCAL.equals(type);
	}

	public boolean isDeezer() {
		return PlaylistType.DEEZER.equals(type);
	}

	public void setType(PlaylistType type) {
		this.type = type;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public List<PlaylistTrack> getItems() {
		return items;
	}

	public void setItems(List<PlaylistTrack> items) {
		this.items = items;
		this.items.forEach(o -> o.setPlaylist(this));
	}

	public Long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public String getExportFileName() {
		String validName = title.replaceAll("[^a-zA-Zа-яА-Я0-9\\.\\-]", "_");
		return uid + " " + validName + ".xml";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Playlist playlist = (Playlist) o;
		return Objects.equals(uid, playlist.uid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uid);
	}

	@Override
	public String toString() {
		return "Playlist{" +
				"uid='" + uid + '\'' +
				", title='" + title + '\'' +
				", type=" + type +
				", link='" + link + '\'' +
				", readOnly=" + readOnly +
				", items=" + items +
				'}';
	}
}
