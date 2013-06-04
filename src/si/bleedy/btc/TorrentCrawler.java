package si.bleedy.btc;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

/**
 * @author DusanM
 */
public class TorrentCrawler extends WebCrawler
{
  private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");

  private static File storageFolder;
  private static String[] crawlDomains;
  private static BeeTeeMagnet m_parent;
  private static int m_numOfMagnetsToRead;

  public static void configure(BeeTeeMagnet parent, String[] crawlDomains, String storageFolderName, int numOfMagnetsToRead)
  {
    m_parent = parent;
    m_numOfMagnetsToRead = numOfMagnetsToRead;
    TorrentCrawler.crawlDomains = crawlDomains;

    storageFolder = new File(storageFolderName);
    if (!storageFolder.exists())
    {
      storageFolder.mkdirs();
    }
  }

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
        if (!m_parent.getMagnetLinks().containsKey(magnet.getBtih())
            || m_parent.getMagnetLinks().get(magnet.getBtih()).getDisplayName().length() < magnet.getDisplayName().length())
        {
          m_parent.getMagnetLinks().put(magnet.getBtih(), magnet);
        }
      }
    }

    if (m_parent.getMagnetLinks().size() >= m_numOfMagnetsToRead)
    {
      getMyController().shutdown();
    }
  }
}
