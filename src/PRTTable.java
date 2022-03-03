import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.*;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Font;
import java.awt.event.*;
import java.awt.*;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.JTableHeader;
//import javax.swing.InputVerifier;

import java.util.Vector;
	
public class PRTTable extends JTable implements MouseListener, MouseMotionListener {

	private PRTTableModel fModel;
	private PRTTableCellRenderer fCellRenderer;
	private JTextField fTextFieldforEditor = new JTextField();
	private PRTCellEditor fTextCellEditor = new PRTCellEditor(fTextFieldforEditor);

	private JScrollPane fMyScrollPane;
	private int fBaseFontSize = 12;
	private String fFontName = "sansserif";
	
	public PRTTable(){
		super();
		}
	
	public PRTTable(PRTTableModel myModel) {
		super(myModel);
		fModel = myModel;
		
		fCellRenderer = new PRTTableCellRenderer(myModel);
		JTextField textField = (JTextField) fTextCellEditor.getComponent();
		textField.setHorizontalAlignment(0);

		fTextFieldforEditor.addFocusListener(myModel);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(myModel);

		showHorizontalLines = false;
		showVerticalLines = false;
		//setIntercellSpacing(new Dimension(0,0));
		getTableHeader().setReorderingAllowed(false);
		rowMargin = 1; // tweak space to draw lines?
		}
		
//		public void saySomething(String eventDescription, MouseEvent e) {
////			LPAssistant.speak(eventDescription 
////						+ " (" + e.getX() + "," + e.getY() + ")"
////						+ " detected on "
////                        + e.getComponent().getClass().getName()
////                        + ".");
//    }
		

		
// *********************************************************************
// ********************* Mouse Listening Methods ***********************

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount()==1) { // so we ignore double- and triple-clicks ???
			int row = rowAtPoint(e.getPoint());
			int col = columnAtPoint(e.getPoint());
			fModel.handleClick(row, col, e.getPoint());
			}
		}
	public void mousePressed(MouseEvent e) {
//			   saySomething("Mouse pressed; # of clicks: "
//							+ e.getClickCount(), e);
		}

	public void mouseReleased(MouseEvent e) {
//			   saySomething("Mouse released; # of clicks: "
//							+ e.getClickCount(), e);
		}

	public void mouseEntered(MouseEvent e) {
//			   saySomething("Mouse entered", e);
		}

	public void mouseExited(MouseEvent e) {
	   fModel.handleMouseExited();
		}

//			public void mouseClicked(MouseEvent e) {
//			   saySomething("Mouse clicked (# of clicks: "
//							+ e.getClickCount() + ")", e);
//				}

