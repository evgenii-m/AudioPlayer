package ru.push.caudioplayer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 22.11.16
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
@ImportResource("classpath:spring/application-context.xml")
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
  }

  protected static void launchApp(Class<? extends AbstractJavaFxApplicationSupport> appClass, String[] args) {
    AbstractJavaFxApplicationSupport.savedArgs = args;
    Application.launch(appClass, args);
  }
}