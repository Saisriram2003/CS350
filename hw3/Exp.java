package HW1;
import java.lang.Math;
public class Exp {
    public static void main(String args[]){
        double lambda = Double.parseDouble(args[0]);
        int n = Integer.parseInt(args[1]);
        Exp exp = new Exp();
        for (int i = 0; i < n; i++){
            double x = exp.getExp(lambda);
            System.out.println(x);
        }
    }
    public double getExp(double lambda){
        double Y = Math.random();
        double x =(-Math.log(1-Y))/lambda;
        return x;
    }
}
