package ru.push.caudioplayer.core.facades.domain;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/24/17
 */
public class PlaylistData {
  private static final String DEFAULT_PLAYLIST_NAME = "New playlist";

  private String uid;
  private String name;
  private final PlaylistType playlistType;
  private String link;
  private boolean editable;
  private List<AudioTrackData> tracks = new ArrayList<>();

  public static String getNewPlaylistName() {
    String currentTimeString = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());
    return DEFAULT_PLAYLIST_NAME + " " + currentTimeString;
  }

  public PlaylistData(PlaylistType playlistType) {
    this(getNewPlaylistName(), playlistType);
  }

  public PlaylistData(String name, PlaylistType playlistType) {
    this(name, playlistType, new ArrayList<>());
  }

	public PlaylistData(String name, PlaylistType playlistType, List<AudioTrackData> tracks) {
		this.uid = UUID.randomUUID().toString();
  	this.name = name;
		this.playlistType = playlistType;
		this.tracks = tracks;
		this.editable = true;
	}

  public PlaylistData(String uid, String name, PlaylistType playlistType, String link,
											List<AudioTrackData> tracks, boolean editable) {
    this.uid = uid;
    this.name = name;
    this.playlistType = playlistType;
    this.link = link;
    this.tracks = tracks;
    this.editable = editable;
  }

  public String getUid() {
    return uid;
  }

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

	public PlaylistType getPlaylistType() {
		return playlistType;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public List<AudioTrackData> getTracks() {
    return tracks;
  }

  public void setTracks(List<AudioTrackData> tracks) {
    this.tracks = tracks;
  }

	public boolean getEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public String getExportFileName() {
		String validName = this.name.replaceAll("[^a-zA-Zа-яА-Я0-9\\.\\-]", "_");
  	return this.uid + " " + validName + ".xml";
	}

	public boolean isLocal() {
		return PlaylistType.LOCAL.equals(playlistType);
	}

	public boolean isDeezer() {
		return PlaylistType.DEEZER.equals(playlistType);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PlaylistData that = (PlaylistData) o;
		return Objects.equals(uid, that.uid) &&
				Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uid, name);
	}

	@Override
	public String toString() {
		return "PlaylistData{" +
				"uid='" + uid + '\'' +
				", name='" + name + '\'' +
				", playlistType=" + playlistType +
				", link='" + link + '\'' +
				", editable='" + editable + '\'' +
				", tracks=" + tracks +
				'}';
	}
}
