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

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Established MQTT listener connection to dyson purifier and asks for updates periodically.
 */
public class DysonDataProvider implements MqttCallback {

    private final static Logger LOGGER = LoggerFactory.getLogger(MDnsDysonFinder.class);
    private final static int QOS = 1;
    private final static ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
    private DysonPureCoolData.Builder dysonPureCoolDataBuilder = new DysonPureCoolData.Builder();
    private DysonDevice.Builder dysonDeviceBuilder = new DysonDevice.Builder();
    private MqttClient client;
    private Consumer<DysonDevice> dysonPureCoolDataConsumer;

    /**
     * Establishes connection to dyson devices ans periodically sends signal to it to force an updated mqtt message
     * about status to be broadcasted.
     * Listens to this message in {@link #messageArrived(String, MqttMessage)}.
     *
     * @param device device socket address
     * @param deviceDescription product type (used to build mqtt topic URIs).
     * @throws MqttException thrown in case something wrong happens in mqtt communication.
     */
    public DysonDataProvider(final InetSocketAddress device,
                             final DeviceDescription deviceDescription, long pollInterval,
                             TimeUnit timeUnit) throws MqttException {

        dysonDeviceBuilder.withDeviceDescription(deviceDescription);
        final String host = String.format("tcp://%s:%d", device.getHostName(), device.getPort());
        final String username = deviceDescription.localCredentials.serial;
        final String topic = deviceDescription.productType + "/" + username + "/status/current";

        final MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        conOpt.setUserName(username);
        conOpt.setPassword(deviceDescription.localCredentials.passwordHash.toCharArray());
        conOpt.setAutomaticReconnect(true);

        this.client = new MqttClient(host, "JavaDysonListener", new MemoryPersistence());
        this.client.setCallback(this);
        this.client.connect(conOpt);

        this.client.subscribe(topic, QOS);
        EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            final String payload = "{\"msg\":\"REQUEST-CURRENT-STATE\",\"time\":\"" + Instant.now() + "\"}";
            LOGGER.debug("SendingSending CMD: {}", payload);
            try {
                if (!client.isConnected()) {
                    client.connect(conOpt);
                }
                client.publish(deviceDescription.productType + "/" + username + "/command",
                    new MqttMessage((payload).getBytes()));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }, 0, pollInterval, timeUnit);
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
                final State.Builder stateBuilder = new State.Builder()
                    .withProductState(parseProductState(jsonMsg.getJSONObject("product-state")))
                    .withChannel(jsonMsg.getInt("channel"))
                    .withDate(Instant.parse(jsonMsg.getString("time")))
                    .withDial(parseBoolean(jsonMsg, "dial"))
                    .withRssi(jsonMsg.getInt("rssi"));
                dysonPureCoolDataBuilder.withCurrentState(stateBuilder.build());
            } else if (jsonMsg.getString("msg").equals(SensorData.KEY)) {
                final JSONObject seonsorsJsonData = jsonMsg.getJSONObject("data");
                final SensorData.Builder sensorsDataBuilder =
                    new SensorData.Builder()
                        .withDate(Instant.parse(jsonMsg.getString("time")))
                        .withTact(seonsorsJsonData.getInt("tact"))
                        .withHact(seonsorsJsonData.getInt("hact"))
                        .withPm25(seonsorsJsonData.getInt("pm25"))
                        .withPm10(seonsorsJsonData.getInt("pm10"))
                        .withVa10(seonsorsJsonData.getInt("va10"))
                        .withNoxl(seonsorsJsonData.getInt("noxl"))
                        .withP25r(seonsorsJsonData.getInt("p25r"))
                        .withP10r(seonsorsJsonData.getInt("p10r"))
                        .withSltm(parseBoolean(seonsorsJsonData, "sltm"));
                dysonPureCoolDataBuilder.withCurrentSensorData(sensorsDataBuilder.build());
            }
            if (dysonPureCoolDataConsumer != null) {
                dysonPureCoolDataConsumer.accept(
                    dysonDeviceBuilder.withDysonPureCoolData(dysonPureCoolDataBuilder.build()).build());
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
