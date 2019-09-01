package ru.push.caudioplayer.core.deezer.domain.internal;

public class UserId {

	private Long id;

	public UserId() {
	}

	public UserId(final Long id) {
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
		return "UserId{" +
				"id=" + id +
				'}';
	}
}
