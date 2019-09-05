package ru.push.caudioplayer.core.facades.dto;

import java.util.List;
import java.util.Objects;

public class PlaylistData {
	private String uid;
	private String title;
	private boolean readOnly;
	private PlaylistType type;
	private List<TrackData> tracks;

	public PlaylistData(String uid, String title, boolean readOnly, PlaylistType type, List<TrackData> tracks) {
		this.uid = uid;
		this.title = title;
		this.readOnly = readOnly;
		this.type = type;
		this.tracks = tracks;
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

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public PlaylistType getType() {
		return type;
	}

	public void setType(PlaylistType type) {
		this.type = type;
	}

	public List<TrackData> getTracks() {
		return tracks;
	}

	public void setTracks(List<TrackData> tracks) {
		this.tracks = tracks;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PlaylistData that = (PlaylistData) o;
		return Objects.equals(uid, that.uid);
	}

	@Override
	public int hashCode() {

		return Objects.hash(uid);
	}

	@Override
	public String toString() {
		return "PlaylistData{" +
				"uid='" + uid + '\'' +
				", title='" + title + '\'' +
				", readOnly=" + readOnly +
				", type=" + type +
				", tracks=" + tracks +
				'}';
	}
}
