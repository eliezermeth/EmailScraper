import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Eliezer Meth
 * @version 1
 * Start Date: 05.17.2020
 */

public class EmailScraper
{
    static final int TOTAL_EMAILS = 500;
    static final int NUM_THREADS = 20;

    String startingURL = "http://touro.edu";

    Set<String> synsetToVisit;
    Set<String> synsetVisited;
    Set<String> synsetEmails; // make 26 array of sorted synset

    public EmailScraper()
    {
        synsetToVisit = Collections.synchronizedSet(new HashSet<String>());
        synsetVisited = Collections.synchronizedSet(new HashSet<String>());
        synsetEmails = Collections.synchronizedSet(new HashSet<String>());

        synsetToVisit.add(startingURL);
    }
}
