package ru.push.caudioplayer.core.config.domain.view;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

	public ObjectFactory() {
	}

	public View createView() {
		return new View();
	}
}
