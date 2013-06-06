package si.bleedy.btc.crawlers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import si.bleedy.btc.BeeTeeMagnet;
import si.bleedy.btc.MagnetURI;

/**
 * @author bratwurzt
 */
public abstract class TorrentCrawler extends WebCrawler
{
  private final static Pattern FILTERS = Pattern.compile(".*(\\.(txt|css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");

  protected static File storageFolder;
  protected static String[] crawlDomains;
  protected static BeeTeeMagnet m_parent;
  protected static int m_numOfMagnetsToRead;

  @Override
  public boolean shouldVisit(WebURL url)
  {
    String href = url.getURL().toLowerCase();

    if (FILTERS.matcher(href).matches())
    {
      return false;
    }

    for (String domain : crawlDomains)
    {
      if (href.startsWith(domain))
      {
        return true;
      }
    }
    return false;
  }
}
