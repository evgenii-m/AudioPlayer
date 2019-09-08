package ru.push.caudioplayer.core.config.dto;

public class PlaylistItemData {
	private String playlistUid;
	private long position;

	public PlaylistItemData(String playlistUid, long position) {
		this.playlistUid = playlistUid;
		this.position = position;
	}

	public String getPlaylistUid() {
		return playlistUid;
	}

	public void setPlaylistUid(String playlistUid) {
		this.playlistUid = playlistUid;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	@Override
	public String toString() {
		return "PlaylistItemData{" +
				"playlistUid='" + playlistUid + '\'' +
				", position=" + position +
				'}';
	}
}
