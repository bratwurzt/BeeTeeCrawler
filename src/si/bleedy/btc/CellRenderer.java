package si.bleedy.btc;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author DusanM
 */
public class CellRenderer extends JEditorPane implements TableCellRenderer
{
  public CellRenderer()
  {
    setEditable(false);
    setContentType("text/html");
//    HyperlinkListener listener = new HyperlinkListener()
//    {
//      public void hyperlinkUpdate(HyperlinkEvent e)
//      {
//        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
//        {
//          open(e.getURL());
//        }
//      }
//    };
//    addHyperlinkListener(listener);
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
  {
    String text = (String) value;
//    setWrapStyleWord(true);
//            setLineWrap(true);
    // get the text area preferred height and add the row margin
    int height = getPreferredSize().height + table.getRowMargin();
    // ensure the row height fits the cell with most lines
    if (height != table.getRowHeight(row))
    {
      table.setRowHeight(row, height);
    }
    setText(text);
    return this;
  }

}
