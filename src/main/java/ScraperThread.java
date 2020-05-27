import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Eliezer Meth
 * @version 1
 * Start Date: 05.17.2020
 */

public class ScraperThread implements Runnable
{
    private String url;

    private Set<String> emails = new HashSet<>(100);

    private ArrayList<String> links = new ArrayList<>(100);
    private int upToLink = 0;

    public ScraperThread(String url)
    {
        this.url = url;
    }

    public void run()
    {
        harvest(url);
    }

    public Collection[] harvest(String url)
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
            emails.addAll(pageEmails);

        ArrayList<String> pageLinks = RegexEmail.findWebsites(html);
        if (pageLinks.size() > 0)
        {
            for (int i = 0; i < pageLinks.size(); i++)
            {
                if (!links.contains(pageLinks.get((i))))
                    links.add(pageLinks.get(i));
            }
        }

        // recursive code
        if (emails.size() < 100 && links.size() < 100 && links.size() > upToLink)
        {
            harvest(links.get(upToLink++));
        }

        // reached when 100 of emails or links to dump, or no links left to check
        Collection[] produce = new Collection[2];
        produce[0] = emails;
        produce[1] = links;
        return produce;
    }
}
