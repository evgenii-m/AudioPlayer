package ru.push.caudioplayer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.push.caudioplayer.core.mediaplayer.CustomPlayerComponent;
import ru.push.caudioplayer.core.mediaplayer.impl.DefaultCustomPlayerComponent;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 1/23/17
 */
@Configuration
public class ApplicationConfiguration {

  @Bean
  public CustomPlayerComponent getPlayerComponent() {
    return new DefaultCustomPlayerComponent();
  }
}
