package HW5;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;
import java.nio.file.*;

public class Dispatcher {
    public static void main(String args[]) throws NoSuchAlgorithmException {
        String filepath = args[0];
        File f = new File(filepath);

        UnHash unhasher = new UnHash();


        try (Stream<String> linesStream = Files.lines(f.toPath())) {
            linesStream.forEach(line -> {
                try {
                    System.out.println(unhasher.unhash(line));
                } catch (NoSuchAlgorithmException e) {
                    System.out.println("Unhasher did not work");
                }
            });
        }
        catch(Exception e){
            
        }

        
    }
    
}
