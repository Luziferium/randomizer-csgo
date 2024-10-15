package de.metaphoriker;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import de.metaphoriker.model.action.Action;
import de.metaphoriker.model.action.handling.ActionRepository;
import de.metaphoriker.model.action.sequence.ActionSequenceExecutorRunnable;
import de.metaphoriker.model.action.sequence.ActionSequenceRepository;
import de.metaphoriker.model.cfg.ConfigLoader;
import de.metaphoriker.model.cfg.keybind.KeyBind;
import de.metaphoriker.model.cfg.keybind.KeyBindRepository;
import de.metaphoriker.model.exception.UncaughtExceptionLogger;
import de.metaphoriker.model.messages.Messages;
import de.metaphoriker.model.stuff.ApplicationContext;
import de.metaphoriker.model.updater.Updater;
import de.metaphoriker.model.watcher.FileSystemWatcher;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ThreadLocalRandom;
import javafx.application.Application;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

  public static boolean testMode = false;
  public static Injector injector;

  @Inject @Getter private ActionSequenceRepository actionSequenceRepository;
  @Inject @Getter private ActionRepository actionRepository;
  @Inject private KeyBindRepository keyBindRepository;
  @Inject private ActionSequenceExecutorRunnable actionSequenceExecutorRunnable;
  @Inject private ApplicationContext applicationContext;
  @Inject private FileSystemWatcher fileSystemWatcher;

  public static void main(String[] args) {
    testMode = hasTestModeFlag(args);
    if (testMode) log.debug("Anwendung wird im Testmodus gestartet");

    log.debug("Starte JavaFX Anwendung...");
    Application.launch(RandomizerStarter.class, args);
  }

  private static boolean hasTestModeFlag(String[] args) {
    for (String arg : args) {
      if (arg.startsWith("-testMode=")) {
        return Boolean.parseBoolean(arg.substring(arg.indexOf("=") + 1));
      }
    }
    return false;
  }

  public void startApplication() throws IOException, URISyntaxException {
    log.debug("Initialisiere Applikation...");
    if (!testMode) installAndRunUpdaterIfNeeded();
    loadKeyBinds();
    registerActions();
    cacheActions();
    startExecutors();
    setupListeners();
    Messages.cache();
    setupGlobalExceptionHandler();
    registerNativeKeyHook();
  }

  private void loadKeyBinds() {
    log.debug("Lade KeyBinds...");
    ConfigLoader.loadKeyBinds(keyBindRepository);
  }

  private void setupListeners() {
    log.debug("Richte Listener ein...");
    fileSystemWatcher.addFileChangeListener(_ -> cacheActions());
  }

  private void setupGlobalExceptionHandler() {
    log.debug("Richte Global Exception Handler ein...");
    Thread.currentThread()
        .setUncaughtExceptionHandler(UncaughtExceptionLogger.DEFAULT_UNCAUGHT_EXCEPTION_LOGGER);
  }

  private void cacheActions() {
    log.debug("Cache Actions...");
    actionSequenceRepository.updateActionSequencesCache();
  }

  private void startExecutors() {
    log.debug("Starte Executor...");
    startThread(new Thread(actionSequenceExecutorRunnable));
    startThread(new Thread(new FileSystemWatcher()));
  }

  private void startThread(Thread thread) {
    log.debug("Starte Thread: {}", thread.getName());
    thread.setUncaughtExceptionHandler(UncaughtExceptionLogger.DEFAULT_UNCAUGHT_EXCEPTION_LOGGER);
    thread.setDaemon(true);
    thread.start();
  }

  private void installAndRunUpdaterIfNeeded() throws IOException, URISyntaxException {
    log.debug("Richte Appdata Verzeichnis ein...");
    File updater = installUpdater();
    startUpdaterIfNecessary(updater.getAbsolutePath());
  }

  private File installUpdater() {
    log.debug("Installiere Updater...");
    File updaterJar = new File(ApplicationContext.getAppdataFolder(), "randomizer-updater.jar");
    try {
      updaterJar.createNewFile();
    } catch (IOException e) {
      log.error("Fehler beim Erstellen der Updater Datei", e);
      throw new RuntimeException(e);
    }

    if (Updater.isUpdateAvailable(updaterJar, Updater.UPDATER_VERSION_URL)) {
      Updater.update(updaterJar, Updater.UPDATER_DOWNLOAD_URL);
    }

    return updaterJar;
  }

  private void startUpdaterIfNecessary(String path) throws IOException, URISyntaxException {
    log.debug("Starte Updater falls notwendig...");
    File jarPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    if (Updater.isUpdateAvailable(Updater.getCurrentVersion(), Updater.RANDOMIZER_VERSION_URL)) {
      log.debug("Updater gestartet");
      Runtime.getRuntime()
          .exec("java -jar " + path + " -randomizerLocation=" + jarPath.getAbsolutePath());
      System.exit(0);
    }
  }

  private void registerActions() {
    log.debug("Registriere Aktionen...");
    actionRepository.register(
        new Action(new KeyBind(KeyBind.EMPTY_KEYBIND.getKey(), "Pause")) {
          @Override
          public void execute() {
            if (getInterval().isEmpty()) return;

            int min = getInterval().getMin();
            int max = getInterval().getMax();
            try {
              Thread.sleep(ThreadLocalRandom.current().nextInt(min, max));
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
        });
    actionRepository.register(
        new Action(new KeyBind(KeyBind.EMPTY_KEYBIND.getKey(), "Mouse Move")) {
          @Override
          public void execute() {
            Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();

            int middleX = dimension.width / 2;
            int middleY = dimension.height / 2;

            int x = ThreadLocalRandom.current().nextInt(middleX - 5, middleX + 5);
            int y = ThreadLocalRandom.current().nextInt(middleY - 5, middleY + 5);

            robot.mouseMove(x, y);
          }
        });
    actionRepository.register(new Action(new KeyBind("ESCAPE", "Escape")));
    keyBindRepository
        .getKeyBinds()
        .forEach(
            keyBind -> {
              Action action = new Action(keyBind);
              actionRepository.register(action);
            });
    actionRepository.loadStatesIfExist();
  }

  private void registerNativeKeyHook() {
    try {
      log.debug("Registriere Native Key Hook...");
      GlobalScreen.registerNativeHook();
    } catch (NativeHookException e) {
      log.error("Fehler bei der Registrierung des Native Hooks", e);
    }
  }
}
