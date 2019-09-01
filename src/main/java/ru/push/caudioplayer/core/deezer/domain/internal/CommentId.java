package ru.push.caudioplayer.core.deezer.domain.internal;

public class CommentId {

	private Long id;


	public CommentId() {
	}

	public CommentId(final Long id) {
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
		return "CommentId{" +
				"id=" + id +
				'}';
	}
}
