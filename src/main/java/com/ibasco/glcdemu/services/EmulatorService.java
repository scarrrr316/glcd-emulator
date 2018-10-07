package com.ibasco.glcdemu.services;

import com.ibasco.glcdemu.Context;
import com.ibasco.glcdemu.emulator.GlcdEmulator;
import com.ibasco.glcdemu.emulator.GlcdEmulatorFactory;
import com.ibasco.glcdemu.enums.ConnectionType;
import com.ibasco.glcdemu.net.EmulatorListenerTask;
import com.ibasco.glcdemu.net.ListenerOptions;
import com.ibasco.glcdemu.utils.PixelBuffer;
import com.ibasco.pidisplay.drivers.glcd.GlcdDisplay;
import com.ibasco.pidisplay.drivers.glcd.enums.GlcdBusInterface;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * A background service that listens on the network for emulator instructions and processes it accordingly.
 *
 * @author Rafael Ibasco
 */
public class EmulatorService extends Service<Void> {

    private static final Logger log = LoggerFactory.getLogger(EmulatorService.class);

    //<editor-fold desc="Service Properties">
    private ObjectProperty<GlcdDisplay> display = new SimpleObjectProperty<>();

    private ObjectProperty<GlcdBusInterface> busInterface = new SimpleObjectProperty<>();

    private ObjectProperty<PixelBuffer> pixelBuffer = new SimpleObjectProperty<>();

    private ReadOnlyBooleanWrapper clientConnected = new ReadOnlyBooleanWrapper();

    private ObjectProperty<ConnectionType> connectionType = new SimpleObjectProperty<>();

    private ObjectProperty<ListenerOptions> connectionOptions = new SimpleObjectProperty<>();

    private ReadOnlyObjectWrapper<EmulatorListenerTask> emulatorTask = new ReadOnlyObjectWrapper<>();
    //</editor-fold>

    private ChangeListener<String> messageListener;

    public EmulatorService() {
        setExecutor(Context.getTaskExecutor());
    }

    //<editor-fold desc="Service Getter/Setter Properties">
    public PixelBuffer getPixelBuffer() {
        return pixelBuffer.get();
    }

    public ObjectProperty<PixelBuffer> pixelBufferProperty() {
        return pixelBuffer;
    }

    public void setPixelBuffer(PixelBuffer pixelBuffer) {
        this.pixelBuffer.set(pixelBuffer);
    }

    public GlcdDisplay getDisplay() {
        return display.get();
    }

    public ObjectProperty<GlcdDisplay> displayProperty() {
        return display;
    }

    public void setDisplay(GlcdDisplay display) {
        this.display.set(display);
    }

    public GlcdBusInterface getBusInterface() {
        return busInterface.get();
    }

    public ObjectProperty<GlcdBusInterface> busInterfaceProperty() {
        return busInterface;
    }

    public void setBusInterface(GlcdBusInterface busInterface) {
        this.busInterface.set(busInterface);
    }

    public EmulatorListenerTask getEmulatorTask() {
        return emulatorTask.get();
    }

    public ReadOnlyObjectProperty<EmulatorListenerTask> emulatorTaskProperty() {
        return emulatorTask.getReadOnlyProperty();
    }

    public ListenerOptions getConnectionOptions() {
        return connectionOptions.get();
    }

    public ObjectProperty<ListenerOptions> connectionOptionsProperty() {
        return connectionOptions;
    }

    public void setConnectionOptions(ListenerOptions connectionOptions) {
        this.connectionOptions.set(connectionOptions);
    }

    public ConnectionType getConnectionType() {
        return connectionType.get();
    }

    public ObjectProperty<ConnectionType> connectionTypeProperty() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType.set(connectionType);
    }

    public boolean isClientConnected() {
        return clientConnected.get();
    }

    public ReadOnlyBooleanProperty clientConnectedProperty() {
        return clientConnected.getReadOnlyProperty();
    }

    private void setClientConnected(boolean value) {
        Platform.runLater(() -> clientConnected.set(value));
    }
    //</editor-fold>

    public void setMessageListener(ChangeListener<String> listener) {
        this.messageListener = listener;
    }

    private GlcdEmulator createEmulator() {
        return GlcdEmulatorFactory.createFrom(getDisplay(), getBusInterface(), getPixelBuffer());
    }

    /**
     * Factory method for constructing listener tasks based on the connection type provided
     *
     * @param connectionType
     *         The {@link ConnectionType} of this emulator
     *
     * @return A {@link EmulatorListenerTask}
     */
    private EmulatorListenerTask createListenerTask(ConnectionType connectionType) {
        try {
            Class<? extends EmulatorListenerTask> listenerClass = connectionType.getListenerClass();
            return listenerClass.getConstructor(GlcdEmulator.class).newInstance(createEmulator());
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Error occured while instatiating listener class", e);
        }
    }

    @Override
    protected Task<Void> createTask() {
        if (connectionType.get() == null)
            throw new IllegalStateException("No listener task has been specified");
        if (connectionOptions.get() == null)
            throw new IllegalStateException("No connection options specified");
        EmulatorListenerTask task = createListenerTask(connectionType.get());
        emulatorTask.set(task);
        task.listenerOptionsProperty().bind(connectionOptions);
        clientConnected.bindBidirectional(task.connectedProperty());
        if (messageListener != null) {
            task.messageProperty().addListener(messageListener);
        }
        return task;
    }
}
