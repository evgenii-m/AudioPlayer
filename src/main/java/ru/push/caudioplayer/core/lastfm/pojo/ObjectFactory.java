package ru.push.caudioplayer.core.lastfm.pojo;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

	public ObjectFactory() {
	}

	public LastFmResponse createLastFmResponse() {
		return new LastFmResponse();
	}
}
