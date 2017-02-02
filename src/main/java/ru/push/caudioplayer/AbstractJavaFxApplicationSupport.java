package ru.push.caudioplayer;

import javafx.application.Platform;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 22.11.16
 */
public abstract class AbstractJavaFxApplicationSupport extends Application {

  private static String[] savedArgs;

  protected ConfigurableApplicationContext context;

  @Override
  public void init() throws Exception {
    context = SpringApplication.run(getClass(), savedArgs);
    context.getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    context.close();
    Platform.exit();
    System.exit(0);
  }

  protected static void launchApp(Class<? extends AbstractJavaFxApplicationSupport> appClass, String[] args) {
    AbstractJavaFxApplicationSupport.savedArgs = args;
    Application.launch(appClass, args);
  }
}