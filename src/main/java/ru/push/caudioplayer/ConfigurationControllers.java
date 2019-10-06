package ru.push.caudioplayer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.push.caudioplayer.controller.ConfirmActionPopupController;
import ru.push.caudioplayer.controller.DeezerPanelController;
import ru.push.caudioplayer.controller.LastFmPanelController;
import ru.push.caudioplayer.controller.MainController;
import ru.push.caudioplayer.controller.AudioPlayerController;
import ru.push.caudioplayer.controller.NotificationsPanelController;
import ru.push.caudioplayer.controller.PlaylistController;
import ru.push.caudioplayer.controller.RadioPanelController;
import ru.push.caudioplayer.controller.RenamePopupController;
import ru.push.caudioplayer.controller.TextInputActionPopupController;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 22.11.16
 */
@Configuration
public class ConfigurationControllers {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationControllers.class);

  @Bean(name = "mainView")
  public View getMainView() {
    return loadView("view/main.fxml");
  }

  @Bean
  public MainController getMainController() {
    return (MainController) getMainView().getController();
  }

  @Bean(name = "audioPlayerView")
  public View audioPlayerView() {
    return loadView("view/audioplayer-component.fxml");
  }

  @Bean
  public AudioPlayerController getAudioPlayerController() {
    return (AudioPlayerController) audioPlayerView().getController();
  }

  @Bean(name = "playlistView")
  public View playlistView() {
    return loadView("view/playlist-component.fxml");
  }

  @Bean
  public PlaylistController getPlaylistController() {
    return (PlaylistController) playlistView().getController();
  }

  @Bean(name = "renamePopupView")
  public View renamePopupView() {
    return loadView("view/rename-popup.fxml");
  }

  @Bean
  public RenamePopupController getRenamePopupController() {
    return (RenamePopupController) renamePopupView().getController();
  }

  @Bean(name ="lastfmPanelView")
	public View getLastFmPanelView() {
  	return loadView("view/lastfm-panel-component.fxml");
	}

	@Bean
	public LastFmPanelController getLastFmPanelController() {
  	return (LastFmPanelController) getLastFmPanelView().getController();
	}

	@Bean(name = "confirmActionPopupView")
	public View getConfirmActionPopupView() {
  	return loadView("view/confirm-action-popup.fxml");
	}

	@Bean
	public ConfirmActionPopupController getConfirmActionPopupController() {
  	return (ConfirmActionPopupController) getConfirmActionPopupView().getController();
	}

	@Bean(name = "textInputActionPopupView")
	public View getTextInputActionPopupView() {
		return loadView("view/text-input-action-popup.fxml");
	}

	@Bean
	public TextInputActionPopupController getTextInputActionPopupController() {
		return (TextInputActionPopupController) getTextInputActionPopupView().getController();
	}

	@Bean(name = "radioPanelComponentView")
	public View getRadioPanelComponentView() {
		return loadView("view/radio-panel-component.fxml");
	}

	@Bean
	public RadioPanelController getRadioPanelController() {
		return (RadioPanelController) getRadioPanelComponentView().getController();
	}

	@Bean(name = "deezerPanelComponentView")
	public View getDeezerPanelComponentView() {
		return loadView("view/deezer-panel-component.fxml");
	}

	@Bean
	public DeezerPanelController getDeezerPanelController() {
		return (DeezerPanelController) getDeezerPanelComponentView().getController();
	}

	@Bean(name = "notificationsPanelComponentView")
	public View getNotificationsPanelComponentView() {
		return loadView("view/notifications-panel-component.fxml");
	}

	@Bean
	public NotificationsPanelController getNotificationsPanelController() {
		return (NotificationsPanelController) getNotificationsPanelComponentView().getController();
	}

  protected View loadView(String url) {
    try (InputStream fxmlStream = getClass().getClassLoader().getResourceAsStream(url)) {
      FXMLLoader loader = new FXMLLoader();
      loader.load(fxmlStream);
      return new View(loader.getRoot(), loader.getController());
    } catch (IOException e) {
      LOG.error("Load view fail", e);
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
