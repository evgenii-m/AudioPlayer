package ru.push.caudioplayer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.push.caudioplayer.controller.MainController;
import ru.push.caudioplayer.controller.MediaPlayerController;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 22.11.16
 */
@Configuration
public class ConfigurationControllers {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationControllers.class);

  @Bean(name = "mainView")
  public View getMainView() {
    return loadView("view/main.fxml");
  }

  @Bean
  public MainController getMainController() {
    return (MainController) getMainView().getController();
  }

  @Bean(name = "mediaPlayerView")
  public View getMediaPlayerView() {
    return loadView("view/mediaplayer.fxml");
  }

  @Bean
  public MediaPlayerController getMediaPlayerController() {
    return (MediaPlayerController) getMediaPlayerView().getController();
  }

  protected View loadView(String url) {
    try (InputStream fxmlStream = getClass().getClassLoader().getResourceAsStream(url)) {
      FXMLLoader loader = new FXMLLoader();
      loader.load(fxmlStream);
      return new View(loader.getRoot(), loader.getController());
    } catch (IOException e) {
      LOGGER.error("Load view fail", e);
      throw new RuntimeException(e);
    }
  }

  public class View {
    private Parent view;
    private Object controller;

    public View(Parent view, Object controller) {
      this.view = view;
      this.controller = controller;
    }

    public Parent getView() {
      return view;
    }

    public void setView(Parent view) {
      this.view = view;
    }

    public Object getController() {
      return controller;
    }

    public void setController(Object controller) {
      this.controller = controller;
    }
  }

}
