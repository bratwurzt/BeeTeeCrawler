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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import si.bleedy.btc.BeeTeeMagnet;
import si.bleedy.btc.MagnetURI;

/**
 * @author bratwurzt
 */
public class BitsnoopMagnetCrawler extends WebCrawler
{
  protected static Map<String, MagnetURI> m_crawlMap;
  private final static Pattern FILTERS = Pattern
      .compile(".*(\\.(txt|css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");

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
        return !href.startsWith(domain + "search/") && !href.startsWith(domain + "tv/series/");
      }
    }
    return false;
  }

  public static void configure(BeeTeeMagnet parent, String[] crawlDomains, String storageFolderName, int numOfMagnetsToRead)
  {
    m_parent = parent;
    m_numOfMagnetsToRead = numOfMagnetsToRead;
    BitsnoopMagnetCrawler.crawlDomains = crawlDomains;
    m_crawlMap = new HashMap<String, MagnetURI>();
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
        MagnetURI magnet = new MagnetURI(magnetLink);
        Element seeders = doc.select("[title^=seed]").first();
        Element leechers = doc.select("[title^=leech]").first();
        if (seeders != null)
        {
          magnet.setSeeders(seeders.text());
        }
        if (leechers != null)
        {
          magnet.setLeechers(leechers.text());
        }
        if (!m_parent.getMagnetLinks().containsKey(magnet.getBtih()) && !m_crawlMap.containsKey(magnet.getBtih()))
        {
          m_crawlMap.put(magnet.getBtih(), magnet);
        }
      }

      if (m_crawlMap.size() >= m_numOfMagnetsToRead)
      {
        m_parent.getMagnetLinks().putAll(m_crawlMap);
        getMyController().shutdown();
      }
    }
  }
}