// *********************************************************************
// ***************** Mouse Motion Listening Methods ********************

	public void mouseMoved(MouseEvent e) {
			int row = rowAtPoint(e.getPoint());
			int col = columnAtPoint(e.getPoint());
			fModel.handleMouseMovedTo(row,col);
		}

	public void mouseDragged(MouseEvent e) {
//				saySomething("Mouse dragged", e);
		}			

	public int getBaseFontSize() {
		return fBaseFontSize;
		}
	
	public TableCellRenderer getCellRenderer(int row, int column) {
		Color theColor = fModel.getColorAt(row,column);
		fCellRenderer.setBackground(theColor);
		fCellRenderer.setLocked(fModel.isLocked());
//		fCellRenderer.setDrawLinesLRB(
//			column==fModel.getColumnCount()-1, // left
//			column==0,							// right
//			true								// bottom
//			);
		return fCellRenderer;
		}

	public TableCellEditor getCellEditor(int row, int column) {
//		if ((column==0) && (row==getRowCount()-1)) {
//			return fLockCellEditor;
//			}
		//fTextFieldforEditor.selectAll();
		return fTextCellEditor;
		}

	private void paintMyDecorations(Graphics g) {

		Rectangle clipRect = g.getClipBounds();
////		LPAssistant.speak("clip rect Height="+ clipRect.height+ ", Width=" + clipRect.width+ ", x=" + clipRect.x + ", y=" + clipRect.y);

		int nColumns = columnModel.getColumnCount();
////		LPAssistant.speak("column count is " + nColumns);
//
//// the following draws the right set of vertical lines for every situations
//

		JTableHeader jth = getTableHeader();
		Rectangle r = jth.getHeaderRect(0);
		g.drawLine(r.width-1,0,r.width-1,getRowCount()*getRowHeight()-1);
		r = jth.getHeaderRect(nColumns-1);
		g.drawLine(r.x-1,0,r.x-1,getRowCount()*getRowHeight()-1);
		if (fModel.getNumArtificialVariables()>0) {
			r = jth.getHeaderRect(fModel.getNumRegularVariables()+1);
			g.drawLine(r.x-1,0,r.x-1,getRowCount()*getRowHeight()-1);
			}
//
//// the following does the right horizontal lines for the FIRST tableau -- we need
//// to replicate the lines throughout tableau 2 and higher.
//
//		// top (if replicated, produces a double line between successive tableau?)
//		//g.drawLine(0,0,clipRect.width,0);
//		
//		// separate contraints from objective function
		int constraints = fModel.getNumConstraints();
		int rowHeight = getRowHeight();
		int hOffset1 = rowHeight*constraints - 1;
		int hOffset2 = rowHeight * fModel.getTableauRowCount() - 1;
//		
		for (int i=1; i<=fModel.getNumTableaux(); i++) {
			g.drawLine(0, hOffset1, clipRect.width, hOffset1);
			g.drawLine(0, hOffset2, clipRect.width, hOffset2);
			hOffset1 += rowHeight * fModel.getTableauRowCount();
			hOffset2 += rowHeight * fModel.getTableauRowCount();
			}
	
		// finally, do pivot locations, which are kept as a Vector of Points
		
		Vector v = fModel.getPivotLocations();
		int numPivotstoDraw = v.size();
		//LPAssistant.speak("numPivotstoDraw is " + numPivotstoDraw);
		for (int i=0; i<numPivotstoDraw; i++) {
			Point pt = (Point) v.elementAt(i);
			r = getCellRect(pt.x, pt.y, false);  // false = no spacing
			g.drawOval(r.x, r.y, r.width, r.height);
			//LPAssistant.speak("draw pivot loc at " + pt.x + ", " + pt.y);
			}
		}

	public void paint(Graphics g) {
		
		super.paint(g);
		paintMyDecorations(g);
		}
		
	public Component prepareEditor(TableCellEditor editor, int row, int column) {
		//LPAssistant.speak("prepareEditor at row " + row + ", column " + column);
		Component theComponent = super.prepareEditor(editor, row, column);
		fTextFieldforEditor.selectAll();
		fTextFieldforEditor.setFont(new Font(fFontName, Font.PLAIN, fBaseFontSize));
		return theComponent;
		}
	
	public void setLocked(boolean lock) {
		if (lock)
			clearSelection();
		//setCellSelectionEnabled(lock);
		}

	public void setScrollPane(JScrollPane theScrollPane) {
		fMyScrollPane = theScrollPane;
		}
	
	public void setValueAt(Object value, int row, int col) {
//		LPAssistant.speak("Setting value at " + row + "," + col
//                                  + " to " + value
//                                   + " (an instance of "
//                                   + value.getClass() + ")");
		BigRational br;
		try {
			br = new BigRational((String)value);
			}
		catch (java.lang.NumberFormatException e) { // just replace with a zero for now
			br = new BigRational(0);
			}
//		finally {  // just in case
//			br = new BigRational(0);
//			}		
		super.setValueAt(br,row,col);
		
		}
	public JTableHeader getTableHeader() {
		return tableHeader;
		}
		
	public void setFontAndSize(String theFontName, int theSize) {
		setFont( new Font(theFontName, Font.PLAIN, theSize) );
		//fTextFieldforEditor.setFont(new Font(fFontName, Font.PLAIN, fBaseFontSize));
		fFontName = theFontName;
		fBaseFontSize = theSize;
		updateRowHeight();
		}
	
	public void updateRowHeight() {		
//		Font theFont = getFont();
//		LPAssistant.speak("Font is currently " + theFont.getName());
//		LPAssistant.speak("its style is " + theFont.getStyle());
//		LPAssistant.speak("its size is " + theFont.getSize());
		
		int newRowHeight = 20; // default value on open
		Font theFont = getFont();
		Graphics2D g2 = (Graphics2D) getGraphics();
		if (g2!=null) {	// this will happen during creation, since not everything will be set up then (?)			
			FontMetrics fm = g2.getFontMetrics();
			newRowHeight = fm.getHeight() + 6; // 3 pixels above, 3 pixels below
			if (fModel.isLocked() && (fModel.getAccuracy()==0)) {
				setFont( new Font(theFont.getName(), Font.PLAIN, fModel.getFractionTermSize()) );
				newRowHeight = fm.getAscent()*2 + 8; // char in numerator, char in denominator, no descenders in numbers,
														// 4 pixels between, 2 above, 2 below
				}
			}
			
		setRowHeight(newRowHeight);
		fMyScrollPane.getVerticalScrollBar().setUnitIncrement(newRowHeight);
		fMyScrollPane.getVerticalScrollBar().setBlockIncrement(newRowHeight);
		}
		

	}