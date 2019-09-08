package ru.push.caudioplayer.core.config.model;

import ru.push.caudioplayer.core.config.model.view.View;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement
public class Configuration implements Serializable {

	@XmlElement(name = "lastfm")
	private LastfmSessionData lastfmSessionData;

	@XmlElement(name = "deezer")
	private DeezerSessionData deezerSessionData;

	@NotNull
	@XmlElement
	private Playlists playlists;

	@NotNull
	@XmlElement
	private View view;


	public Configuration() {
	}

	public Configuration(LastfmSessionData lastfmSessionData, DeezerSessionData deezerSessionData, Playlists playlists, View view) {
		this.lastfmSessionData = lastfmSessionData;
		this.deezerSessionData = deezerSessionData;
		this.playlists = playlists;
		this.view = view;
	}

	public LastfmSessionData getLastfmSessionData() {
		return lastfmSessionData;
	}

	public void setLastfmSessionData(LastfmSessionData lastfmSessionData) {
		this.lastfmSessionData = lastfmSessionData;
	}

	public DeezerSessionData getDeezerSessionData() {
		return deezerSessionData;
	}

	public void setDeezerSessionData(DeezerSessionData deezerSessionData) {
		this.deezerSessionData = deezerSessionData;
	}

	public Playlists getPlaylists() {
		return playlists;
	}

	public void setPlaylists(Playlists playlists) {
		this.playlists = playlists;
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	@Override
	public String toString() {
		return "Configuration{" +
				"lastfmSessionData=" + lastfmSessionData +
				", deezerSessionData=" + deezerSessionData +
				", playlists=" + playlists +
				", view=" + view +
				'}';
	}
}
