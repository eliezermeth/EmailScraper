import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static jdk.nashorn.internal.objects.NativeDate.getTime;

/**
 * @author Eliezer Meth
 * @version 2
 * Start Date: 05.27.2020
 */

public class Final
{
    long startTime;
    long endTime;

    static final int TOTAL_EMAILS = 10000;
    static final int NUM_THREADS = 100;

    String startingURL = "http://touro.edu";

    List<String> synsetToVisit;
    Set<String> synsetVisited;
    Set<String> synsetEmails; // make 26 array of sorted synset

    List<String> synDomName;
    List<Integer> synDomNum;

    //*****************************************************************
    public static void main(String[] args) { new Final(); }
    //*****************************************************************

    public Final()
    {
        startTime = System.currentTimeMillis();
        System.out.println("StartTime: " + getTime(startTime));

        synsetToVisit = Collections.synchronizedList(new LinkedList<String>());
        synsetVisited = Collections.synchronizedSet(new HashSet<String>());
        synsetEmails = Collections.synchronizedSet(new HashSet<String>());

        synDomName = Collections.synchronizedList(new ArrayList<String>());
        synDomNum = Collections.synchronizedList(new ArrayList<Integer>());

        synsetToVisit.add(startingURL); // should add to DomName and DomNum

        runThreads();
    }

    private void runThreads()
    {
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);

        while (synsetEmails.size() < TOTAL_EMAILS) // && synsetToVisit.size() != 0
        {
            if (synsetToVisit.size() > 0)
                pool.execute(new FinalThreadInternal(popURL()));
        }

        pool.shutdown(); // if shutdownNow; System.exit(0) will not shut down program

        endTime = System.currentTimeMillis();
        System.out.println("EndTime: " + getTime(endTime));

        synchronized (synsetEmails)
        {
            synchronized (synsetToVisit)
            {
                synchronized (synsetVisited)
                {
                    saveResults(synsetEmails, synsetVisited, synsetToVisit);
                }
            }
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

    private void saveResults(Collection colEmails, Collection colLinksVisited, Collection colLinksToVisit)
    {
        System.out.println("LINKS VISITED: " + synsetVisited.size());
        System.out.println("SAVED LINKS: " + synsetToVisit.size());
        System.out.println("EMAILS SAVED: " + colEmails.size());

        System.out.println("Time elapsed: " + getTime(endTime - startTime));

        System.out.print("Writing emails to file. . . ");
        WriteToFile.writeCollection("emailList.txt", colEmails);
        System.out.println("Completed");

        System.out.print("Writing links visited to file. . . ");
        WriteToFile.writeCollection("visitedList.txt", colLinksVisited);
        System.out.println("Completed");

        System.out.print("Writing links not visited to file. . . ");
        WriteToFile.writeCollection("notVisitedList.txt", colLinksToVisit);
        System.out.println("Completed");

        System.out.print("Writing emails to online database. . . ");
        ConnectURL.addEmails(colEmails);
        System.out.println("Completed");
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
    class FinalThreadInternal implements Runnable
    {
        private int MAX_PER_DOMAIN = 50;

        private Set<String> emails = new HashSet<>(20); // assume max 20 per site
        private ArrayList<String> links = new ArrayList<>(20);

        private String initialURL;

        public FinalThreadInternal(String url)
        {
            initialURL = url;
        }

        public void run()
        {
            System.out.println("STARTING " + initialURL);
            harvest(initialURL);
            synsetEmails.addAll(emails);

            // add harvested links to main program
            synchronized (synDomName)
            {
                for (String link : links)
                {
                    try
                    {
                        String hostURL = new URL(initialURL).getHost();
                        int index = synDomName.indexOf(hostURL);

                        if (index != -1) // host domain was already searched
                        {
                            synchronized (synDomNum)
                            {
                                int num = synDomNum.get(index);

                                if (num < MAX_PER_DOMAIN) // can add to links to be searched
                                {
                                    synchronized (synsetToVisit)
                                    {
                                        synsetToVisit.add(link);
                                        synDomNum.set(index, num + 1); // increment domain number
                                    }
                                }
                            }
                        }
                        else // host domain was not yet searched
                        {
                            synchronized (synDomName) // captured so index congruent
                            {
                                synchronized (synDomNum) // captured so index congruent
                                {
                                    synDomName.add(hostURL);
                                    synDomNum.add(1); // how many times hostURL added; same index as in synDomName
                                }
                            }
                        }
                    } catch (MalformedURLException e) // if link does not function as a URL
                    {
                        e.printStackTrace();
                    }
                }
            }

            // add harvested emails to main program
            if (emails.size() > 0)
            {
                // need loop to change emails to lowercase
                for (String email : emails)
                    synsetEmails.add(email.toLowerCase());
            }

            int total;
            synchronized (synsetEmails)
            {
                total = synsetEmails.size();
            }

            // print out finished
            String space = (emails.size() > 0) ? "\t" : "";
            System.out.println(space + "FINISHED " + initialURL + " with " + links.size() + ":" + emails.size() + " -> " + total);
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
                for (String poss : pageEmails)
                {
                    if (!(poss.substring(poss.length() - 4).equals(".png") ||
                            poss.substring(poss.length() - 4).equals(".jpg"))) // check that possible is not ".png" or ".jpg"
                        emails.add(poss);
                }
            }

            ArrayList<String> pageLinks = RegexEmail.findWebsites(html);
            if (pageLinks.size() > 0)
            {
                for (int i = 0; i < pageLinks.size(); i++)
                {
                    if (!links.contains(pageLinks.get((i))))
                        links.add(pageLinks.get(i));
                }
            }

            // return to run
        }
    }
}
