package dev.luzifer;

import dev.luzifer.backend.event.EventExecutor;
import dev.luzifer.backend.event.EventRepository;
import dev.luzifer.backend.event.cluster.EventClusterRepository;
import dev.luzifer.backend.event.events.CrouchEvent;
import dev.luzifer.backend.event.events.DropWeaponEvent;
import dev.luzifer.backend.event.events.EmptyMagazineEvent;
import dev.luzifer.backend.event.events.EscapeEvent;
import dev.luzifer.backend.event.events.IWannaKnifeEvent;
import dev.luzifer.backend.event.events.JumpEvent;
import dev.luzifer.backend.event.events.MouseLeftClickEvent;
import dev.luzifer.backend.event.events.MouseMoveEvent;
import dev.luzifer.backend.event.events.MouseRightClickEvent;
import dev.luzifer.backend.event.events.MoveEvent;
import dev.luzifer.backend.event.events.ReloadEvent;
import dev.luzifer.backend.event.events.ShiftEvent;
import dev.luzifer.backend.exception.UncaughtExceptionLogger;
import dev.luzifer.backend.scheduler.Scheduler;
import dev.luzifer.backend.scheduler.SchedulerThread;
import dev.luzifer.gui.AppStarter;
import javafx.application.Application;

import java.io.File;

public class Main {
    
    private static final File LOG_FILE = new File("log.txt");
    private static final UncaughtExceptionLogger DEFAULT_UNCAUGHT_EXCEPTION_LOGGER = new UncaughtExceptionLogger(LOG_FILE);

    private static final EventRepository EVENT_REPOSITORY = new EventRepository();
    private static final EventClusterRepository EVENT_CLUSTER_REPOSITORY = new EventClusterRepository();

    private static final Scheduler SCHEDULER = new Scheduler();
    
    public static void main(String[] args) {
    
        registerEvents();
    
        setupScheduler();
        // setupEventExecutor();
        
        Thread.currentThread().setUncaughtExceptionHandler(DEFAULT_UNCAUGHT_EXCEPTION_LOGGER);
        Application.launch(AppStarter.class);
    }

    // I don't like those singletons

    public static Scheduler getScheduler() {
        return SCHEDULER;
    }
    
    public static EventRepository getEventRepository() {
        return EVENT_REPOSITORY;
    }
    
    public static EventClusterRepository getEventClusterRepository() {
        return EVENT_CLUSTER_REPOSITORY;
    }
    
    private static void setupScheduler() {
    
        SchedulerThread schedulerThread = new SchedulerThread(SCHEDULER);
        schedulerThread.setUncaughtExceptionHandler(DEFAULT_UNCAUGHT_EXCEPTION_LOGGER);
        schedulerThread.setDaemon(true);
        schedulerThread.start();
    }
    
    private static void setupEventExecutor() {
        
        EventExecutor eventExecutor = new EventExecutor(EVENT_REPOSITORY, EVENT_CLUSTER_REPOSITORY);
        eventExecutor.setUncaughtExceptionHandler(DEFAULT_UNCAUGHT_EXCEPTION_LOGGER);
        eventExecutor.setDaemon(true);
        eventExecutor.start();
    }
    
    private static void registerEvents() {
    
        EVENT_REPOSITORY.registerEvent(new MouseLeftClickEvent());
        EVENT_REPOSITORY.registerEvent(new MouseRightClickEvent());
        EVENT_REPOSITORY.registerEvent(new MouseMoveEvent());
        EVENT_REPOSITORY.registerEvent(new MoveEvent());
        EVENT_REPOSITORY.registerEvent(new CrouchEvent());
        EVENT_REPOSITORY.registerEvent(new ShiftEvent());
        EVENT_REPOSITORY.registerEvent(new JumpEvent());
        EVENT_REPOSITORY.registerEvent(new ReloadEvent());
        EVENT_REPOSITORY.registerEvent(new EscapeEvent());
        EVENT_REPOSITORY.registerEvent(new DropWeaponEvent());
        EVENT_REPOSITORY.registerEvent(new EmptyMagazineEvent());
        EVENT_REPOSITORY.registerEvent(new IWannaKnifeEvent());
    }
    
}