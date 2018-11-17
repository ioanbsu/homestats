package com.artigile.homestats.sensor.dyson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

/**
 * Utilises jmdns library to do multicast dns devices resolving. Finds dyson device by provided device id.
 * If nothing is foind the
 */
public class MDnsDysonFinder implements ServiceListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(MDnsDysonFinder.class);

    /**
     * Filtering mdns services to only watch for dyson types.
     */
    private static final String DYSON_SERVICE_TYPE = "_dyson_mqtt._tcp.local.";
    private static final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private final CompletableFuture<InetSocketAddress> foundDeviceFuture;
    private final String deviceIdentifier;

    public MDnsDysonFinder(final CompletableFuture<InetSocketAddress> foundDeviceFuture,
                           final String deviceIdentifier) {
        this.foundDeviceFuture = foundDeviceFuture;
        this.deviceIdentifier = deviceIdentifier;
    }

    /**
     * Does dyson device lookup by provided device id. If no device found in within specified TTL returns null.
     *
     * @param deviceId device id.
     * @param waitTimeout wait timeout.
     * @param timeUnit time unit to wait in.
     * @return Inet Socket Address of the resolved device, OR null is nothing is found within specified TTL.
     * @throws IOException in case local address to bind the mdns can not be resolved. Should not be the case really,
     * unless there is some sort of VPN used on machine where this executed.
     */
    @Nullable
    public static InetSocketAddress getDeviceById(final String deviceId, final long waitTimeout,
                                                  final TimeUnit timeUnit) throws IOException {
        CompletableFuture<InetSocketAddress> foundDeviceFuture = new CompletableFuture<>();
        final MDnsDysonFinder deviceFinder = new MDnsDysonFinder(foundDeviceFuture, deviceId);
        // Create a JmDNS instance
        JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());

        // Add a service listener
        jmdns.addServiceListener(DYSON_SERVICE_TYPE, deviceFinder);
        final InetSocketAddress dysonDevice = deviceFinder.lookUp(waitTimeout, timeUnit);
        singleThreadExecutor.submit(() -> {
            try {
                jmdns.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return dysonDevice;
    }

    /**
     * waits for the device to be resolved.
     *
     * @param waitTimeout max time to wait before giving up and returning null.
     * @param timeUnit wait time units.
     * @return Inet socket address if such found. Null otherwise.
     */
    private InetSocketAddress lookUp(final long waitTimeout, final TimeUnit timeUnit) {
        try {
            return foundDeviceFuture.get(waitTimeout, timeUnit);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            LOGGER.error("Failed to get local dyson device socket address.", e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void serviceAdded(ServiceEvent event) {
        LOGGER.info("Service added: " + event.getInfo());
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        LOGGER.info("Service removed: " + event.getInfo());
    }

    /**
     * When service is resolved making sure that it matches dyson device id "nubmer"(it's not really a number so to
     * speak, there are letters in it as well.
     *
     * @param event information about resolved service.
     */
    @Override
    public void serviceResolved(ServiceEvent event) {
        final Inet4Address[] addresses = event.getInfo().getInet4Addresses();
        if (addresses.length == 0) {
            LOGGER.warn("Address is resolved but no Inet4AddressAvailable: " + addresses);
        }
        if (deviceIdentifier.equals(event.getInfo().getName())) {
            foundDeviceFuture.complete(new InetSocketAddress(addresses[0].getHostAddress(), event.getInfo().getPort()));
        } else {
            LOGGER.warn("Found device but ids are not matching. Expected id " + deviceIdentifier + ", actual id " +
                event.getInfo().getName());
        }
        System.out.println("Service resolved: " + event.getInfo());
    }
}
