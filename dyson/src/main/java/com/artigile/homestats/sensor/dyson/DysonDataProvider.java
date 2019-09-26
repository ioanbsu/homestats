package com.artigile.homestats.sensor.dyson;

import com.artigile.homestats.sensor.dyson.model.DeviceDescription;
import com.artigile.homestats.sensor.dyson.model.DysonDevice;
import com.artigile.homestats.sensor.dyson.model.DysonPureCoolData;
import com.artigile.homestats.sensor.dyson.model.SensorData;
import com.artigile.homestats.sensor.dyson.model.State;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.Nullable;

/**
 * Established MQTT listener connection to dyson purifier and asks for updates periodically.
 */
public class DysonDataProvider implements MqttCallback {

    private final static Logger LOGGER = LoggerFactory.getLogger(DysonDataProvider.class);
    private final static int QOS = 1;
    private final static ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
    private final static SecureRandom mqttClientSessionIdRandomizer = new SecureRandom();
    private final DeviceDescription deviceDescription;
    private DysonPureCoolData.Builder dysonPureCoolDataBuilder = new DysonPureCoolData.Builder();
    private DysonDevice.Builder dysonDeviceBuilder = new DysonDevice.Builder();
    private MqttClient client;
    private Consumer<DysonDevice> dysonPureCoolDataConsumer;

