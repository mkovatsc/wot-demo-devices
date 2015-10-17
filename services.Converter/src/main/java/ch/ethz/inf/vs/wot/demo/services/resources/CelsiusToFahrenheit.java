package ch.ethz.inf.vs.wot.demo.services.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.text.DecimalFormat;

/**
 * @author Yassin N. Hassan
 * @since 02.11.14.
 */
public class CelsiusToFahrenheit extends CoapResource {

	public CelsiusToFahrenheit() {
		super("cel2degf");
		getAttributes().addResourceType("converter");
	}

	@Override
	public void handlePOST(CoapExchange exchange) {
		try {
			double celsius = Double.parseDouble(exchange.getRequestText());
			double fahrenheit = (celsius * 9d / 5d) + 32d;
			// I hope we can extend this to ##.00 to also support floating point values
			exchange.respond(new DecimalFormat("#0").format(fahrenheit));
		} catch (Exception ex) {
			exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
		}
	}
}
