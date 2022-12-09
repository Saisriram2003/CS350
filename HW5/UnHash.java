package HW5;

import java.security.NoSuchAlgorithmException;

public class UnHash {

    public static void main(String args[]) throws NoSuchAlgorithmException {
        String hash = args[0];
        UnHash unhasher = new UnHash();
        System.out.println(unhasher.unhash(hash));

        
    }
    public int unhash (String to_val) throws NoSuchAlgorithmException
    {
        Hash hasher = new Hash();
        for(int i = 0;; ++i) {
            String tmp = hasher.hash(i);

            if(tmp.equals(to_val))
                return i;
        }
    }
    
}
