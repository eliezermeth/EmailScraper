import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eliezer Meth
 * @version 1
 * Start Date: 5.17.2020
 * Code copied and modified from RegexTestHarness
 */

public class RegexEmail
{

    public static ArrayList<String> findEmails(String html)
    {
        // in a url, an @ symbol will appear as %40
        // email may not appear as an href
        String emailRegex = "([A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4})";

        return findRegex(emailRegex, html);
    }

    public static ArrayList<String> findWebsites(String html)
    {
        String webRegex = "<a href=\"(http\\S+)\">"; // can be http or https; should this be in href?
        String webSpecificRegex = "https?://(www\\.)?[A-Za-z0-9]+\\.(com|org|edu|gov|us)/?.*"; // only works for .com, .org, etc.

        return findRegex(webRegex, html);
    }

    private static ArrayList<String> findRegex(String regex, String line)
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        return findRegexUtil(matcher);
    }

    private static ArrayList<String> findRegexUtil(Matcher matcher)
    {
        ArrayList<String> links = new ArrayList<>();

        while (matcher.find())
        {
            if (matcher.groupCount() > 0) // found capturing groups; #1 will be targeted
            {
                links.add(matcher.group(1)); // add link to arraylist
            }
            // end added for groups
        }

        return links;
    }
}
// does an ArrayList return work well?
// how to save url links to same website domain but different page that do not start with http

/*

    \s matches any white-space character
    \S matches any non-white-space character
    You can match a space character with just the space character;
    [^ ] matches anything but a space character.

 */