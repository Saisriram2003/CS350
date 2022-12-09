package HW6;

import java.security.NoSuchAlgorithmException;

public class UnHash extends Thread{
    // This is a worker

    int timeout;
    double startTime;
    boolean ended = false;

    String hash = null;

    public static void main(String args[]) throws NoSuchAlgorithmException {
        String hash = args[0];
        UnHash unhasher = new UnHash();
        System.out.println(unhasher.unhash(hash));

        
    }
    public void addWork(String hash){
        this.startTime = System.nanoTime();
        this.hash = hash;
    }
    public boolean idle(){
        return (this.hash == null);
    }
    public void removeWork(){
        this.ended = true;
    }

    public void run(){
        try{
            while (!this.ended || this.hash != null){

                if (this.hash != null){
                    unhash(hash);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public int unhash (String to_val) throws NoSuchAlgorithmException
    {
        Hash hasher = new Hash();
        for(int i = 0;;++i) {
            if (this.timeout > 0 && (System.nanoTime()-this.startTime)/1e6 > this.timeout){
                System.out.println(hash);
                this.addWork(null);
                return i;
            }
            String tmp = hasher.hash(i);
            if(tmp.equals(to_val)){
                this.addWork(null);
                System.out.println(i);
                return i;
            }

        }
    }
    
}
