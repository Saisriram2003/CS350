package HW5;
import java.math.BigInteger;
import java.security.MessageDigest;

public class Hash {
    public String hash(int to_hash){
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            String val = Integer.toString(to_hash);
            byte[] digest = md.digest(val.getBytes());

            BigInteger intHash = new BigInteger(1, digest);
            String theHash = intHash.toString(16);
            while (theHash.length() < 32) {
                theHash = "0" + theHash;
            }
            
            return theHash;
        }
        catch(Exception e){
            System.out.println("The hash did not work");

        }
        return "";
    }
}