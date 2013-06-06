package si.bleedy.btc.crawlers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import si.bleedy.btc.BeeTeeMagnet;
import si.bleedy.btc.MagnetURI;

/**
 * @author bratwurzt
 */
public class PirateBayMagnetCrawler extends TorrentCrawler
{
  protected static Map<String, MagnetURI> m_crawlMap;

  public static void configure(BeeTeeMagnet parent, String[] crawlDomains, String storageFolderName, int numOfMagnetsToRead)
  {
    m_parent = parent;
    m_numOfMagnetsToRead = numOfMagnetsToRead;
    PirateBayMagnetCrawler.crawlDomains = crawlDomains;
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
        Elements areSearchResults = doc.select("table#searchResult");
        if (areSearchResults.size() > 0)
        {
          Elements seedPeer = link.parents().select("> td[align=right]");
          Element seeders = seedPeer.first();
          Element leechers = seedPeer.last();
          if (seeders != null)
          {
            magnet.setSeeders(seeders.text());
          }
          else
          {
            System.out.println();
          }
          if (leechers != null)
          {
            magnet.setLeechers(leechers.text());
          }
          else
          {
            System.out.println();
          }
          if (!m_parent.getMagnetLinks().containsKey(magnet.getBtih())
              || m_parent.getMagnetLinks().get(magnet.getBtih()).getDisplayName().length() < magnet.getDisplayName().length())
          {
            m_crawlMap.put(magnet.getBtih(), magnet);
          }
        }
      }
    }

    if (m_crawlMap.size() >= m_numOfMagnetsToRead)
    {
      m_parent.getMagnetLinks().putAll(m_crawlMap);
      getMyController().shutdown();
    }
  }
}
