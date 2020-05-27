import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Eliezer Meth
 * @version 1
 * Start Date: 05.26.2020
 */

public class WriteToFile
{
    public static void writeCollection(String filename, Collection col)
    {
        // file operations
        try (BufferedWriter destination = new BufferedWriter(new FileWriter(filename)))
        {
            String line;
            Iterator entry = col.iterator();

            while (entry.hasNext())
            {
                destination.write(entry.next() + "\n");
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
