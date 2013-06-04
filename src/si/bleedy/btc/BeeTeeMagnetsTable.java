package si.bleedy.btc;

import si.bleedy.btc.uriSchemeHandler.CouldNotOpenUriSchemeHandler;
import si.bleedy.btc.uriSchemeHandler.CouldNotRegisterUriSchemeHandler;
import si.bleedy.btc.uriSchemeHandler.URISchemeHandler;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author DusanM
 */
public class BeeTeeMagnetsTable extends JTable
{
  private URISchemeHandler m_uriSchemeHandler;
  public BeeTeeMagnetsTable(TableModel dm) throws CouldNotRegisterUriSchemeHandler
  {
    super(dm);
    initURISchemeHandler();
    this.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        int row = rowAtPoint(new Point(e.getX(), e.getY()));
        int col = columnAtPoint(new Point(e.getX(), e.getY()));
        if (BeeTeeMagnetsTableModel.COL_LINK == col)
        {
          String url = (String) getModel().getValueAt(row, col);
          try
          {
            URI magnetLinkUri = new URI(url);
            m_uriSchemeHandler.open(magnetLinkUri);
          }
          catch (URISyntaxException e1)
          {
            e1.printStackTrace();
          }
          catch (CouldNotOpenUriSchemeHandler couldNotOpenUriSchemeHandler)
          {
            couldNotOpenUriSchemeHandler.printStackTrace();
          }
        }
      }
    });
  }

  private void initURISchemeHandler() throws CouldNotRegisterUriSchemeHandler
  {
    m_uriSchemeHandler = new URISchemeHandler();
    m_uriSchemeHandler.register("magnet", "c:/windows/system32//notepad.exe");
  }

  public void setColumnWidths(int[] nRelativeColumnWidths)
  {
    int nCount = columnModel.getColumnCount();
    int nRelativeColumnWidthsSum = 0;
    for (int i = 0; i < nCount; i++)
    {
      nRelativeColumnWidthsSum += nRelativeColumnWidths[i];
    }

    int nWidth = getWidth();
    for (int i = 0; i < nCount; i++)
    {
      int nColumnWidth = nWidth * nRelativeColumnWidths[i] / nRelativeColumnWidthsSum;
      columnModel.getColumn(i).setPreferredWidth(nColumnWidth);
      nWidth -= nColumnWidth;
      nRelativeColumnWidthsSum -= nRelativeColumnWidths[i];
    }

    sizeColumnsToFit(-1);
  }
}
