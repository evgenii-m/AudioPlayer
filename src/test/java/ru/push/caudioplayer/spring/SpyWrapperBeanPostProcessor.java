package ru.push.caudioplayer.spring;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.facades.impl.DefaultAudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.components.impl.DefaultCustomAudioPlayerComponent;
import ru.push.caudioplayer.core.mediaplayer.components.impl.DefaultCustomPlaylistComponent;
import ru.push.caudioplayer.core.mediaplayer.helpers.impl.DefaultMediaInfoDataLoader;
import ru.push.caudioplayer.core.mediaplayer.services.impl.CommonsAppConfigurationService;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 4/3/17
 */
@Component
public class SpyWrapperBeanPostProcessor implements BeanPostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(SpyWrapperBeanPostProcessor.class);
  private static final Collection<Class> wrappedClasses = Arrays.asList(
      CustomMediaPlayerFactory.class,
      DefaultCustomAudioPlayerComponent.class,
      DefaultCustomPlaylistComponent.class,
      CommonsAppConfigurationService.class,
      DefaultMediaInfoDataLoader.class,
      DefaultAudioPlayerFacade.class
  );

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class beanClass = bean.getClass();
    if (wrappedClasses.contains(beanClass)) {
      LOG.debug("Bean wrapped in Mockito.spy [beanName = " + beanName
          + ", beanClass = " + beanClass.toString() + "]");
      return Mockito.spy(bean);
    } else {
      return bean;
    }
  }
}
