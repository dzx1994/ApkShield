import com.googlecode.dex2jar.reader.DexFileReader;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Created by nnn7h on 17-6-1.
 */
public class TestString {
    @Test
    public void testStrings(){
        File dexFile = new File("input/classes.dex");
        try {
            InputStream is = new FileInputStream(dexFile);
            DexFileReader reader = new DexFileReader(is);
            Set<String> allStrings = reader.loadStrings();
            for (String s : allStrings){
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
