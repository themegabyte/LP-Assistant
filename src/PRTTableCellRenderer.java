import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Color.*;
import java.awt.*;
import java.awt.Container;

//import BigRational;

public class PRTTableCellRenderer extends DefaultTableCellRenderer {

//	private boolean drawLeft;
//	private boolean drawRight;
//	private boolean drawBottom;

	private boolean fLocked;
	private Color fColor;
	private PRTTableModel fPRTTableModel;

	public PRTTableCellRenderer(PRTTableModel theTableModel) {
		super();
		setHorizontalAlignment(CENTER);
		fPRTTableModel = theTableModel;
		}
	
	public void setLocked(boolean lock) {
		fLocked = lock;
		}

	public void setBackground(Color c) {
		fColor = c;
		super.setBackground(c);
		}
		
	public void setValue(Object aValue){
		//if ( (aValue!=null) && (aValue instanceof String) ) {
		//	LPAssistant.speak((String)aValue);
		//	}
		super.setValue(aValue);
		}
		
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		if (!fLocked) {
			super.paint(g);
			return;
			}
			
		Rectangle r = g2.getClipBounds();
		g2.setColor(fColor);
		g2.fillRect(r.x, r.y, r.width, r.height);
		String s = getText();
		if (s.startsWith("X") || s.startsWith("Y"))
			fPRTTableModel.paintBasicVariable(g2,r,s);
		else if (s.indexOf("/", 0)<0)	// take no special action if we have an integer
			fPRTTableModel.paintInteger(g2,r,s);
		else
			fPRTTableModel.paintRationalNumber(g2,r,s);
		}
		
//	public void setDrawLinesLRB(boolean dL, boolean dR, boolean dB) {
//		drawLeft = dL;
//		drawRight = dR;
//		drawBottom = dB;
//		}

//	public void paintMyBoundary(Graphics g) {
//		String s = getText();
//		//LPAssistant.speak("string is "+s);
//		Rectangle r = g.getClipBounds();
//		//LPAssistant.speak("Height="+r.height+ ", Width=" + r.width+ ", x=" + r.x + ", y=" + r.y);
//		if (drawBottom)
//			g.drawLine(0,r.height-1,r.width,r.height-1);
//		if (drawLeft)
//			g.drawLine(0,0,0,r.height-1);
//		if (drawRight)
//			g.drawLine(r.width-1,0,r.width-1,r.height-1);
//		}
		

}
