package ch.ethz.inf.vs.wot.demo.utils.w3c;

public class PropertyResource extends WoTResource {
	
	public PropertyResource(String type, String name, String href, String valueType, boolean writable) {
		super(type, name, href);
		
		td.addProperty("@type", type);
		td.addProperty("name", name);
		td.addProperty("valueType", valueType);
		td.addProperty("writable", writable);	
	}
}
