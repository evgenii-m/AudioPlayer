package ru.push.caudioplayer.core.deezer.model.internal;

public class TrackId {

	private Long id;

	public TrackId() {
	}

	public TrackId(final Long id) {
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
		return "TrackId{" +
				"id=" + id +
				'}';
	}
}
