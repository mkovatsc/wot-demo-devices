package ch.ethz.inf.vs.wot.demo.services.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.text.DecimalFormat;

/**
 * @author Yassin N. Hassan
 * @since 02.11.14.
 */
public class FahrenheitToCelsius extends CoapResource {

	public FahrenheitToCelsius() {
		super("degf2cel");
		getAttributes().addResourceType("converter");
	}

	@Override
	public void handlePOST(CoapExchange exchange) {
		try {
			double fahrenheit = Double.parseDouble(exchange.getRequestText());
			double celsius  = (fahrenheit - 32d) * 5d / 9d;
			// I hope we can extend this to ##.00 to also support floating point values
			exchange.respond(new DecimalFormat("#0").format(celsius));
		} catch (Exception ex) {
			exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
		}
	}
}
