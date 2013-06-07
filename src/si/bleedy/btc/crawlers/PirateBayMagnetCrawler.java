package si.bleedy.btc.crawlers;

import java.io.File;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import si.bleedy.btc.BeeTeeMagnet;
import si.bleedy.btc.DataWorker;
import si.bleedy.btc.MagnetURI;

/**
 * @author bratwurzt
 */
public class PirateBayMagnetCrawler extends WebCrawler
{
  private final static Pattern FILTERS =
      Pattern.compile(".*(\\.(txt|css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");
  protected static int m_currentSize;
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

  public static void configure(BeeTeeMagnet parent, String[] crawlDomains, String storageFolderName, int numOfMagnetsToRead)
  {
    m_parent = parent;
    m_numOfMagnetsToRead = numOfMagnetsToRead;
    PirateBayMagnetCrawler.crawlDomains = crawlDomains;
    m_currentSize = 0;
    storageFolder = new File(storageFolderName);
    if (!storageFolder.exists())
    {
      storageFolder.mkdirs();
    }
  }

  @Override
  public void visit(Page page)
  {
    if (!(page.getParseData() instanceof HtmlParseData))
    {
      return;
    }

    HtmlParseData htmlParseData = (HtmlParseData)page.getParseData();

    Document doc = Jsoup.parse(htmlParseData.getHtml());

    Elements links = doc.select("a[href^=magnet]");

    if (links.size() > 0)
    {
      for (Element link : links)
      {
        String magnetLink = link.attr("href");
        MagnetURI magnet = new MagnetURI(magnetLink, "piratebay.sx");
        Elements areSearchResults = doc.select("table#searchResult");
        if (areSearchResults.size() > 0)
        {
          Elements seedPeer = link.parents().select("> td[align=right]");
          Element seeders = seedPeer.first();
          Element leechers = seedPeer.last();
          try
          {
            if (seeders != null)
            {
              magnet.setSeeders(DataWorker.getNumberFormat().parse(seeders.text()).intValue());
            }
            if (leechers != null)
            {
              magnet.setLeechers(DataWorker.getNumberFormat().parse(leechers.text()).intValue());
            }
          }
          catch (ParseException e)
          {
            e.printStackTrace();
          }

          MagnetURI oldMagnet = m_parent.getMagnetLinks().putIfAbsent(magnet.getBtih(), magnet);
          if (oldMagnet != null)
          {
            if (oldMagnet.getSeeders() > magnet.getSeeders() && magnet.getSeeders() > 0)
            {
              m_parent.getMagnetLinks().put(magnet.getBtih(), magnet);
            }
          }
          else
          {
            m_currentSize++;
          }
        }
      }

      if (m_currentSize >= m_numOfMagnetsToRead)
      {
        getMyController().shutdown();
      }
    }
  }
}
