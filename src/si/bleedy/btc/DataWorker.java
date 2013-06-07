package si.bleedy.btc;

import java.sql.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bratwurzt
 */
public class DataWorker
{
  private static Connection m_con;
  public static NumberFormat m_numberFormat;

  public DataWorker()
  {
    m_numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
  }

  public void initDataConnection(String databaseFilename)
  {
    try
    {
      if (m_con != null)
      {
        m_con.close();
      }
      Class.forName("org.sqlite.JDBC");
      m_con = DriverManager.getConnection("jdbc:sqlite:" + databaseFilename);
      Statement stat = m_con.createStatement();
      try
      {
        stat.executeUpdate("create table if not exists magnets (btih, display_name, trackers, seeders, leechers, source);");
      }
      finally
      {
        stat.close();
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
  }

  public ConcurrentHashMap<String, MagnetURI> readBeeTeeDB() throws SQLException, ParseException
  {
    ConcurrentHashMap<String, MagnetURI> magnetLinks = new ConcurrentHashMap<String, MagnetURI>();
    Statement stat = m_con.createStatement();
    try
    {
      stat.executeUpdate("create table if not exists magnets (btih, display_name, trackers, seeders, leechers, source);");
      ResultSet resultSet = stat.executeQuery("SELECT btih, display_name, trackers, seeders, leechers, source FROM magnets");
      try
      {
        while (resultSet.next())
        {
          MagnetURI magnetLink = new MagnetURI(
              resultSet.getString("btih"),
              resultSet.getString("display_name"),
              resultSet.getString("trackers"),
              resultSet.getInt("seeders"),
              resultSet.getInt("leechers"),
              resultSet.getString("source"));
          magnetLinks.put(magnetLink.getBtih(), magnetLink);
        }
      }
      finally
      {
        resultSet.close();
      }
    }
    finally
    {
      stat.close();
    }
    return magnetLinks;
  }

  public void saveMagnetLinks(Map<String, MagnetURI> magnetLinks)
  {
    try
    {
      PreparedStatement prep = m_con.prepareStatement("insert into magnets values (?, ?, ?, ?, ?, ?);");
      try
      {
        m_con.setAutoCommit(false);
        for (Map.Entry<String, MagnetURI> magnetLink : magnetLinks.entrySet())
        {
          MagnetURI mgl = magnetLink.getValue();
          prep.setString(1, mgl.getBtih());
          prep.setString(2, mgl.getDisplayName());
          prep.setString(3, mgl.getAddressTrackerList());
          prep.setInt(4, mgl.getSeeders());
          prep.setInt(5, mgl.getLeechers());
          prep.setString(6, mgl.getSourcePage());
          prep.addBatch();
        }
        prep.executeBatch();
      }
      finally
      {
        prep.close();
      }
      m_con.commit();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }

  public static NumberFormat getNumberFormat()
  {
    return m_numberFormat;
  }
}
