package ch.ethz.inf.vs.wot.demo.android;

import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.server.resources.Resource;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DeviceServer extends CoapServer {

    public static final String DEMO_IP = "[2001:0470:cafe::38b2:cf50]";
    private static final int RD_LIFETIME = 20; // minimum is 60 seconds
    private static ScheduledThreadPoolExecutor tasks = new ScheduledThreadPoolExecutor(1);

    private String rdHandle;
    public final String id = UUID.randomUUID().toString();
    ;

    public DeviceServer(int port) {
        super(port);
        tasks.schedule(new Runnable() {
            @Override
            public void run() {
                registerSelf();
            }
        }, 6, TimeUnit.SECONDS);
    }


    private void registerSelf() {
        CoapClient c = createClient();
        c.setTimeout(5000);
        c.setURI("coap://" + DEMO_IP + ":5683");
        Set<WebLink> resources = c.discover("rt=core.rd");
        if (resources != null) {
            if (resources.size() > 0) {
                WebLink w = resources.iterator().next();
                String uri = "coap://" + DEMO_IP + ":5683" + w.getURI();
                registerSelf(uri);
            }
        } else {
            System.out.println("Discover timeout");
        }
    }

    private void registerSelf(String uri) {
        final CoapClient client = createClient();
        client.setTimeout(5000);
        client.setURI(uri + "?ep=" + getID() + "&lt=" + RD_LIFETIME);
        client.post(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {

                rdHandle = "coap://" + DEMO_IP + ":5683/" + response.getOptions().getLocationPathString();

                tasks.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        client.setURI(rdHandle);
                        client.post("", MediaTypeRegistry.APPLICATION_LINK_FORMAT);
                    }
                }, RD_LIFETIME, RD_LIFETIME, TimeUnit.SECONDS);
            }

            @Override
            public void onError() {
            }

        }, discoverTree(), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
    }

    String discoverTree() {
        StringBuilder buffer = new StringBuilder();
        for (Resource child : getRoot().getChildren()) {
            LinkFormat.serializeTree(child, null, buffer);
        }
        // remove last comma ',' of the buffer
        if (buffer.length() > 1)
            buffer.delete(buffer.length() - 1, buffer.length());

        return buffer.toString();
    }

    final public CoapClient createClient() {
        CoapClient client = new CoapClient();
        List<Endpoint> endpoints = getEndpoints();
        client.setExecutor(getRoot().getExecutor());
        if (!endpoints.isEmpty()) {
            Endpoint ep = endpoints.get(0);
            client.setEndpoint(ep);
        }
        return client;
    }

    final public String getID() {
        return id;
    }
}
