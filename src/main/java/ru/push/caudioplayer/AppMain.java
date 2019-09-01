package ru.push.caudioplayer;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.annotation.Resource;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 22.11.16
 */
@Lazy
@SpringBootApplication
public class AppMain extends AbstractJavaFxApplicationSupport {

  private static final double DEFAULT_WIDTH  = 1000;
  private static final double DEFAULT_HEIGHT = 600;

  @Value("${ui.title:JavaFX приложение}")
  private String windowTitle;

  @Resource(name = "mainView")
  private ConfigurationControllers.View mainView;
  @Autowired
  private AudioPlayerFacade audioPlayerFacade;

  @Override
  public void start(Stage stage) throws Exception {
    new NativeDiscovery().discover();     // discover libvlc native libraries

    stage.setTitle(windowTitle);
    stage.setScene(new Scene(mainView.getView(), DEFAULT_WIDTH, DEFAULT_HEIGHT));
    stage.centerOnScreen();
    stage.setResizable(false);
    stage.show();
  }

  @Override
  public void stop() throws Exception {
    audioPlayerFacade.stopApplication();
    super.stop();
  }

  public void openWebPage(String pageUrl) {
		getHostServices().showDocument(pageUrl);
	}

	public static void main(String[] args) {

  	// todo: make read from configuration file
  	System.setProperty("jna.library.path", "C:/Program Files/VideoLAN/VLC");
  	System.setProperty("VLC_PLUGIN_PATH", "C:/Program Files/VideoLAN/VLC/plugins");

    launchApp(AppMain.class, args);
  }
}
