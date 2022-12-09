package HW1;

public class Request {
    char type;
    int id;
    double runs = 1;

    double s0_original_arrival_time;
    double s0_arrival_time;
    double s0_start_time;
    double s0_finish_time;

    double s1_original_arrival_time = -1;
    double s1_arrival_time;
    double s1_start_time;
    double s1_finish_time;

    double s2_original_arrival_time = -1;
    double s2_arrival_time;
    double s2_start_time;
    double s2_finish_time;

    double length;
    int server;
    public Request(char type,int id) {
        this.type = type;
        this.id = id;
    }
}
