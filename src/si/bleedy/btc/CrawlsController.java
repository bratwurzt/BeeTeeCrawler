package si.bleedy.btc;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import si.bleedy.btc.crawlers.BitsnoopMagnetCrawler;
import si.bleedy.btc.crawlers.PirateBayMagnetCrawler;

/**
 * @author bratwurzt
 */
public class CrawlsController
{
  private String m_rootFolder;
  private int m_numOfMagnetsToRead;
  private CrawlController m_bsController, m_pbController;
  private BeeTeeMagnet m_parent;

  public CrawlsController(BeeTeeMagnet parent, String rootFolder, int numOfMagnetsToRead) throws Exception
  {
    m_rootFolder = rootFolder;
    m_numOfMagnetsToRead = numOfMagnetsToRead;
    m_parent = parent;
    init();
  }

  private void init() throws Exception
  {
    String[] bitsnoopDomains = new String[]{"http://bitsnoop.com/"};
    String bsCrawlStorageFolder = m_rootFolder + "/bitsnoop";
    CrawlConfig bsConfig = new CrawlConfig();
    bsConfig.setCrawlStorageFolder(bsCrawlStorageFolder);
    bsConfig.setIncludeBinaryContentInCrawling(false);
    bsConfig.setResumableCrawling(false);
    PageFetcher bsPageFetcher = new PageFetcher(bsConfig);
    RobotstxtServer bsRobotstxtServer = new RobotstxtServer(new RobotstxtConfig(), bsPageFetcher);

    BitsnoopMagnetCrawler.configure(m_parent, bitsnoopDomains, bsCrawlStorageFolder, m_numOfMagnetsToRead);
    m_bsController = new CrawlController(bsConfig, bsPageFetcher, bsRobotstxtServer);
    m_bsController.setCustomData(bitsnoopDomains);
    for (String domain : bitsnoopDomains)
    {
      m_bsController.addSeed(domain);
    }
    m_bsController.addSeed("http://bitsnoop.com/browse");

    String[] piratebayDomains = new String[]{"http://thepiratebay.sx/"};
    String pbCrawlStorageFolder = m_rootFolder + "/piratebay";
    CrawlConfig pbConfig = new CrawlConfig();
    pbConfig.setCrawlStorageFolder(pbCrawlStorageFolder);
    pbConfig.setIncludeBinaryContentInCrawling(false);
    pbConfig.setResumableCrawling(false);
    PageFetcher pbPageFetcher = new PageFetcher(pbConfig);
    RobotstxtServer pbRobotstxtServer = new RobotstxtServer(new RobotstxtConfig(), pbPageFetcher);

    PirateBayMagnetCrawler.configure(m_parent, piratebayDomains, pbCrawlStorageFolder, m_numOfMagnetsToRead);
    m_pbController = new CrawlController(pbConfig, pbPageFetcher, pbRobotstxtServer);
    m_pbController.setCustomData(piratebayDomains);
    for (String domain : piratebayDomains)
    {
      m_pbController.addSeed(domain);
    }
    m_pbController.addSeed("http://thepiratebay.sx/search/");
  }

  public void start()
  {
    m_bsController.startNonBlocking(BitsnoopMagnetCrawler.class, 1);
    m_pbController.startNonBlocking(PirateBayMagnetCrawler.class, 1);
    m_bsController.waitUntilFinish();
    m_pbController.waitUntilFinish();
  }
}
