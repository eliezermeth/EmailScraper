import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Eliezer Meth
 * @version 1
 * Start Date: 05.17.2020
 * Last Modified: 05.26.2020
 */

public class SingleFileScraper
{
    long startTime;
    long endTime;

    static final int TOTAL_EMAILS = 10000;
    static final int NUM_THREADS = 100;

    String startingURL = "http://touro.edu";

    List<String> synsetToVisit;
    Set<String> synsetVisited;
    Set<String> synsetEmails; // make 26 array of sorted synset

    //*****************************************************************
    public static void main(String[] args) { new SingleFileScraper(); }
    //*****************************************************************

    public SingleFileScraper()
    {
        startTime = System.currentTimeMillis();
        System.out.println("StartTime: " + getTime(startTime));

        synsetToVisit = Collections.synchronizedList(new LinkedList<String>());
        synsetVisited = Collections.synchronizedSet(new HashSet<String>());
        synsetEmails = Collections.synchronizedSet(new HashSet<String>());

        synsetToVisit.add(startingURL);

        runThreads();
    }

    private void runThreads()
    {
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);

        while (synsetEmails.size() < TOTAL_EMAILS) // && synsetToVisit.size() != 0
        {
            if (synsetToVisit.size() > 0)
                pool.execute(new ScraperThreadInternal(popURL()));
        }

        pool.shutdown(); // if shutdownNow; System.exit(0) will not shut down program

        endTime = System.currentTimeMillis();
        System.out.println("EndTime: " + getTime(endTime));

        synchronized (synsetEmails)
        {
            printResults(synsetEmails);
        }

        System.exit(0); // shouldn't have to do this; fix
    }

    private String popURL()
    {
        String url;
        synchronized (synsetToVisit)
        {
            url = synsetToVisit.get(0);
            synsetToVisit.remove(0);
            synsetVisited.add(url);
        }
        return url;
    }

    private void printResults(Collection col)
    {
        Iterator<String> emails = col.iterator();
        while (emails.hasNext())
        {
            System.out.println("EMAIL: " + emails.next());
        }

        System.out.println("LINKS VISITED: " + synsetVisited.size());
        System.out.println("SAVED LINKS: " + synsetToVisit.size());
        System.out.println("EMAILS SAVED: " + col.size());

        System.out.println("Time elapsed: " + getTime(endTime - startTime));

        WriteToFile.writeCollection("emailList.txt", col);
        System.out.println("Wrote emails to file.");

        ConnectURL.addEmails(col);
        System.out.println("Added emails to database.");
    }

    private String getTime(long time)
    {
        time = time / 1000;
        long sec = time % 60; long temp = time / 60;
        long min = temp % 60; temp = temp / 60;
        long hour = temp % 60; temp = temp / 60;
        return hour + ":" + min + ":" + sec;
    }

    // worker thread ---------------------------------------------------------------------------------------------------
    class ScraperThreadInternal implements Runnable
    {
        private int MAX_EMAIL_SIZE = 50;

        private Set<String> emails = new HashSet<>(MAX_EMAIL_SIZE + 20);

        private ArrayList<String> links = new ArrayList<>(100);
        private ArrayList<String> visited = new ArrayList<>(100);

        private String initialURL;

        public ScraperThreadInternal(String url)
        {
            links.add(url);
            initialURL = url;
        }

        public void run()
        {
            System.out.println("STARTING " + initialURL);
            harvest(nextURL());
            synsetEmails.addAll(emails);
            synsetVisited.addAll(visited);

            synchronized (synsetToVisit)
            {
                for (int i = 0; i < links.size(); i++)
                {
                    if (!synsetToVisit.contains(links.get(i)))
                        synsetToVisit.add(links.get(i));
                }
            }

            int num;
            synchronized (synsetEmails)
            {
                num = synsetEmails.size();
            }

            String space = (emails.size() > 0) ? "\t" : "";
            System.out.println(space + "FINISHED " + initialURL + " with " + visited.size() + ":" +
                    links.size() + ":" + emails.size() + " -> " + num);
        }

        public String nextURL()
        {
            String url = links.get(0);
            links.remove(0);
            visited.add(url);
            return url;
        }

        public void harvest(String url)
        {
            String html = "";

            try {
                html = Jsoup.connect(url).get().html();
            } catch (HttpStatusException e) {
                // do nothing
            } catch (IOException e) {
                // do nothing
            }

            ArrayList<String> pageEmails = RegexEmail.findEmails(html);
            if (pageEmails.size() > 0)
            {
                for (String poss : pageEmails) {
                    if (!(poss.substring(poss.length() - 4).equals(".png") ||
                            poss.substring(poss.length() - 4).equals(".jpg"))) // check that possible is not ".png" or ".jpg"
                        emails.add(poss);
                }
                emails.addAll(pageEmails);
            }

            ArrayList<String> pageLinks = RegexEmail.findWebsites(html);
            if (pageLinks.size() > 0)
            {
                for (int i = 0; i < pageLinks.size(); i++)
                {
                    if (!links.contains(pageLinks.get((i))) && !visited.contains(pageLinks.get(i)))
                        links.add(pageLinks.get(i));
                }
            }

            // recursive code
            if (emails.size() < MAX_EMAIL_SIZE && links.size() > 0)
            {
                harvest(nextURL());
            }

            // when MAX_EMAIL_SIZE reached, or no links left to check, return to run
        }
    }
}