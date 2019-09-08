package ru.push.caudioplayer.core.playlist.dao.entity;


import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

	public ObjectFactory() {
	}

	public PlaylistEntity createPlaylistEntity() {
		return new PlaylistEntity();
	}

}
