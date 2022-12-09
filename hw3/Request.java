package HW1;

public class Request {
    char type;
    int id;
    double runs = 1;
    double original_arrival_time;
    double arrival_time;
    double start_time;
    double finish_time;
    double length;
    public Request(char type,int id) {
        this.type = type;
        this.id = id;
    }
}
