/**
 * Place.java
 * Created On 2005, Nov 19, 2005 1:21:26 PM
 * @author E. Rajasekar
 */

package app.astrosoft.beans;

import static app.astrosoft.beans.Place.Direction.ofChar;
import static app.astrosoft.beans.Place.Direction.ofVal;
import java.util.EnumSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.XmlConsts;
import app.astrosoft.export.XMLHelper;
import static app.astrosoft.export.XMLHelper.addAttribute;
import static app.astrosoft.export.XMLHelper.addElement;
import app.astrosoft.util.AstroUtil;
import static app.astrosoft.util.AstroUtil.decimal;
import static app.astrosoft.util.AstroUtil.int_dms;
import static app.astrosoft.util.AstroUtil.toDouble;
import static app.astrosoft.util.AstroUtil.toDouble;
import static app.astrosoft.util.AstroUtil.twoDigit;
import app.astrosoft.util.AstrosoftTimeZone;
import static app.astrosoft.util.AstrosoftTimeZone.offset;
import static java.lang.Integer.parseInt;
import static java.lang.Integer.parseInt;
import static java.lang.Math.abs;
import static java.lang.String.format;

public class Place {

	public static enum LocationType{
		Longitude, Latitude;
	}
	
	public static enum Direction {
		EAST(1),WEST(-1),NORTH(1),SOUTH(-1);
		
		private int val = 1;
		
		private Direction(int val) {
			this.val = val;
		}
		
		public static Direction ofChar(char c){
			for(Direction d : values()){
				if(d.charVal() == Character.toUpperCase(c)){
					return d;
				}
			}
			return null;
		}
		
		public char charVal(){
			return this.name().charAt(0);
		}
		
		public static EnumSet NS(){
			return EnumSet.of(NORTH, SOUTH);
		}
		
		public static EnumSet EW(){
			return EnumSet.of(EAST, WEST);
		}
		
		public static Direction ofVal(double val, LocationType type){
			switch(type){
				case Latitude: return val < 0 ? SOUTH : NORTH;
				case Longitude: return val < 0 ? WEST : EAST;
			}
			return null;
		}
	};
	
	public static class Location{
		private int deg;
		private int min;
		Direction dir;
		
		public Location(int deg, int min, Direction dir) {
			this.deg = deg;
			this.min = min;
			this.dir = dir;
		}
		
		public Location(String deg, String min, Direction dir) {
			this(parseInt(deg), parseInt(min), dir);
		}
		
		public Location(double value, LocationType type) {
			int []degmin = int_dms(value);
			
			deg = abs(degmin[0]);
			min = abs(degmin[1]);
			dir = ofVal(value, type);
		}
		
		public double value(){
			return decimal(deg, min, 0) * dir.val;
		}
		
		public int deg(){
			return deg;
		}
		
		public int min(){
			return min;
		}
		
		public Direction dir(){
			return dir;
		}
		
		@Override
		public String toString() {
			
			return deg + "." + min + dir;
		}
		
		public String format(){
			
			return twoDigit(deg) + "." + twoDigit(min);
		}
	}
	
	private String city;
	private String state;
	private String country;
	private double longitude;
	private double latitude;
	private double timeZone;
	private String timeZoneId;
	
	public Place(String city, String state, String country, double latitude, double longitude, double timeZone) {
		this.state = state;
		this.country = country;
		this.city = city;
		this.longitude = longitude; 
		this.latitude = latitude;
		this.timeZone = timeZone;
	}
	
	public Place(String city, String state, String country, double latitude, double longitude, String timeZoneId) {
		this(city, state, country, latitude, longitude, offset(timeZoneId));
		this.timeZoneId = timeZoneId;
	}
	
	public Place(String city, String state, String country, double latitude, Direction latDir, double longitude, Direction longDir, double timeZone) {
		this(city, state, country , latitude * latDir.val, longitude * longDir.val, timeZone);
	}
	
	public Place(String city, String state, String country, String latitude, char latDir, String longitude, char longDir, String timeZoneId) {
		this(city, state, country, toDouble(latitude,"\\."), ofChar(latDir), toDouble(longitude, "\\."), ofChar(longDir), offset(timeZoneId));
		this.timeZoneId = timeZoneId;
	}
	
