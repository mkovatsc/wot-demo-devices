package ch.ethz.inf.vs.wot.demo.devices.utils;

import ch.ethz.inf.vs.semantics.parser.ExecutionPlan;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class CoapRequest extends ExecutionPlan.Request {
    public CoapRequest(String id, ExecutionPlan.RequestValue method, ExecutionPlan.RequestValue uri, ExecutionPlan.RequestValue reqBody, ExecutionPlan.RequestValue resp) {
        super(id, method, uri, reqBody, resp);
    }

    @Override
    public void execute(final ExecutionPlan.RequestCallback callback) {
        running = true;
        System.out.println("REQUEST: " + this.method + " " + this.uri);
        if (reqBody != null)
            System.out.println("reqBody:\n" + this.reqBody);
        if (resp != null)
            System.out.println("resp:\n" + this.resp);

        CoapClient client = new CoapClient();
        client.setURI(this.uri.toString());
        String m = method.toString().toUpperCase();
        CoapHandler handler = new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                CoapRequest.this.done = true;
                if (callback != null) {
                    callback.onComplete(response);

                }
            }

            @Override
            public void onError() {
                System.out.println("ERROR: " + CoapRequest.this.method + " " + CoapRequest.this.uri);
            }
        };
        if (m.equals("POST")) {
            client.post(handler, reqBody.toString(), MediaTypeRegistry.TEXT_PLAIN);
        } else if (m.equals("PUT")) {
            client.put(handler, reqBody.toString(), MediaTypeRegistry.TEXT_PLAIN);
        } else if (m.equals("GET")) {
            client.get(handler);
        }
    }

    public static class Factory implements ExecutionPlan.IRequestFactory {

        @Override
        public ExecutionPlan.Request getRequest(String id, ExecutionPlan.RequestValue method, ExecutionPlan.RequestValue uri, ExecutionPlan.RequestValue reqBody, ExecutionPlan.RequestValue resp) {
            return new CoapRequest(id, method, uri, reqBody, resp);
        }
    }
}
