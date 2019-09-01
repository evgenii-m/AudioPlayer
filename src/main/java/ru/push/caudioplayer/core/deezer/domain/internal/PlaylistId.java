package ru.push.caudioplayer.core.deezer.domain.internal;

public class PlaylistId {

	private Long id;

	public PlaylistId() {
	}

	public PlaylistId(final Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "PlaylistId{" +
				"id=" + id +
				'}';
	}
}