    /**
     * Establishes connection to dyson devices ans periodically sends signal to it to force an updated mqtt message
     * about status to be broadcasted. Listens to this message in {@link #messageArrived(String, MqttMessage)}.
     *
     * @param deviceDescription product type (used to build mqtt topic URIs).
     * @param pollInterval      interval for how often to attempt to check for updates from devices.
     * @param timeUnit          the time unit used for poll interval.
     */
    public DysonDataProvider(final DeviceDescription deviceDescription, final long pollInterval,
                             final TimeUnit timeUnit) {
        this.deviceDescription = deviceDescription;
        dysonDeviceBuilder.withDeviceDescription(deviceDescription);
        final String askForUpdatesTopic = String.format("%s/%s/command", deviceDescription.productType,
            deviceDescription.localCredentials.serial);
        final String deviceIdentifier = deviceDescription.productType + "_" + deviceDescription.serial;
        EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            if (client == null) {
                LOGGER.info("Not connecting to dyson device, establishing connection {}", deviceDescription);
                try {
                    initiateNewMqttConnection(MDnsDysonFinder.getDeviceById(deviceIdentifier, 5, TimeUnit.SECONDS));
                } catch (MqttException | IOException e) {
                    LOGGER.warn("Failed to connect to dyson device. Will retry later. Device {}", deviceDescription);
                }
            }
            final String payload = "{\"msg\":\"REQUEST-CURRENT-STATE\",\"time\":\"" + Instant.now() + "\"}";
            LOGGER.debug("SendingSending CMD: {}", payload);
            try {
                if (!client.isConnected()) {
                    client.close(true);
                    LOGGER.info("Connection lost to dyson {}. \n\tReconnecting... ", deviceDescription);
                    initiateNewMqttConnection(MDnsDysonFinder.getDeviceById(deviceIdentifier, 5, TimeUnit.SECONDS));
                }
                client.publish(askForUpdatesTopic, new MqttMessage((payload).getBytes()));
            } catch (MqttException | IOException mqttException) {
                LOGGER.error(
                    "Can't connect to mqtt client. Resolving new local device address. Will try to reconnect later.",
                    mqttException);
            }
        }, 0, pollInterval, timeUnit);
    }

    private void initiateNewMqttConnection(@Nullable final InetSocketAddress deviceLocalAddress) throws MqttException {
        if (deviceLocalAddress == null) {
            LOGGER.warn("Provided device address was null, skipping connection for {}", deviceDescription);
            return;
        }
        final String host = String.format("tcp://%s:%d", deviceLocalAddress.getHostName(),
            deviceLocalAddress.getPort());
        final String username = deviceDescription.localCredentials.serial;
        final String topic = deviceDescription.productType + "/" + username + "/status/current";

        final MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        conOpt.setUserName(username);
        conOpt.setKeepAliveInterval(10);
        conOpt.setPassword(deviceDescription.localCredentials.passwordHash.toCharArray());

        this.client = new MqttClient(host, getOrGenerateClientId(), new MemoryPersistence());
        this.client.setCallback(this);
        this.client.connect(conOpt);
        this.client.subscribe(topic, QOS);
        this.client.setTimeToWait(1000);
    }

    private synchronized String getOrGenerateClientId() {
        return "JavaDysonListener_" + mqttClientSessionIdRandomizer.nextInt(1000);
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Error" + cause);
        try {
            client.reconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        LOGGER.debug("Message arrived: {}", message);
        try {
            final JSONObject jsonMsg = new JSONObject(new String(message.getPayload()));
            if (jsonMsg.getString("msg").equals(State.KEY)) {
                LOGGER.info("Received json: {}", jsonMsg.toString());
                final State.Builder stateBuilder = new State.Builder()
                    .withProductState(parseProductState(jsonMsg.getJSONObject("product-state")))
                    .withChannel(jsonMsg.getInt("channel"))
                    .withDate(Instant.parse(jsonMsg.getString("time")))
                    .withRssi(jsonMsg.getInt("rssi"));
                dysonPureCoolDataBuilder.withCurrentState(stateBuilder.build());
            } else if (jsonMsg.getString("msg").equals(SensorData.KEY)) {
                final JSONObject seonsorsJsonData = jsonMsg.getJSONObject("data");
                final SensorData.Builder sensorsDataBuilder =
                    new SensorData.Builder()
                        .withDate(Instant.parse(jsonMsg.getString("time")))
                        .withTempCelsius(seonsorsJsonData.getInt("tact") / 10. - 273)
                        .withHact(seonsorsJsonData.getInt("hact"))
                        .withPm25(seonsorsJsonData.getInt("pm25"))
                        .withPm10(seonsorsJsonData.getInt("pm10"))
                        .withVa10(seonsorsJsonData.getInt("va10"))
                        .withNoxl(seonsorsJsonData.getInt("noxl"))
                        .withP25r(seonsorsJsonData.getInt("p25r"))
                        .withP10r(seonsorsJsonData.getInt("p10r"))
                        .withSltm(parseBoolean(seonsorsJsonData, "sltm"));
                dysonPureCoolDataBuilder.withCurrentSensorData(sensorsDataBuilder.build());
                if (dysonPureCoolDataConsumer != null) {
                    dysonPureCoolDataConsumer.accept(
                        dysonDeviceBuilder.withDysonPureCoolData(dysonPureCoolDataBuilder.build()).build());
                }
            }

        } catch (Exception e) {
            LOGGER.warn("Message parsing exception", e);
        }
    }

    private State.ProductState parseProductState(final JSONObject productState) {

        return new State.ProductState.Builder()
            .withFpwr(parseBoolean(productState, "fpwr"))
            .withFdir(parseBoolean(productState, "fdir"))
            .withAuto(parseBoolean(productState, "auto"))
            .withOscs(parseBoolean(productState, "oscs"))
            .withOson(parseBoolean(productState, "oson"))
            .withNmod(parseBoolean(productState, "nmod"))
            .withRhtm(parseBoolean(productState, "rhtm"))
            .withFnst(productState.getString("fnst"))
            .withErcd(productState.getString("ercd"))
            .withWacd(productState.getString("wacd"))
            .withNmdv(productState.getInt("nmdv"))
            .withFnsp(productState.getString("fnsp"))
            .withBril(productState.getInt("bril"))
            .withCorf(parseBoolean(productState, "corf"))
            .withCflr(productState.getInt("cflr"))
            .withHflr(productState.getInt("hflr"))
            .withSltm(parseBoolean(productState, "sltm"))
            .withOsal(productState.getInt("osal"))
            .withOsau(productState.getInt("osau"))
            .withAncp(productState.getInt("ancp"))
            .build();

    }

    private boolean parseBoolean(final JSONObject jsonObject, final String key) {
        return "ON".equals(jsonObject.getString(key));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        LOGGER.debug("Token delivery event. {}", token);
    }

    public void registerDataConsumer(final Consumer<DysonDevice> dysonPureCoolDataConsumer) {
        this.dysonPureCoolDataConsumer = dysonPureCoolDataConsumer;
    }
}
