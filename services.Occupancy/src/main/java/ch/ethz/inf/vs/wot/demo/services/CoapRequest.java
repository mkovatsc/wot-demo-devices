package ch.ethz.inf.vs.wot.demo.services;


import ch.ethz.inf.vs.semantics.parser.ExecutionPlan;
import ch.ethz.inf.vs.semantics.parser.ExecutionPlan.IRequestFactory;
import ch.ethz.inf.vs.semantics.parser.ExecutionPlan.RequestCallback;
import ch.ethz.inf.vs.wot.demo.devices.utils.DeviceServer;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.CoapEndpoint;

import java.net.InetSocketAddress;


public class CoapRequest extends ExecutionPlan.Request {
    public CoapRequest(String id, ExecutionPlan.RequestValue method, ExecutionPlan.RequestValue uri, ExecutionPlan.RequestValue reqBody, ExecutionPlan.RequestValue resp) {
        super(id, method, uri, reqBody, resp);
    }

    @Override
    public void execute(final RequestCallback callback) {
        this.running = true;
        System.out.println("REQUEST " + method + " " + uri);
        if (this.reqBody != null)
            System.out.println("reqBody:\n" + reqBody);
        if (this.resp != null)
            System.out.println("resp:\n" + resp);

        CoapClient client = new CoapClient();

        client.setEndpoint(new CoapEndpoint(new InetSocketAddress("2001:0470:cafe::38b2:cf50",0)));
        client.setTimeout(10000);
        client.setURI(uri.toString().replace("coap://localhost", "coap://" + DeviceServer.DEMO_IP).replace("coap://127.0.0.1", "coap://" + DeviceServer.DEMO_IP));
        String m = this.method.toString().toUpperCase();
        CoapHandler handler = new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                done = true;
                if (callback != null) {
                    callback.onComplete(response.getResponseText());

                }
            }

            @Override
            public void onError() {

                System.err.println("REQUEST " + CoapRequest.this.method + " " + CoapRequest.this.uri);
            }
        };
        if (m.equals("POST")) {
            client.post(handler, this.reqBody.toString(), MediaTypeRegistry.TEXT_PLAIN);
        } else if (m.equals("PUT")) {
            client.put(handler, this.reqBody.toString(), MediaTypeRegistry.TEXT_PLAIN);
        } else if (m.equals("GET")) {
            client.get(handler);
        }
    }

    public static class Factory implements IRequestFactory {

        @Override
        public ExecutionPlan.Request getRequest(String id, ExecutionPlan.RequestValue method, ExecutionPlan.RequestValue uri, ExecutionPlan.RequestValue reqBody, ExecutionPlan.RequestValue resp) {
            return new CoapRequest(id, method, uri, reqBody, resp);
        }
    }
}
