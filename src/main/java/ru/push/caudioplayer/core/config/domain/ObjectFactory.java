package ru.push.caudioplayer.core.config.domain;


import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

	public ObjectFactory() {
	}

	public Configuration createConfiguration() {
		return new Configuration();
	}

	public PlaylistConfig createPlaylist() {
		return new PlaylistConfig();
	}

}
