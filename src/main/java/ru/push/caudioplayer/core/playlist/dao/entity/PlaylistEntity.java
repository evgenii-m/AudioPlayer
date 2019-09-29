package ru.push.caudioplayer.core.playlist.dao.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "PLAYLIST")
public class PlaylistEntity implements Serializable {

	@NotNull
	@Id
	@Column(nullable = false)
	private String uid;

	@Column
	private String title;

	@NotNull
	@Column(nullable = false)
	private String type;

	@Column
	private String link;

	@Column
	private boolean readOnly;

	@OneToMany(mappedBy = "playlist", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private List<PlaylistItemEntity> items;


	public PlaylistEntity() {
	}

	public PlaylistEntity(String uid, String title, String type, String link, boolean readOnly) {
		this.uid = uid;
		this.title = title;
		this.type = type;
		this.link = link;
		this.readOnly = readOnly;
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
