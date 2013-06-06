package si.bleedy.btc;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author bratwurzt
 */
public class MagnetURI
{
  private Long m_exactLength;
  private String m_exactSource;
  private String m_acceptableSource;
  private String m_btih;
  private List<URI> m_addressTracker = null;
  private String m_keywordTopic;
  private String m_manifestTopic;
  private String m_displayName;
  private String m_seeders;
  private String m_leechers;

  public MagnetURI(final String btih, final String displayName, final String addressTracker, final String seeders, final String leechers)
  {
    m_btih = btih;
    m_displayName = displayName;
    setAddressTrackerList(addressTracker);
    m_seeders = seeders;
    m_leechers = leechers;
  }

  public MagnetURI(String magnetLink)
  {
    StringTokenizer st, keyPair;
    magnetLink = magnetLink.substring("magnet:?".length());
    st = new StringTokenizer(magnetLink, "&");
    while (st.hasMoreTokens())
    {
      String value = st.nextToken();
      keyPair = new StringTokenizer(value, "=");
      while (keyPair.hasMoreTokens())
      {
        String key = keyPair.nextToken();
        String val = keyPair.nextToken();
        try
        {
          setParameter(key, val);
        }
        catch (UnsupportedEncodingException e)
        {
          e.printStackTrace();
        }
      }
    }
  }

  private void setParameter(final String key, final String val) throws UnsupportedEncodingException
  {
    if ("xt".equals(key))     // eXactTopic
    {
      m_btih = val.substring("urn:btih:".length());
    }
    else if ("dn".equals(key))// DisplayName
    {
      m_displayName = URLDecoder.decode(val, "UTF-8");
    }
    else if ("tr".equals(key))// address TRacker
    {
      if (m_addressTracker == null)
      {
        m_addressTracker = new ArrayList<URI>();
      }
      m_addressTracker.add(URI.create(URLDecoder.decode(val, "UTF-8")));
    }
    else if ("xl".equals(key))// eXact Length
    {
      m_exactLength = Long.parseLong(val);
    }
    else if ("as".equals(key))// Acceptable Source
    {
      m_acceptableSource = URLDecoder.decode(val, "UTF-8");
    }
    else if ("xs".equals(key))// eXact Source
    {
      m_exactSource = URLDecoder.decode(val, "UTF-8");
    }
    else if ("kt".equals(key))// Keyword Topic
    {
      m_keywordTopic = val;
    }
    else if ("mt".equals(key))// ManifesT
    {
      m_manifestTopic = val;
    }
  }

  public String getHref() throws UnsupportedEncodingException
  {
    StringBuilder builder = new StringBuilder("magnet:?");
    if (m_btih != null)     // eXactTopic
    {
      builder.append("xt=urn:btih:").append(m_btih);
    }
    if (m_displayName != null)     // DisplayName
    {
      builder.append("&dn=").append(URLEncoder.encode(m_displayName, "UTF-8"));
    }
    if (m_addressTracker != null && m_addressTracker.size() > 0)     // address TRacker
    {
      for (URI uri : m_addressTracker)
      {
        builder.append("&tr=").append(URLEncoder.encode(uri.toString(), "UTF-8"));
      }
    }
    if (m_exactLength != null)     // eXact Length
    {
      builder.append("&xl=").append(m_exactLength);
    }
    if (m_acceptableSource != null)     // Acceptable Source
    {
      builder.append("&as=").append(URLEncoder.encode(m_acceptableSource, "UTF-8"));
    }
    if (m_exactSource != null)     // eXact Source
    {
      builder.append("&xs=").append(URLEncoder.encode(m_exactSource, "UTF-8"));
    }
    if (m_keywordTopic != null)     // Keyword Topic
    {
      builder.append("&kt=").append(m_keywordTopic);
    }
    if (m_manifestTopic != null)     // ManifesT
    {
      builder.append("&mt=").append(m_manifestTopic);
    }

    return builder.toString();
  }

  public Long getExactLength()
  {
    return m_exactLength;
  }

  public String getExactSource()
  {
    return m_exactSource;
  }

  public String getAcceptableSource()
  {
    return m_acceptableSource;
  }

  public String getBtih()
  {
    return m_btih;
  }

  public List<URI> getAddressTracker()
  {
    return m_addressTracker;
  }

  public String getAddressTrackerList()
  {
    StringBuilder buff = new StringBuilder();
    if (m_addressTracker != null)
    {
      for (URI tracker : m_addressTracker)
      {
        buff.append(tracker);
        buff.append(";");
      }
    }

    return buff.toString();
  }

  public void setAddressTrackerList(String lista)
  {
    if (m_addressTracker == null)
    {
      m_addressTracker = new ArrayList<URI>();
    }
    StringTokenizer tokenizer = new StringTokenizer(lista, ";");
    while (tokenizer.hasMoreTokens())
    {
      m_addressTracker.add(URI.create(tokenizer.nextToken()));
    }
  }

  public String getKeywordTopic()
  {
    return m_keywordTopic;
  }

  public String getManifestTopic()
  {
    return m_manifestTopic;
  }

  public String getDisplayName()
  {
    return m_displayName;
  }

  public String getSeeders()
  {
    return m_seeders;
  }

  public void setSeeders(String seeders)
  {
    m_seeders = seeders;
  }

  public String getLeechers()
  {
    return m_leechers;
  }

  public void setLeechers(String leechers)
  {
    m_leechers = leechers;
  }

  @Override
  public boolean equals(Object obj)
  {
    MagnetURI o = (MagnetURI) obj;
    try
    {
      return getHref().equals(o.getHref());
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace();
    }
    return false;
  }


}
