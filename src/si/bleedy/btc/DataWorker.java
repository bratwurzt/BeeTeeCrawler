package si.bleedy.btc;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author DusanM
 */
public class DataWorker
{
  private static Connection m_con;

  public DataWorker()
  {
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
        stat.executeUpdate("create table if not exists magnets (btih, display_name, trackers, seeders, leechers);");
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

  public Map<String, MagnetURI> readBeeTeeDB() throws SQLException
  {
    Map<String, MagnetURI> magnetLinks = new HashMap<String, MagnetURI>();
    Statement stat = m_con.createStatement();
    try
    {
      stat.executeUpdate("create table if not exists magnets (btih, display_name, trackers, seeders, leechers);");
      ResultSet resultSet = stat.executeQuery("SELECT btih, display_name, trackers, seeders, leechers FROM magnets");
      try
      {
        while (resultSet.next())
        {
          MagnetURI magnetLink = new MagnetURI(
              resultSet.getString("btih"),
              resultSet.getString("display_name"),
              resultSet.getString("trackers"),
              resultSet.getString("seeders"),
              resultSet.getString("leechers")
          );
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
      PreparedStatement prep = m_con.prepareStatement("insert into magnets values (?, ?, ?, ?, ?);");
      try
      {
        m_con.setAutoCommit(false);
        for (Map.Entry<String, MagnetURI> magnetLink : magnetLinks.entrySet())
        {
          MagnetURI mgl = magnetLink.getValue();
          prep.setString(1, mgl.getBtih());
          prep.setString(2, mgl.getDisplayName());
          prep.setString(3, mgl.getAddressTrackerList());
          prep.setString(4, mgl.getSeeders());
          prep.setString(5, mgl.getLeechers());
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
}
