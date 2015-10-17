package ch.ethz.inf.vs.wot.demo.android.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.io.IOException;
import java.io.InputStream;

import ch.ethz.inf.vs.wot.demo.android.AudioDock;
import ch.ethz.inf.vs.wot.demo.android.R;

public class DeviceSemantics extends CoapResource {

    public DeviceSemantics() {
        super("restdesc");
        getAttributes().addResourceType("semantics");
    }

	@Override
	public void handleGET(CoapExchange exchange) {
		try {
			InputStream in_s = AudioDock.context.getResources().openRawResource(R.raw.audiodock);

			byte[] b = new byte[in_s.available()];
			in_s.read(b);
			String theString = new String(b);
			exchange.respond(theString);
		} catch (IOException e) {
			exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
		}
	}
}
