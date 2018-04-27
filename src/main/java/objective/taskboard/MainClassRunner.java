package objective.taskboard;

import java.util.Arrays;

public class MainClassRunner {

    public static void main(String[] args) {
        if ( !isCacheGenerate(args) ) {
            Application.main( args );
        } else {
            CacheGenerate.main( args );
        }
    }
    private static Boolean isCacheGenerate(String[] args) {
        return Arrays.stream(args).anyMatch(s -> s.equals("--CacheGenerate"));
    }
}

