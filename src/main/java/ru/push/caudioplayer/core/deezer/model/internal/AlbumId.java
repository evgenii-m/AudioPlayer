package ru.push.caudioplayer.core.deezer.model.internal;

public class AlbumId {

	private Long id;

	public AlbumId() {
	}

	public AlbumId(final Long id) {
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
		return "AlbumId{" +
				"id=" + id +
				'}';
	}
}
