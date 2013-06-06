package si.bleedy.btc;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.table.TableRowSorter;

import si.bleedy.btc.uriSchemeHandler.CouldNotRegisterUriSchemeHandler;

/**
 * @author bratwurzt
 */
public class BeeTeeMagnet extends JFrame implements ActionListener
{
  private JMenuBar m_menuBar;
  private DataWorker m_dataWorker;
  public TableRowSorter<BeeTeeMagnetsTableModel> m_sorter;
  private JMenu m_mainMenu, m_submenu, m_recentlyMenu;
  private JMenuItem m_menuItem, m_recentlySubMenuItem;
  private JFileChooser m_fileChooser;
  private BeeTeeMagnetsTable m_mainTable;
  private JScrollPane m_scrollPane;
  private TreeSet m_recentlyOpenedFiles;
  public static int[] m_columnWidths = {80, 100, 20, 20, 100};
  private Map<String, MagnetURI> m_magnetLinks;
  private CrawlsController m_crawlsController;

  public BeeTeeMagnet()
      throws HeadlessException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException,
      CouldNotRegisterUriSchemeHandler
  {
    super("BeeTee");
    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
    m_magnetLinks = new ConcurrentHashMap<String, MagnetURI>();
    m_recentlyOpenedFiles = readRecentlyOpenedFiles();
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent we)
      {
        saveRecentlyOpenedFilenames(m_recentlyOpenedFiles);
      }
    });

    buildMenus();
    openDialog();
  }

  private void buildMenus()
  {
    m_fileChooser = new JFileChooser();
    //    m_fileChooser.addChoosableFileFilter(new DataFileFilter());
    m_menuBar = new JMenuBar();
    m_mainMenu = new JMenu("File");
    m_mainMenu.setMnemonic(KeyEvent.VK_F);
    m_menuBar.add(m_mainMenu);
    m_menuItem = new JMenuItem("Open...", KeyEvent.VK_O);
    m_menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK));
    m_menuItem.addActionListener(this);
    m_menuItem.setActionCommand("MENU.OPEN");
    m_mainMenu.add(m_menuItem);
    m_menuItem = new JMenuItem("Save...", KeyEvent.VK_S);
    m_menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_MASK));
    m_menuItem.addActionListener(this);
    m_menuItem.setActionCommand("MENU.SAVE");
    m_mainMenu.add(m_menuItem);
    m_menuItem = new JMenuItem("Close", KeyEvent.VK_C);
    m_menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK));
    m_menuItem.setActionCommand("MENU.CLOSE");
    m_menuItem.addActionListener(this);
    m_mainMenu.add(m_menuItem);
    m_mainMenu.addSeparator();
    m_recentlyMenu = new JMenu("Recently opened files...");
    m_recentlyMenu.setMnemonic(KeyEvent.VK_R);
    refreshRecentlyMenu();
    m_mainMenu.add(m_recentlyMenu);
    m_mainMenu.addSeparator();
    m_submenu = new JMenu("Crawl...");
    m_submenu.setMnemonic(KeyEvent.VK_T);
    for (int i = 1; i <= 10; i++)
    {
      m_menuItem = new JMenuItem(String.valueOf(i * 100), KeyEvent.VK_2);
      m_menuItem.setActionCommand("MENU.CRAWL");
      m_menuItem.addActionListener(this);
      m_submenu.add(m_menuItem);
    }

    m_menuItem = new JMenuItem("10000", KeyEvent.VK_2);
    m_menuItem.setActionCommand("MENU.CRAWL");
    m_menuItem.addActionListener(this);
    m_submenu.add(m_menuItem);

    m_menuItem = new JMenuItem("50000", KeyEvent.VK_2);
    m_menuItem.setActionCommand("MENU.CRAWL");
    m_menuItem.addActionListener(this);
    m_submenu.add(m_menuItem);

    m_mainMenu.add(m_submenu);
  }

  public void openDialog() throws CouldNotRegisterUriSchemeHandler
  {
    m_dataWorker = new DataWorker();

    setJMenuBar(m_menuBar);
    //    m_groupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    //    m_groupPanel.setBorder(BorderFactory.createTitledBorder("Groups"));
    //    m_groupPanel.setPreferredSize(new Dimension(1100, 80));

    m_mainTable = new BeeTeeMagnetsTable(new BeeTeeMagnetsTableModel(m_magnetLinks));
    m_mainTable.setDefaultRenderer(String.class, new CellRenderer());
    m_mainTable.setPreferredScrollableViewportSize(new Dimension(1200, 800));
    m_mainTable.setFillsViewportHeight(true);
    m_sorter = new TableRowSorter<BeeTeeMagnetsTableModel>((BeeTeeMagnetsTableModel)m_mainTable.getModel());
    m_mainTable.setRowSorter(m_sorter);

    m_scrollPane = new JScrollPane(m_mainTable);
    JPanel panel = new JPanel(new GridBagLayout());
    //    panel.add(m_groupPanel,
    //        new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    panel.add(m_scrollPane,
        new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    panel.setOpaque(true);
    add(panel, BorderLayout.CENTER);

    pack();
    setVisible(true);
    m_mainTable.setColumnWidths(m_columnWidths);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    String action = e.getActionCommand();
    if (action.equals("MENU.OPEN"))
    {
      int returnVal = m_fileChooser.showOpenDialog(BeeTeeMagnet.this);

      if (returnVal == JFileChooser.APPROVE_OPTION)
      {
        File file = m_fileChooser.getSelectedFile();
        if (file.exists())
        {
          setHoldCursor();
          try
          {
            m_dataWorker.initDataConnection(file.getAbsolutePath());
            m_magnetLinks = m_dataWorker.readBeeTeeDB();
            m_recentlyOpenedFiles.add(file.getAbsolutePath());
            refreshRecentlyMenu();
          }
          catch (SQLException e1)
          {
            e1.printStackTrace();
          }
          finally
          {
            resetCursor();
          }
        }
        refreshTableModel();
      }
    }
    else if (action.equals("MENU.SAVE"))
    {
      int returnVal = m_fileChooser.showSaveDialog(BeeTeeMagnet.this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {
        File file = m_fileChooser.getSelectedFile();
        m_dataWorker.initDataConnection(file.getAbsolutePath());
        m_dataWorker.saveMagnetLinks(m_magnetLinks);
      }
    }
    else if (action.equals("MENU.CLOSE"))
    {
      m_magnetLinks = new HashMap<String, MagnetURI>();
      refreshTableModel();
    }
    else if (action.equals("MENU.RECENT"))
    {
      File file = new File(((JMenuItem)e.getSource()).getText());
      if (file.exists())
      {
        try
        {
          setHoldCursor();
          m_dataWorker.initDataConnection(file.getAbsolutePath());
          m_magnetLinks = m_dataWorker.readBeeTeeDB();
        }
        catch (SQLException e1)
        {
          e1.printStackTrace();
        }
        finally
        {
          resetCursor();
        }
      }
      refreshTableModel();
    }
    else if (action.equals("MENU.CRAWL"))// read magnets directly form bitsnoop
    {
      final int numOfPostsToRead = Integer.parseInt(((JMenuItem)e.getSource()).getText());

      setHoldCursor();
      try
      {
        m_crawlsController = new CrawlsController(this, "C:/Temp/btc", numOfPostsToRead);
        m_crawlsController.start();
        refreshTableModel();
      }
      catch (Exception e1)
      {
        e1.printStackTrace();
      }
      finally
      {
        resetCursor();
      }
    }
  }

  private TreeSet readRecentlyOpenedFiles()
  {
    TreeSet list = null;
    File appFolder = new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".anon" + System.getProperty("file.separator"));
    appFolder.mkdir();
    File file = new File(appFolder, "user.anonym");
    try
    {
      if (file.exists())
      {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        list = (TreeSet)ois.readObject();
        ois.close();
        fis.close();
      }
      else
      {
        list = new TreeSet();
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    return list;
  }

  public void saveRecentlyOpenedFilenames(TreeSet treeSet)
  {
    File appFolder = new File(
        System.getProperty("user.home") + System.getProperty("file.separator") + ".anon" + System.getProperty("file.separator"));
    appFolder.mkdir();
    File usernameFile = new File(appFolder, "user.anonym");
    try
    {
      FileOutputStream fos = new FileOutputStream(usernameFile);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(treeSet);
      fos.flush();
      fos.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private void refreshRecentlyMenu()
  {
    if (m_recentlyOpenedFiles != null && m_recentlyOpenedFiles.size() > 0)
    {
      m_recentlyMenu.removeAll();
      for (Object m_recentlyOpenedFile : m_recentlyOpenedFiles)
      {
        m_recentlySubMenuItem = new JMenuItem((String)m_recentlyOpenedFile);
        m_recentlySubMenuItem.addActionListener(this);
        m_recentlySubMenuItem.setActionCommand("MENU.RECENT");
        m_recentlyMenu.add(m_recentlySubMenuItem);
      }
    }
  }

  public void setHoldCursor()
  {
    setCursorOnActiveWindow(this, Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  }

  public void resetCursor()
  {
    setCursorOnActiveWindow(this, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  private Cursor setCursorOnActiveWindow(Window window, Cursor cursor)
  {
    if (window.getFocusOwner() != null)
    {
      Cursor oldCursor = window.getCursor();
      window.setCursor(cursor);
      return oldCursor;
    }
    else if (window.getOwnedWindows().length > 0)
    {
      Window[] temp = window.getOwnedWindows();
      Cursor tmpCursor = null;
      for (int i = 0; i < temp.length && tmpCursor == null; i++)
      {
        tmpCursor = setCursorOnActiveWindow(temp[i], cursor);
      }
      return tmpCursor;
    }

    return null;
  }

  private void refreshTableModel()
  {
    BeeTeeMagnetsTableModel model = new BeeTeeMagnetsTableModel(m_magnetLinks);
    m_mainTable.setModel(model);
    m_sorter.setModel(model);
    m_mainTable.setColumnWidths(m_columnWidths);
    //    m_mainTable.resetGroupFilters();
    this.pack();
  }

  public Map<String, MagnetURI> getMagnetLinks()
  {
    return m_magnetLinks;
  }

  public static void main(String[] args)
  {
    try
    {
      new BeeTeeMagnet();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (UnsupportedLookAndFeelException e)
    {
      e.printStackTrace();
    }
    catch (InstantiationException e)
    {
      e.printStackTrace();
    }
    catch (IllegalAccessException e)
    {
      e.printStackTrace();
    }
    catch (CouldNotRegisterUriSchemeHandler couldNotRegisterUriSchemeHandler)
    {
      couldNotRegisterUriSchemeHandler.printStackTrace();
    }
  }
}
