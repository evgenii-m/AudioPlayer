package ru.push.caudioplayer.core.deezer.domain.internal;

public class ArtistId {

	private Long id;

	public ArtistId() {
	}

	public ArtistId(final Long id) {
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
		return "ArtistId{" +
				"id=" + id +
				'}';
	}
}
