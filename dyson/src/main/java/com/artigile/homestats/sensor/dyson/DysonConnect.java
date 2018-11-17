package com.artigile.homestats.sensor.dyson;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

import com.artigile.homestats.sensor.dyson.model.DeviceDescription;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.HttpStatus;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.util.Strings;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DysonConnect {

    private final static Logger LOGGER = LoggerFactory.getLogger(DysonConnect.class);

    private static final String API_HOSTNAME = "api.cp.dyson.com";
    private static final String BASIC_AUTH_URL =
        "https://" + API_HOSTNAME + "/v1/userregistration/authenticate?country=";
    private static final String REGISTERED_DEVICES_URL =
        "https://" + API_HOSTNAME + "/v2/provisioningservice/manifest";
    private static final String BASIC_AUTH_EMAIL = "Email";
    private static final String REGION = "US";
    private static final String BASIC_AUTH_PWD = "Password";
    private final String email;
    private final String password;

    public DysonConnect(final String email, final String password) {
        Preconditions.checkArgument(!Strings.isEmpty(email),"Please provide email, email can't be empty");
        Preconditions.checkArgument(!Strings.isEmpty(email),"Please provide password, password can't be empty");
        this.email = email;
        this.password = password;
    }

    /**
     * Returns set of dyson devices registered in local network. Each device will be queried for updates sensor data on
     * regular basis.
     * This method involves communication over 3 protocols:
     * 1. Rest http(OAuth2.0): First it makes http OAuth2.0 Basic auth(credentials flow) call to dyson api{@link
     * #API_HOSTNAME} to get list
     * of registered online devices.
     * 2. Multicast DNS: After list of devices received and list if not empty, this method will ask {@link
     * MDnsDysonFinder} to look up
     * {@link InetSocketAddress} for each device so that we can connect to it over MQTT in next step.
     * 3. MQTT: Established MQTT connection to devices and polls for a new data with provided pollInterval.
     *
     * @param pollInterval poll interval that dyson devices will be asked for new data.
     * @param timeUnit timeUnit.
     */
    final public ImmutableSet<DysonDataProvider> watchLocalDevices(final long pollInterval,
                                                                   final TimeUnit timeUnit) throws UnirestException,
        IOException, MqttException {
        final HttpResponse<JsonNode> loginResponse = Unirest.post(BASIC_AUTH_URL + REGION)
            .field(BASIC_AUTH_EMAIL, email)
            .field(BASIC_AUTH_PWD, password)
            .asJson();
        Preconditions.checkState(loginResponse.getStatus() == HttpStatus.SC_OK,
            "Login failed. received response:" + loginResponse.getBody());

        final Set<DeviceDescription> registeredDevices =
            getRegisteredDevices(loginResponse.getHeaders().getFirst(AUTHORIZATION));
        if (registeredDevices.isEmpty()) {
            LOGGER.warn("No devised registered online for user: {}", email);
            return ImmutableSet.of();
        }
        final ImmutableSet.Builder<DysonDataProvider> dataProvidersBuilder = ImmutableSet.builder();
        for (DeviceDescription deviceDescription : registeredDevices) {
            final String deviceIdentifier =
                deviceDescription.productType + "_" + deviceDescription.serial;
            final InetSocketAddress deviceLocalAddress = MDnsDysonFinder.getDeviceById(deviceIdentifier, 5,
                TimeUnit.SECONDS);
            if (deviceLocalAddress == null) {
                LOGGER.warn("No dyson device found for identifier: " + deviceIdentifier);
            } else if (deviceDescription.localCredentials != null) {
                dataProvidersBuilder.add(
                    new DysonDataProvider(deviceLocalAddress, deviceDescription, pollInterval, timeUnit));
            }
        }

        return dataProvidersBuilder.build();
    }

    /**
     * Fetches list of registered devices over rest API(using Basic Oauth2.0 access token)
     *
     * @param basicToken basicAuthToken
     * @return set of JSON objects. Each json object represents dyson device registered online.
     * @throws UnirestException thrown when error happened connecting to dyson api.
     */
    private Set<DeviceDescription> getRegisteredDevices(final String basicToken) throws UnirestException {
        final HttpResponse<JsonNode> rawRegisteredDevices = Unirest.get(
            REGISTERED_DEVICES_URL)
            .header(AUTHORIZATION, basicToken).asJson();
        Preconditions.checkState(rawRegisteredDevices.getStatus() == HttpStatus.SC_OK,
            "Failed to fetch registered online devices" + rawRegisteredDevices.getBody());

        final ImmutableSet.Builder<DeviceDescription> devicesBuilder = ImmutableSet.builder();
        final JSONArray devicesArray = rawRegisteredDevices.getBody().getArray();
        for (int i = 0; i < devicesArray.length(); i++) {
            final JSONObject jsonObject = devicesArray.getJSONObject(i);
            devicesBuilder.add(new DeviceDescription.Builder()
                .withSerial(jsonObject.getString("Serial"))
                .withConnectionType(jsonObject.getString("ConnectionType"))
                .withVersion(jsonObject.getString("Version"))
                .withAutoUpdate(jsonObject.getBoolean("AutoUpdate"))
                .withNewVersionAvailable(jsonObject.getBoolean("NewVersionAvailable"))
                .withProductType(jsonObject.getString("ProductType"))
                .withLocalCredentials(decryptLocalCredentials(jsonObject.getString("LocalCredentials")))
                .withName(jsonObject.getString("Name"))
                .build());
        }
        return devicesBuilder.build();
    }

    private DeviceDescription.LocalCredentials decryptLocalCredentials(final String localCredentials) {
        final byte KEY[] = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
            0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x20};
        final byte[] ivBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00};
        try {
            byte[] encrypted_decoded_bytes = Base64.getDecoder().decode(localCredentials);

            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            SecretKeySpec skeySpec = new SecretKeySpec(KEY, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            final String decrypted = new String(cipher.doFinal(encrypted_decoded_bytes));//Returns garbage characters
            final JSONObject localCredJson = new JSONObject(decrypted);
            return new DeviceDescription.LocalCredentials.Builder()
                .withPasswordHash(localCredJson.getString("apPasswordHash"))
                .withSerial(localCredJson.getString("serial"))
                .build();

        } catch (Exception e) {
            LOGGER.warn(
                "device local credentials failed to be parsed. This device won't be connected to. LocalCredentials: {}",
                localCredentials);
            return null;
        }
    }
}
