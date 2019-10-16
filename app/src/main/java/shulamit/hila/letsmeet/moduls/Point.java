package shulamit.hila.letsmeet.moduls;

public class Point {
    private double lat;
    private double lng;
    private String name;
    /**the class define a Point with a lat , lng and name
     * */
    public Point(double lat, double lng, String name) {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }
}
