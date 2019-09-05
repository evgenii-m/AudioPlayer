package ru.push.caudioplayer.core.playlist.dao.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "playlist")
public class PlaylistEntity implements Serializable {

	@XmlAttribute
	private String uid;

	@XmlAttribute
	private String title;

	@XmlAttribute
	private String type;

	@XmlAttribute
	private String link;

	@XmlAttribute
	private boolean readOnly;

	@XmlElement(name = "item")
	private List<PlaylistItemEntity> items;


	public PlaylistEntity() {
	}

	public PlaylistEntity(String uid, String title, String type, String link, boolean readOnly, List<PlaylistItemEntity> items) {
		this.uid = uid;
		this.title = title;
		this.type = type;
		this.link = link;
		this.readOnly = readOnly;
		this.items = items;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
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

	public List<PlaylistItemEntity> getItems() {
		return items;
	}

	public void setItems(List<PlaylistItemEntity> items) {
		this.items = items;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PlaylistEntity playlist = (PlaylistEntity) o;
		return Objects.equals(uid, playlist.uid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uid);
	}

	@Override
	public String toString() {
		return "PlaylistEntity{" +
				"uid='" + uid + '\'' +
				", title='" + title + '\'' +
				", type=" + type +
				", link='" + link + '\'' +
				", readOnly=" + readOnly +
				", items=" + items +
				'}';
	}
}
