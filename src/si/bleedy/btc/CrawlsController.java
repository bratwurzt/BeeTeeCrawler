package si.bleedy.btc;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * @author DusanM
 */
public class CrawlsController
{
  private String m_rootFolder;
  private String m_storageFolder;
  private int m_numOfMagnetsToRead;
  private CrawlController m_controller;
  private BeeTeeMagnet m_parent;

  public CrawlsController(BeeTeeMagnet parent, String rootFolder, String storageFolder, int numOfMagnetsToRead, String[] crawlDomains) throws Exception
  {
    m_rootFolder = rootFolder;
    m_storageFolder = storageFolder;
    m_numOfMagnetsToRead = numOfMagnetsToRead;
    m_parent = parent;
    init(crawlDomains);
  }

  private void init(String[] crawlDomains) throws Exception
  {
    CrawlConfig config = new CrawlConfig();

    config.setCrawlStorageFolder(m_rootFolder);
    config.setIncludeBinaryContentInCrawling(false);
    config.setResumableCrawling(false);

    PageFetcher pageFetcher = new PageFetcher(config);
    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
    TorrentCrawler.configure(m_parent, crawlDomains, m_storageFolder, m_numOfMagnetsToRead);
    m_controller = new CrawlController(config, pageFetcher, robotstxtServer);
    for (String domain : crawlDomains)
    {
      m_controller.addSeed(domain);
    }
  }

  public void start()
  {
    m_controller.start(TorrentCrawler.class, 1);
  }
}
