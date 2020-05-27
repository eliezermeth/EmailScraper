import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

/**
 * @author Eliezer Meth
 * @version 1
 * Start Date: 05.17.2020
 */

public class ReadWebJSoup
{
    private ArrayList<String> links = new ArrayList<>();
    private int upToLink = 0;

    private Set<String> emails = new HashSet<>(100);

    //*************************************************************
    public static void main(String[] args)throws IOException
    {
        String url = "https://adobe.com";

        ReadWebJSoup read = new ReadWebJSoup();
        read.harvest(url);
        read.printResults();
    }
    //*************************************************************

    public void harvest(String url)
    {
        String html = "";
        try {
            html = Jsoup.connect(url).get().html();
        } catch (HttpStatusException e) {
            System.out.println("*** ERROR 1 ***");
            //e.printStackTrace();
            System.out.println("Continue.");
        } catch (IOException e) {
            System.out.println("*** ERROR 2 ***");
            //e.printStackTrace();
            System.out.println("Continue.");
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

        ArrayList<String> pageEmails = RegexEmail.findEmails(html);
        if (pageEmails.size() > 0)
            emails.addAll(pageEmails);

        // recursive code
        if (emails.size() < 50 && links.size() > upToLink)
        {
            harvest(links.get(upToLink++));
        }

    }

    public void printResults()
    {
        System.out.println("Finished with upToLink at " + upToLink);
        System.out.println("Total links: " + links.size());
        System.out.println("Total emails: " + emails.size());
        System.out.println();

        for (int i = 0; i < links.size(); i++)
        {
            System.out.println("LINK: " + links.get(i));
        }

        Iterator<String> iterator = emails.iterator();
        while (iterator.hasNext())
        {
            System.out.println("EMAIL: " + iterator.next());
        }
    }
}