	/*public Place(String city, String state, String country, String latitude, char latDir, String longitude, char longDir, double timeZone) {
		this(city, state, country, latitude, latDir, longitude, longDir, )
	}*/

	public Place(String city, double latitude, double longitude, double timeZone) {
		this(city, null, null, latitude, longitude, timeZone);
	}
	
	public Place(String city, String state, String country, Location latitude, Location longitude, String timeZoneId) {
		this(city, state, country , latitude.value(), longitude.value(), offset(timeZoneId));
		this.timeZoneId = timeZoneId;
	}
	
	public static Place getDefault(){
		return new Place("Erode", "Tamil Nadu", "India" , new Location(11, 22, Direction.NORTH), new Location(77,44, Direction.EAST), "IST");
	}
	
	public String city() {
		return city;
	}
	
	public String state() {
		return state;
	}
	
	public String country() {
		return country;
	}

	public String display(){
		return format("[%s , %s , %s, %s, %s, %s]", city, state, country, longitude, latitude, timeZone) ;
	}
	
	public String toString(){
		
		StringBuilder sb = new StringBuilder(city);
		if (state != null){
			sb.append(" , ");
			sb.append(state);
		}
		
		if (country != null){
			sb.append(" , ");
			sb.append(country);
		}
		return sb.toString() ;
	}
	
	public double longitude() {
		return longitude;
	}

	public double latitude() {
		return latitude;
	}
	
	public double timeZone() {
		return timeZone;
	}
	
	public void setTimeZone(double timeZone) {
		this.timeZone = timeZone;
	}
	
	public AstrosoftTimeZone astrosoftTimeZone(){
		return new AstrosoftTimeZone(timeZoneId);
	}
	
	public Location latitudeLocation(){
		return new Location(latitude, LocationType.Latitude);
	}
	
	public Location longitudeLocation(){
		return new Location(longitude, LocationType.Longitude);
	}
	
	public Element toXMLElement(Document doc){
		
		Element placeElement = doc.createElement(XmlConsts.Place);
		
		Location longitudeLoc = longitudeLocation();
		Location latitudeLoc = latitudeLocation();
		
		addElement(doc, placeElement, XmlConsts.City, this.city);
		
		Element longitudeElement = addElement(doc, placeElement, XmlConsts.Longitude, longitudeLoc.format());
		addAttribute(longitudeElement, XmlConsts.dir, longitudeLoc.dir().charVal());
		
		Element latitudeElement = addElement(doc, placeElement, XmlConsts.Latitude, latitudeLoc.format());
		addAttribute(latitudeElement, XmlConsts.dir, latitudeLoc.dir().charVal());
		
		//TODO: Remove null check once GetInput is fixed.
		addElement(doc, placeElement, XmlConsts.TimeZone, this.timeZoneId != null ? this.timeZoneId : "IST");
		
		return placeElement;
	}
	
	public static void main(String[] args) {
		System.out.println(getDefault());
	}

	public static Place valueOfXMLNode(Node placeNode) {
	
		String city = null;
		String state = null;
		String country = null;
		String longitude = null;
		String latitude = null;
		String timeZoneId = null;
		char latDir = ' ';
		char longDir = ' ';
		
		NodeList children = placeNode.getChildNodes();
		
		for(int i = 0; i < children.getLength(); i++){
		
			Node child = children.item(i);
			
            switch (child.getNodeName()) {
                case XmlConsts.City:
                    city = child.getTextContent();
                    break;
                case XmlConsts.State:
                    state = child.getTextContent();
                    break;
                case XmlConsts.Country:
                    country = child.getTextContent();
                    break;
                case XmlConsts.Longitude:
                    longitude = child.getTextContent();
                    longDir = child.getAttributes().getNamedItem(XmlConsts.dir).getNodeValue().charAt(0);
                    break;
                case XmlConsts.Latitude:
                    latitude = child.getTextContent();
                    latDir = child.getAttributes().getNamedItem(XmlConsts.dir).getNodeValue().charAt(0);
                    break;
                case XmlConsts.TimeZone:
                    timeZoneId = child.getTextContent();
                    break;
            }
		}
		return new Place(city, state, country, latitude, latDir, longitude, longDir, timeZoneId);
	}
}
