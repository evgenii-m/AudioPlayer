package ru.push.caudioplayer;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;

import java.net.URI;
import java.nio.file.Paths;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 22.11.16
 */
@Lazy
@SpringBootApplication
public class AppMain extends AbstractJavaFxApplicationSupport {

  @Value("${ui.title:JavaFX приложение}")//
  private String windowTitle;

  @Qualifier("mainView")
  @Autowired
  private ConfigurationControllers.View view;

  @Override
  public void start(Stage stage) throws Exception {
    final URI resource = Paths.get("target/1. Just One Of Those Things.mp3").toUri();
    final Media media = new Media(resource.toString());
    final MediaPlayer mediaPlayer = new MediaPlayer(media);
    mediaPlayer.play();

    stage.setTitle("Audio Player 1");
    stage.setWidth(200);
    stage.setHeight(200);
    stage.show();
  }

  public static void main(String[] args) {
    launchApp(AppMain.class, args);
  }
}
