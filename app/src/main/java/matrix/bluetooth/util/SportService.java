package matrix.bluetooth.util;

public class SportService {
    private int imageID;
    private String serviceName;
    private int serviceID;

    public int getImageID() {
        return imageID;
    }
    public String getServiceName() {
        return serviceName;
    }
    public int getServiceID() {
        return serviceID;
    }
    public SportService(int imageID, String name, int id) {
        this.imageID = imageID;
        this.serviceName = name;
        this.serviceID = id;
    }


}
