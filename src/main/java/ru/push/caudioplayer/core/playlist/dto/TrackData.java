package ru.push.caudioplayer.core.playlist.dto;

public class TrackData {
	private final String artist;
	private final String album;
	private final String title;

	public TrackData(String artist, String album, String title) {
		this.artist = artist;
		this.album = album;
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public String getAlbum() {
		return album;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return "TrackData{" +
				"artist='" + artist + '\'' +
				", album='" + album + '\'' +
				", title='" + title + '\'' +
				'}';
	}
}
