import java.util.*;
import java.util.Vector;

import javax.swing.table.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.JTable;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import java.awt.Color.*;
import java.awt.*;
import java.awt.print.*;
import java.awt.geom.*;
import java.awt.Font;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.Integer;
import javax.print.attribute.*;
import java.awt.Point;
import java.text.DateFormat;

public class PRTTableModel extends DefaultTableModel implements Printable, ActionListener, FocusListener, KeyListener {

// *************************************************************
// ********************* instance variables ********************

	private boolean fLocked = false;
	private boolean fUsingX = true;
	private int fAccuracy = 0;	// 0 = exact, then 1 = 1 decimal place, etc.
	private PRTTable fPRTTable; // the table i manage
	private PRTFrame fPRTFrame; // the frame that contains my table
	private Vector fPivotLocations = new Vector();
	
	// in this design, all tableaux have the same
	// number of rows, so it's easy to count and
	// keep track of the w-row, since it will
	// be in every tableau if present in the first	
	
	private int	fNumConstraints;
	private int	fNumRegularVariables;
	private int	fNumArtificialVariables = 0;
	private int fNumTableaux = 1;	
	private boolean	fHasDRow = false;
	
	// to further simplify coding, we'll always keep track of the range of
	// rows where the constraints of the last tableau are recorded
	
	private int firstConstraintRowofLastTableau = 0;
	private int lastConstraintRowofLastTableau;
	
	// printing parameters we want to use across each page --
	// these are computed when the first page is called for printing,
	// not recomputed for every page
	
	private int fNumRowsPerPage;
	private int fPageCount;
	private String fPrintUserName;
	private String fPrintStamp;
	
	// the file associated with me, if any, plus temporary holders for artificial
	// variables to add and pivots to make upon opening an exisiting file
	
	private boolean fDataChanged = false;	// reset to true anytime data is changed
	private File fFile = null;
	private Vector fInputArtificialVariableRows = new Vector();
	private Vector fInputPivotLocations = new Vector();

	private int fBaseFontSize;
	private String fFontName;

// *************************************************************
// ****************************** CONSTRUCTORS *****************

	public PRTTableModel() {
		super();
		}

	public PRTTableModel(int numConstraints,int numVariables) {
	
			// there's one more row for the objective function "z", and there
			// are two more columns -- one for the basic variable index, and
			// one for the constraint limit.
			
		super(numConstraints+1,numVariables+2);	
		fNumRegularVariables = numVariables;
		fNumConstraints = numConstraints;
		lastConstraintRowofLastTableau = numConstraints - 1;

			// put some data into the default table model structure so
			// we can at least a minimal tableau to work with 
		
		for (int r=0; r<numConstraints+1; r++) {
			setValueAt(new BigRational(0), r, 0); // for no basis element index yet chosen
			for (int c=1; c<numVariables+2; c++) {
				setValueAt(new BigRational(0), r, c);
				}
			}
		fDataChanged = false;
		}

	public PRTTableModel(File fromFile) {	
		super();	
		try {
			readData(fromFile);
			fFile = fromFile;
			fDataChanged = false;
			}
		catch (IOException e) {
			LPAssistant.speak("Error reading data.  Exception is:"+e.toString());
			}
		}

	public PRTTableModel(PRTTableModel previousPRTTableModel) {
	
			// basically same as above, but copy all appropriate instance variables and
			// data from last tableau of previous PRTTableModel ...
			
		super(previousPRTTableModel.getTableauRowCount(), previousPRTTableModel.getColumnCount());
		fNumConstraints = previousPRTTableModel.fNumConstraints;
		fNumRegularVariables = previousPRTTableModel.fNumRegularVariables;
		lastConstraintRowofLastTableau = fNumConstraints - 1;
		fHasDRow = previousPRTTableModel.fHasDRow;
		fNumArtificialVariables = previousPRTTableModel.fNumArtificialVariables;
		fUsingX = previousPRTTableModel.fUsingX;
		fAccuracy = previousPRTTableModel.fAccuracy;
		
		//previousPRTTableModel.printDebugData();
		
			// copy data from last tableau of previousPRTTableModel
		
		Vector oldDV = previousPRTTableModel.getDataVector();
		for (int r=previousPRTTableModel.getFirstPhysicalRowIndex(); r<=previousPRTTableModel.getLastPhysicalRowIndex(); r++) {
			Vector rowVector = (Vector) oldDV.elementAt(r);
			for (int c=0; c<previousPRTTableModel.getColumnCount(); c++) {
				BigRational br = (BigRational) rowVector.elementAt(c);
				setValueAt(new BigRational(br.toString()), r-previousPRTTableModel.getFirstPhysicalRowIndex(), c);
				}
			}
		
		fDataChanged = false;

		//printDebugData();
				
//		tableauParameters tp = new tableauParameters(fNumConstraints,fNumRegularVariables);
//		fPivotLocations.addElement(tp);
					
		}

// *******************************************************************
// *********** KEY LISTENING CODE

	public void keyTyped(KeyEvent e) {
		//LPAssistant.speak("KEY TYPED: ");
		//displayInfo(e);
		}

	public void keyPressed(KeyEvent e) {
		//LPAssistant.speak("KEY PRESSED: ");
		//displayInfo(e);
		}

	public void keyReleased(KeyEvent e) {
		//LPAssistant.speak("KEY RELEASED: ");
		//displayInfo(e);
		}
 
	private void displayInfo(KeyEvent e){
        

        //You should only rely on the key char if the event
        //is a key typed event.
        int id = e.getID();
        String keyString;
        if (id == KeyEvent.KEY_TYPED) {
            char c = e.getKeyChar();
            keyString = "key character = '" + c + "'";
        } else {
            int keyCode = e.getKeyCode();
            keyString = "key code = " + keyCode
                    + " ("
                    + KeyEvent.getKeyText(keyCode)
                    + ")";
        }
		LPAssistant.speak(keyString);
        
        int modifiersEx = e.getModifiersEx();
        String modString = "extended modifiers = " + modifiersEx;
        String tmpString = KeyEvent.getModifiersExText(modifiersEx);
        if (tmpString.length() > 0) {
            modString += " (" + tmpString + ")";
        } else {
            modString += " (no extended modifiers)";
        }
		LPAssistant.speak(modString);
        
        String actionString = "action key? ";
        if (e.isActionKey()) {
            actionString += "YES";
        } else {
            actionString += "NO";
        }
		LPAssistant.speak(actionString);
        
        String locationString = "key location: ";
        int location = e.getKeyLocation();
        if (location == KeyEvent.KEY_LOCATION_STANDARD) {
            locationString += "standard";
        } else if (location == KeyEvent.KEY_LOCATION_LEFT) {
            locationString += "left";
        } else if (location == KeyEvent.KEY_LOCATION_RIGHT) {
            locationString += "right";
        } else if (location == KeyEvent.KEY_LOCATION_NUMPAD) {
            locationString += "numpad";
        } else { // (location == KeyEvent.KEY_LOCATION_UNKNOWN)
            locationString += "unknown";
        }
		LPAssistant.speak(locationString);
        
    }


	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand()=="Add Constraint")
			doAddConstraint();
		else if (e.getActionCommand()=="Add Regular Variable") 
			doAddRegularVariable();
		else if (e.getActionCommand()=="Remove Last Tableau") 
			removeLastTableau();
		else if (e.getActionCommand()=="Edit") 
			setLocked(false);
		else if (e.getActionCommand()=="Pivot") 
			setLocked(true);
		else if (e.getActionCommand()=="Simplex") 
			setUseX(true);
		else if (e.getActionCommand()=="Dual Simplex") 
			setUseX(false);
		else if (e.getActionCommand()=="1/2") 
			setAccuracy(0);
		else if (e.getActionCommand()=="1.0") 
			setAccuracy(1);
		else if (e.getActionCommand()=="1.00") 
			setAccuracy(2);
		else if (e.getActionCommand()=="1.000") 
			setAccuracy(3);
		else if (e.getActionCommand().startsWith("Variable")) 
			doRemoveVariable(e.getActionCommand());
		else if (e.getActionCommand().startsWith("From Row")) 
			doRemoveConstraint(e.getActionCommand());
		else
			LPAssistant.speak("Event not handled: "+e.getActionCommand());
		}
		
	public boolean askToSave() {
		return fDataChanged;
		}
		
	private void doRemoveConstraint(String actionName) {

			// we know the string actionName has the form "From Row N" 
			// so parse the string for the parameter and execute

		int k = actionName.length();
		String str;
		if (k==10)
			str = actionName.substring(9); 
		else
			str = actionName.substring(9,k);
		int row = Integer.valueOf(str).intValue();
		doRemoveConstraint(row-1);
		}

	private void doRemoveVariable(String actionName) {
	
			// we kow the string actionName has the form "Variable N"
			// so parse the string for the parameter and execute
			
		int k = actionName.length();
		String str;
		if (k==10)
			str = actionName.substring(9);
		else
			str = actionName.substring(9,k);
		int whichVar = Integer.valueOf(str).intValue();
		doRemoveVariable(whichVar);
		}

	public void doAddConstraint() {
		//LPAssistant.speak("table model: add constraint");
		if (fNumTableaux>1)
			return;
		
		Vector v = new Vector();
		for (int j=0; j<getColumnCount(); j++) {
			BigRational br = new BigRational(0);
			v.add(br);
			}
		insertRow(lastConstraintRowofLastTableau+1,v);
		fireTableRowsInserted(lastConstraintRowofLastTableau+1,lastConstraintRowofLastTableau+1);
		++lastConstraintRowofLastTableau;
		++fNumConstraints;
		
		setLocked(false);	// switch back to edit mode, please
		updateMenus();
		recomputeDRow();
		fDataChanged = true;
		}
	
	
	public void doAddRegularVariable() {
		LPAssistant.speak("table model: add regular variable");
		insertNewVariable(fNumRegularVariables+1);
		++fNumRegularVariables;
		
		setLocked(false);	// switch back to edit mode
		fireTableStructureChanged();
		fireTableDataChanged();
		updateMenus();
		fDataChanged = true;
		}

	public void doRemoveVariable(int whichVar) {
		LPAssistant.speak("table model: remove variable (" + whichVar + ")");
		if ((fNumTableaux>1) || (whichVar<=0) ||(whichVar>=getColumnCount()))
			return;
		
		boolean removingArtificialVariable = (whichVar>fNumRegularVariables);
		if ((!removingArtificialVariable) && (fNumRegularVariables==2))	// no less than 2 variables
			return;

		
		removeColumnAndData(whichVar);
		
		if (removingArtificialVariable) {
			--fNumArtificialVariables;
			if (fNumArtificialVariables>0)	// there might not be a D row at this point, but
				recomputeDRow();			// if so, the call to recomputeDRow does nothing
			else if (fHasDRow) {
				fHasDRow = false;
				removeRow(getRowCount()-1);
				fireTableStructureChanged();
				}
			}
		else
			--fNumRegularVariables;  
			
			// reset any basis variable choice if we just deleted it,
			// as well as decrement any existing variable index larger than the one
			// deleted
		
		for (int row=0; row<getRowCount(); row++) {
			int theBasicVariable = getBasicVariableinRow(row);
			if (theBasicVariable==whichVar)
				clearBasicVariableinRow(row);
			else if (theBasicVariable>whichVar)
				setBasicVariableinRow(row, theBasicVariable-1);
			}
			
		updateMenus();
		fDataChanged = true;
		}
		
		
	public void doRemoveConstraint(int whichRow) {
		LPAssistant.speak("table model: remove constraint");
		if (fNumTableaux>1)
			return;
		
			// record the basic variable in the row before we take it out, then
			// remove the row
			
		int basicVariable = getBasicVariableinRow(whichRow);		
		removeRow(whichRow);
		fireTableRowsDeleted(whichRow,whichRow);
		--lastConstraintRowofLastTableau;
		--fNumConstraints;
		
			// before leaving, if we've deleted a row in which there was an artificial
			// variable, we have to remove that variable; and if it was the ONLY artificial variable, 
			// then we have to take out the D row.  but this is all done by simply removing that
			// artificial variable.
		
		if (basicVariable>fNumRegularVariables) 
			doRemoveVariable(basicVariable);
			
		updateMenus();
		fDataChanged = true;
		}
	
	
	public void doSave() {
		try {
			writeData(fFile);
			fDataChanged = false;
			}
		catch (IOException e) {
			LPAssistant.speak("error in doSave()");
			}
		}
		
	public void doSave(File saveFile) {
		try {
			writeData(saveFile);
			fFile = saveFile;
			fPRTFrame.setTitle(saveFile.getName());
			fDataChanged = false;
			}
		catch (IOException e) {
			LPAssistant.speak("error in doSave(File saveFile)");
			}
		}

	public void focusGained(FocusEvent e) {
		displayMessage("Focus gained", e);
		}

	public void focusLost(FocusEvent e) {
		displayMessage("Focus lost", e);
		}
	
	private void displayMessage(String prefix, FocusEvent e) {
		LPAssistant.speak(prefix
                       + (e.isTemporary() ? " (temporary):" : ":")
                       +  e.getComponent().getClass().getName()
                       + "; Opposite component: " 
                       + (e.getOppositeComponent() != null ?
                          e.getOppositeComponent().getClass().getName() : "null")
						  ); 
		}	
	
	private int getCRatioRowIndex() {
		int cRow = getRowCount()-1;		// most times we use the last row
		return cRow;					// JAN08 -- we'll always use the last row!
										// reason: the user should decide that the D row is meaningless
										// and take the responsibility to remove it !!
		
//		if (!fHasDRow)
//			return cRow;
//		
//			// so, there's a D row, thus check to see if the D row has become all zeroes ...
//			// if it has, then the D row has become useless, and we use the objective
//			// function row for _all_ ratios displayed for the dual simplex algorithm
//			
//		boolean allZeroes = true;
//		for (int col = 1; col<=fNumRegularVariables; col++) {
//			BigRational br = (BigRational) super.getValueAt(cRow , col);
//			if (br.sign()!=0) 
//				allZeroes = false;
//			}
//		if (allZeroes)
//			return cRow-1;
//		else
//			return cRow;
		}
	
	private int getFirstConstraintRow() {
		return firstConstraintRowofLastTableau;
		}
		
	private int getFirstPhysicalRowIndex() {
		return firstConstraintRowofLastTableau;
		}
		
	private int getLastConstraintRow() {
		return lastConstraintRowofLastTableau;
		}
	
	private int getLastPhysicalRowIndex() {
		return firstConstraintRowofLastTableau+getTableauRowCount()-1;
		}
		
	public int getNumArtificialVariables() {
		return fNumArtificialVariables;
		}

	public int getNumConstraints() {
		return fNumConstraints;
		}

	public int getNumRegularVariables() {
		return fNumRegularVariables;
		}

	public int getNumTableaux() {
		return fNumTableaux;
		}

	public int getNumVariables() {
		return fNumRegularVariables+fNumArtificialVariables;
		}

	public int getTableauRowCount() {
		int count = fNumConstraints+1;
		if (fHasDRow)
			++count;
		return count;
		}
		
	public PRTTable getTable() {
		return fPRTTable;
		}
		
	public Vector getPivotLocations() {
		return fPivotLocations;
		}
	
	public Class getColumnClass( int column ) 
    {
        return getValueAt(0, column).getClass();
    }
	
	public Color getColorAt(int row, int col) {
		Color returnColor = Color.WHITE;
		if ((col>fNumRegularVariables) && (col<getColumnCount()-1)) // artificial variable column
			returnColor = new Color(255,255,0,50);
		else if (rowIsInDRow(row))
			returnColor = new Color(255,255,0,50);
		return returnColor;
		}

	public void doPivotAt(int row, int col) {
//		LPAssistant.speak("pivot at row = " + row + ", col = " + col);
//		LPAssistant.speak("firstConstraintRowofLastTableau = " + firstConstraintRowofLastTableau);
//		LPAssistant.speak("lastConstraintRowofLastTableau = " + lastConstraintRowofLastTableau);
		duplicateLastTableau();		
		computePivot(firstConstraintRowofLastTableau,getRowCount()-1,row+getTableauRowCount(),col);
		fPivotLocations.addElement(new Point(row,col));
		fireTableDataChanged();
		updateMenus();
		fPRTFrame.scrollToLastTableau();
		}
	
	public void handleClick(int row, int col, Point theClickPoint) {
		

		//LPAssistant.speak("PRTTableModel.handleClick for row "+row+", column "+col);

		if ((fNumTableaux==1) && (col==0) && (row<=getLastConstraintRow())) {
			handleBasisChange(row, theClickPoint);
			return;
			}
		
			
		if ((fNumTableaux==1) && (col==0) && (row==(getLastConstraintRow()+1))) {
			handleAttempttoAddConstraint(theClickPoint);
			return;
			}
		
			
		if ((fLocked) && (canPivotAt(row,col)))
			doPivotAt(row,col);
		else if ((!fLocked) && (isCellEditable(row,col)))
			return;
		}

	public void handleMouseExited() {
		fPRTFrame.updateRatioLabel("   ");
		//fPRTFrame.setToolTipText("");
		}
		
	public void handleMouseMovedTo(int row, int col) {
		if ((!fLocked) || (!canPivotAt(row,col))) {
			fPRTFrame.updateRatioLabel("   ");
			//fPRTFrame.setToolTipText("");
			return;
			}
		
		String str = "<html>";
		BigRational denominator = (BigRational) super.getValueAt(row, col);
		BigRational numerator;
		int displayRow = (row % getTableauRowCount()) + 1;
		if (fUsingX) {
			numerator= (BigRational) super.getValueAt(row, getColumnCount()-1);
			str += "b<sub>" + (displayRow) + "</sub>/";
//			fPRTFrame.updateRatioLabel("   b/a = " + numerator.divide(denominator).toStringDot(3));
			}
		else {
			numerator= (BigRational) super.getValueAt(getCRatioRowIndex(), col);
			if (fHasDRow)
				str += "w<sub>" + col + "</sub>/";
			else
				str += "c<sub>" + col + "</sub>/";
//			fPRTFrame.updateRatioLabel("   c/a = " + numerator.divide(denominator).toStringDot(3));
			}
		str += "a<sub>" + (displayRow) + "," + col + "</sub> = " + numerator.divide(denominator).toStringDot(3) + "</html>";
		fPRTFrame.updateRatioLabel(str);
		//fPRTFrame.setToolTipText(numerator.divide(denominator).toStringDot(3));
		}


	public boolean hasFile() {
		return (fFile!=null);
		}
		
	private void insertNewVariable(int whichColumn) {
		
		addColumn("RHS");
		
		// null data has been added here at the end of every row, so copy the previous 
		// columns over one to the right, leaving zeroes behind in the newly added column
		
		int lastColumnIndex = getColumnCount()-1;
		for (int row = 0; row < getRowCount(); row++) {
			for (int col = lastColumnIndex; col>whichColumn; col--) {
				//LPAssistant.speak("copy from row = " + row + ", column = " + (col-1));
				BigRational br = (BigRational) super.getValueAt(row , col-1);
				//LPAssistant.speak("to row = " + row + ", column = " + col);
				setValueAt(br , row , col);
				}
			setValueAt(new BigRational(0) , row , whichColumn);			
			}
		
		// also, need to increment indices of existing basic variables
		// whose columns have been moved one to the right by the insertion

		for (int row = 0; row < getRowCount(); row++) {
			int theVar = getBasicVariableinRow(row);
			if (theVar > whichColumn)
				setBasicVariableinRow(row, theVar+1);
			}
		
		fDataChanged = true;
		}
			
	public boolean rowHasArtificialVariable(int row) {
		//LPAssistant.speak("table model: rowHasArtificialVariable");
		int theVar = getBasicVariableinRow(row);
		//LPAssistant.speak("row is " + row + ", variable is " + theVar);
		return (theVar>fNumRegularVariables);
		}
	
	public boolean hasArtificialVariablesinBasis() {
		boolean theResult = false;
		for (int row=firstConstraintRowofLastTableau; row<=lastConstraintRowofLastTableau; row++)
			if (getBasicVariableinRow(row)>fNumRegularVariables)
				theResult = true;
		return theResult;
		}

	private int getBasicVariableinRow(int row) {
		BigRational num = (BigRational)super.getValueAt(row,0);
		return num.intValue();
		}
	
	public void setBasicVariableinRow(int whichRow, int theNewBV) {
		//LPAssistant.speak("PRTTableModel:setBasicVariableinRow in row "+whichRow+" to "+theNewBV);
		setValueAt(new BigRational(theNewBV), whichRow, 0);
		//fireTableCellUpdated(whichRow,0);
		fDataChanged = true;
		}

	public void clearBasicVariableinRow(int row) {
		setValueAt(new BigRational(0), row, 0);
		fDataChanged = true;
		}
	
	private boolean isVariableinBasis(int whichVar) {
		boolean theResult = false;
		for (int row=firstConstraintRowofLastTableau; row<=lastConstraintRowofLastTableau; row++)
			if (getBasicVariableinRow(row)==whichVar)
				theResult = true;
		return theResult;
		}
		
// *************************************************************
// ******************* ROUTINES FOR ART VARIABLES **************

	private void recomputeDRow() {
		// this is a full recompute.  it's easier since we could be called after a deletion
		// of an artificial variable.  but we know it will only happen in first tableau
		
		if (!fHasDRow)	// this is an innocent-looking protection; but in a continued 
			return;		// problem, we could have artificial variables hanging around, but
						// we've removed the D row
			
		//LPAssistant.speak("recomputeDRow: start");
		//printDebugData();
		for (int col=1; col<=fNumRegularVariables; col++) {
			BigRational sum = new BigRational(0);
			for (int row=0; row<fNumConstraints; row++) {
				BigRational num = (BigRational)super.getValueAt(row,0);
				if (num.compareTo(fNumRegularVariables)==1) {
					sum = sum.subtract((BigRational)super.getValueAt(row,col));	
					}
				}
			setValueAt(sum,getRowCount()-1,col);
			}

		//LPAssistant.speak("recomputeDRow: do b column");
		BigRational sum = new BigRational(0);
		for (int row=0; row<fNumConstraints; row++) {
			BigRational num = (BigRational)super.getValueAt(row,0);
			if (num.compareTo(fNumRegularVariables)==1)
				sum = sum.subtract((BigRational)super.getValueAt(row,getColumnCount()-1));	
			}
		setValueAt(sum,getRowCount()-1, getColumnCount()-1);
		}
		
	private void createDRow() {
				
			// populates a new w-row with zeroes; it will eventually get recomputed
				
		Vector v = new Vector();
		for (int j=0; j<getColumnCount(); j++) {
			BigRational br = new BigRational(0);
			v.add(br);
			}
		addRow(v);			
		fHasDRow = true;
		}
		
	public void doAddArtificialVariable(int row) {
		//LPAssistant.speak("table model: add artificial variable");
		
			// basic safeguards: only in first tableau, only for a constraint row,
			// and only in a row for which there is not already an artificial variable
		
		if ((fNumTableaux>1) || (row<0) || (row>lastConstraintRowofLastTableau)
				|| (getBasicVariableinRow(row)>fNumRegularVariables))
			return;
			
		
		insertNewVariable(getColumnCount()-1);
		++fNumArtificialVariables;
		if (!fHasDRow) {
			createDRow();
			}
			
		setValueAt(new BigRational(1), row, getColumnCount()-2);
		setBasicVariableinRow(row,getColumnCount()-2);
		//setValueAt(new BigRational(getColumnCount()-2), row, 0);
		recomputeDRow();
		fireTableStructureChanged();
		fireTableDataChanged();
		updateMenus();
		fDataChanged = true;
		}

	public void removeColumnAndData(int whichCol) {
	
		TableColumnModel tcm = fPRTTable.getColumnModel();
		TableColumn tabCol = tcm.getColumn(whichCol);
		fPRTTable.removeColumn(tabCol);
		
		Vector data = dataVector;
		for (int row = 0; row < getRowCount(); row++) {
			Vector v = (Vector) ((Vector)data).elementAt(row);		
			v.removeElementAt(whichCol);
			}
		
		columnIdentifiers.removeElementAt(whichCol);
		setDataVector(data,columnIdentifiers);
		fireTableStructureChanged();
		fDataChanged = true;
		}
		

	public void setTable(PRTTable table) {
		fPRTTable = table;
		}

	public void setFrame(PRTFrame frame) {
		fPRTFrame = frame;
		}

	public void removeLastTableau() {
		LPAssistant.speak("execute Remove Last Tableau");
		
		if (fNumTableaux==1)  // there's only one tableau, can't do it
			return;
			
			// so figure out which are the rows belonging to the last
			// tableau and remove them, as well as the last set of
			// tableau parameters
		
		int lastRowToDelete = getRowCount()-1;
		for (int i=lastRowToDelete; i>=getFirstConstraintRow(); i--) {
			removeRow(i);
			}

		fPivotLocations.remove(fPivotLocations.size()-1);
		fNumTableaux--;
		firstConstraintRowofLastTableau -= getTableauRowCount();
		lastConstraintRowofLastTableau -= getTableauRowCount();
		
		fireTableStructureChanged();
		updateMenus();
		fDataChanged = true;
		}

//	private tableauParameters getLastTableauParameters() {
//		return (tableauParameters) fPivotLocations.lastElement();
//		}
		
//	private tableauParameters getTableauParametersForRow(int row) {
//	
//			// unfortunately, we have to walk the fPivotLocations vector to get the
//			// right set of tableau parameters.  this is not very efficient --
//			// the next iteration of code might look more carefully at this,
//			// but with the size of the problems we consider, this inefficiency
//			// probably will not matter.
//			
//		int index = 0;
//		tableauParameters tp = (tableauParameters) fPivotLocations.elementAt(index);
//		while ((row<tp.getFirstPhysicalRow()) || (row>tp.getLastPhysicalRow())) {
//			tp = (tableauParameters) fPivotLocations.elementAt(++index);
//			}
//		return tp;
//		}
		
		
	private void duplicateLastTableau() {
		if (!fLocked)
			return;
		
		//LPAssistant.speak("execute Duplicate Last Tableau");
		//printDebugData();

		int rowCount = getRowCount();
		//LPAssistant.speak("current rowCount is "+rowCount);

//		tableauParameters tp = getLastTableauParameters();
		
		int rowsToAdd = fNumConstraints + 1;
		if (fHasDRow)
			++rowsToAdd;
		//LPAssistant.speak("number of rows to add is "+rowsToAdd);
		
		int rowStart = firstConstraintRowofLastTableau;
		int rowEnd = lastConstraintRowofLastTableau + 1;
		if (fHasDRow)
			++rowEnd;

		for (int i=rowStart; i<=rowEnd; i++) {
			Vector v = new Vector();
			for (int j=0; j<getColumnCount(); j++) {
				//LPAssistant.speak("i is "+i+"; j is "+j);
				BigRational br = (BigRational) super.getValueAt(i,j);
				v.add(br);
				}
			addRow(v);
			}

		firstConstraintRowofLastTableau += rowsToAdd;
		lastConstraintRowofLastTableau += rowsToAdd;

//		tableauParameters newtp = new tableauParameters(tp);
//		//newtp.offsetRowsBy(rowsToAdd);
//		fPivotLocations.addElement(newtp);
		fNumTableaux++;

		fireTableRowsInserted(rowCount, rowCount+rowsToAdd-1);
		updateMenus();

//		LPAssistant.speak("new rowCount is "+getRowCount());
//		LPAssistant.speak("new first constraint row is "+firstConstraintRowofLastTableau);
//		LPAssistant.speak("new last constraint row is "+lastConstraintRowofLastTableau);
		}

	
	// the handleClick method is exactly the duplicateLastTableau method,
	// except we throw in the pivot on the last tableau before displaying it
	
	private boolean canPivotAt(int row, int col) {
	
			// first rule out a click on any column that's not a regular variable column (why?)
			
//		if ((col==0) || (col>fNumRegularVariables))
//			return false;
			
		if (col==0)
			return false;
			
			// next be sure we're in a row that's in the last tableau
			// and in one of the rows that represents a constraint
			// this is easy to figure out, since all tableaux have the
			// same number of rows.
		
		if (row<getFirstConstraintRow() || row>getLastConstraintRow())
			return false;
			
			// next be sure we're not trying to pivot in the same column which is 
			// already the basic variable.  you cannot bring something into the basis
			// that's already there.
		
		if (col==getBasicVariableinRow(row))
			return false;
		
			// next, let's not pivot on a zero
		
		BigRational br = (BigRational) super.getValueAt(row,col);
		if (br.sign()==0)
			return false;
			
			
		//LPAssistant.speak("PRTTableModel.canPivotAt: firstrow " + getFirstConstraintRow() +
		//		", last row "  + (getRowCount()-1));

				
		return true;
		}

	public void handleBasisChange(int row, Point theClickPoint) {
		LPAssistant.speak("PRTTableModel.handleBasisChange for row "+row);
		LPAssistant.speak("Click at (" + theClickPoint.x + "," + theClickPoint.y + ")");
		
		// build popupmenu for allowable choices in this row
		// the first item is to add/remove an artifical variable
		
		
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.addSeparator();
        JMenuItem menuItem;

		if (rowHasArtificialVariable(row)) {
			menuItem = new JMenuItem("Remove Artifical Variable");
			myPopupMenuActionListener myMAL = new myPopupMenuActionListener();
			myMAL.setReferencesforRemove(this, getBasicVariableinRow(row));
			menuItem.addActionListener(myMAL);
			popupMenu.add(menuItem);

			if (getNumConstraints()>1) {
				popupMenu.addSeparator();
				menuItem = new JMenuItem("Remove This Constraint");
				myMAL = new myPopupMenuActionListener();
				myMAL.setReferencesforRemoveConstraint(this, row);
				menuItem.addActionListener(myMAL);
				popupMenu.add(menuItem);
				}
			}
			
		else {
			menuItem = new JMenuItem("Add Artificial Variable");
			myPopupMenuActionListener myMAL = new myPopupMenuActionListener();
			myMAL.setReferencesforAdd(this, row);
			menuItem.addActionListener(myMAL);
			popupMenu.add(menuItem);

			popupMenu.addSeparator();
			
			int howManyVars = getNumRegularVariables();
			for (int i=1; i<=howManyVars; i++) {
				if (fUsingX)
					menuItem = new JMenuItem("X"+i);
				else
					menuItem = new JMenuItem("Y"+i);
				popupMenu.add(menuItem);
				if (isVariableinBasis(i))
					menuItem.setEnabled(false);
				else {
					myMAL = new myPopupMenuActionListener();
					myMAL.setReferencesforChange(this, row);
					menuItem.addActionListener(myMAL);
					}
				}
				
			popupMenu.addSeparator();

			if (getNumConstraints()>1) {
				menuItem = new JMenuItem("Remove This Constraint");
				myMAL = new myPopupMenuActionListener();
				myMAL.setReferencesforRemoveConstraint(this, row);
				menuItem.addActionListener(myMAL);
				popupMenu.add(menuItem);
				}
			
				// allow clearing of variable if set
				
			int currentBasisElement = getBasicVariableinRow(row);
			if ((currentBasisElement>0) && (currentBasisElement<=fNumRegularVariables)) {
				popupMenu.addSeparator();
				menuItem = new JMenuItem("Clear Basic Variable");
				myMAL = new myPopupMenuActionListener();
				myMAL.setReferencesforClear(this, row);
				menuItem.addActionListener(myMAL);
				popupMenu.add(menuItem);
				}
			

			} // end of "else" for adding + listing variables
			
//		MouseListener popupListener = new PopupListener(popup);
//        fPRTTable.addMouseListener(popupListener);

		popupMenu.show(fPRTTable, theClickPoint.x, theClickPoint.y);
		}
	
	public void handleAttempttoAddConstraint(Point theClickPoint) {
		
		// build popupmenu with one item, to add a constraint
		
		JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem;

		popupMenu.addSeparator();
		menuItem = new JMenuItem("Add Constraint");
		popupMenu.add(menuItem);
		menuItem.addActionListener(this);
		popupMenu.show(fPRTTable, theClickPoint.x, theClickPoint.y);
		}

	public void computePivot(int rowStart, int rowEnd, int pivotRow, int pivotColumn) {
	
		// LPAssistant.speak("PRTTableModel.computePivot for first row "+rowStart+" and last row "+rowEnd);

	// first, normalize the pivot row

		BigRational thePivotValue = (BigRational) super.getValueAt(pivotRow,pivotColumn);
		for (int col=1; col<getColumnCount(); col++) {
			BigRational aNumber = (BigRational) super.getValueAt(pivotRow,col);
			setValueAt(aNumber.divide(thePivotValue),pivotRow,col);
			}
		setValueAt(new BigRational(pivotColumn),pivotRow,0);  // store basis variable index

	// second, for each of the other rows, walk across the row and subtract
	// an appropriate multiple of the pivot row from it (the multiple is the
	// same at each column)

		for (int row=rowStart; row<=rowEnd; row++) {
			if (row!=pivotRow) {
				BigRational theMultiple = (BigRational) super.getValueAt(row,pivotColumn);
				for (int col=1; col<getColumnCount(); col++) {
					BigRational aNumber1 = (BigRational) super.getValueAt(pivotRow,col);
					BigRational aNumber2 = aNumber1.multiply(theMultiple);
					BigRational aNumber3 = (BigRational) super.getValueAt(row,col);
					setValueAt(aNumber3.subtract(aNumber2),row,col);
					}
				}
			}
			
		}
		
	private boolean rowIsInDRow(int row) {
		if (!fHasDRow)
			return false;
		int rowremainder = (row % getTableauRowCount());
		return (rowremainder==(getTableauRowCount()-1));
		}

	
	public Object getValueAt(int row, int col) {
		Object obj = super.getValueAt(row,col);
		
		// first handle the case of a number, which is everything 
		// except the basic variable column.  we can tweak here depending
		// on the value of fAccuracy
		
		if (col>0)
			if (fAccuracy==0)
				return obj;
			else {
				String str = ((BigRational)obj).toStringDot(fAccuracy);
				return str;
				}

			// so this is a request to post something in column zero, which
			// is the basic variable label column.  we need to know if the entry
			// in this physical row needs a label.  since all tableau have the
			// same configuration, this is easy to figure out (?)
		
		int rowoffset = (row % getTableauRowCount()) + 1;
		//LPAssistant.speak("PRTTableModel.getValueAt remainder "+rowoffset);
		
		if (rowoffset==getTableauRowCount())
			return "";
		if (fHasDRow && (rowoffset==(getTableauRowCount()-1)))
			return "";
			
			// so we just need to add a label. 
		
		String str = obj.toString();
		if (fUsingX)
			if (str=="0")
				return "X";
			else
				return "X"+str;
		else
			if (str=="0")
				return "Y";
			else
				return "Y"+str;
		}
	
	public String getColumnName(int col) {
		if (col==0)
			return "Basis";
		if (col==(getColumnCount()-1))
			return "RHS";
		else if (fUsingX)
			return ("X" + col);
		else 
			return ("Y" + col);
		}

	
		// with this new design where every tableau has the same setup and only the
		// first tableua can be editied, it's pretty simple to determine what
		// can and cannot be edited.
		
	public boolean isCellEditable(int row, int col) {
		if ((fNumTableaux>1) || fLocked)
			return false;
		else if (col==0)	// this is the basic variable column
			return false;
		else if (col==getColumnCount()-1) // this is the "b" constant column
			return true;
		else if (col>fNumRegularVariables) // we cannot edit artificial variables
			return false;
		else if (rowIsInDRow(row))
			return false;
		else
			return true;
		}

	public boolean isLocked() {
		return fLocked;
		}
		
		
	public void setUseX(boolean useX) {
		//LPAssistant.speak("change status");
		if (fUsingX != useX)
			fDataChanged = true;
		fUsingX = useX;
		fPRTFrame.setUseX(useX);
//		fPRTTable.repaint();
//		fPRTTable.getTableHeader().repaint(); // can't get this to work ... ???
		fireTableStructureChanged();  // and this resets all the column widths, so it's too much
		fDataChanged = true;
		}
	
	public void setValueAt(Object value, int row, int col) {
		super.setValueAt(value,row,col);
		fDataChanged = true;
		}

	public boolean getUseX() {
		return fUsingX;
		}

	public void setAccuracy(int accuracy) {
		if (fAccuracy != accuracy)
			fDataChanged = true;
		fAccuracy = accuracy;
		fPRTTable.updateRowHeight();
		fireTableDataChanged();
		fPRTFrame.setAccuracy(accuracy);
		fDataChanged = true;
		}
		
	public int getAccuracy() {
		return fAccuracy;
		}
	

// ******************************************************************************
// **************************** PRINTING CODE ***********************************

	public void doPrint() {
	
		setLocked(true);	//  let's always use the nicer output format
	
		// takes us to a wierd sequence, with the only output as a file
		// option being postscript?
		
//		try {
//			fPRTTable.print();
//			} 
//		catch(PrinterException pe) {
//			LPAssistant.speak("Error printing: " + pe);
//			}
		
		PrinterJob printJob = PrinterJob.getPrinterJob();
		
//		LPAssistant.speak("User name is " + printJob.getUserName());
//		LPAssistant.speak("Job name is " + printJob.getJobName());
		
		fPrintUserName = printJob.getUserName();
		PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
		PageFormat thePageFormat = printJob.pageDialog(attributes);
		printJob.setPrintable(this,thePageFormat);
		if ((thePageFormat!=null) && printJob.printDialog())
			try {
				printJob.print();
				} 
			catch(PrinterException pe) {
				LPAssistant.speak("Error printing: " + pe);
				}
		}

	public void printPage(Graphics2D g2, PageFormat pf, int fromRow, int toRow) {

		//LPAssistant.speak("printPage:from row " + fromRow + " to " + toRow);
		
		int pageXStart = (int)pf.getImageableX();
		int pageYStart = (int)pf.getImageableY();

			// first, the header row -- use rectangles from the zero row for column widths
		
		for (int col=1; col<getColumnCount()-1; col++) {
			Rectangle cellRect = fPRTTable.getCellRect(0, col, false);  // false = no spacing
			cellRect.x += pf.getImageableX();
			cellRect.y += pf.getImageableY();
			String s = (new Integer(col)).toString();	
			if (fUsingX)
				s = "X"+s;
			else
				s = "Y"+s;
			paintBasicVariable(g2, cellRect, s);
			}
			
			// now do the row cells.  use the rectangles from the table only for height and width
			// but manage the horizontal and vertical placing as we go.  we also want to compute
			// the full width of the tableau, so we'll get that now while we draw
			
		int tableauWidth = 0;
		int rowYOffset = fPRTTable.getRowHeight();
		int rectYThisRow = (int) pf.getImageableY() + rowYOffset; // we used a line above for the header	
		if (toRow>=getRowCount())
			toRow = getRowCount()-1;
 		for (int i=fromRow; i<=toRow; i++) {
			int rectXThisColumn = (int) pf.getImageableX();
 			for (int j=0; j<getColumnCount(); j++) {
				Rectangle cellRect = fPRTTable.getCellRect(i, j, true);  // i only need the width and height
				cellRect.x = rectXThisColumn;
				rectXThisColumn += cellRect.width;
				if (rectXThisColumn>tableauWidth)
					tableauWidth = rectXThisColumn;	// we're cheating, computing width of whole tableau
				cellRect.y = rectYThisRow;
//				LPAssistant.speak("printPage: cellRect is " + cellRect.toString());
				String s = getValueAt(i,j).toString();
				if (s.startsWith("X"))
					paintBasicVariable(g2,cellRect,s);
				else if (s.startsWith("Y"))
					paintBasicVariable(g2,cellRect,s);
				else if (s.indexOf("/", 0)<0)	// take no special action if we have an integer
					paintInteger(g2,cellRect,s);
				else
					paintRationalNumber(g2,cellRect,s);
				}
			rectYThisRow += rowYOffset;
			}

			// next, draw the horizontal lines.

		g2.drawLine(pageXStart, (int)pf.getImageableY()+rowYOffset, pageXStart+tableauWidth, (int)pf.getImageableY()+rowYOffset);
		g2.drawLine(pageXStart, (int)pf.getImageableY()+rowYOffset+1, pageXStart+tableauWidth, (int)pf.getImageableY()+rowYOffset+1);
		int yOffset1 = (int)pf.getImageableY() + rowYOffset*(fNumConstraints+1);
		int yOffset2 = yOffset1 + rowYOffset;	
		if (fHasDRow)
			yOffset2 += rowYOffset;
		for (int i=1; i<=(toRow-fromRow+1); i += getTableauRowCount()) {
			g2.drawLine(pageXStart, yOffset1, pageXStart+tableauWidth, yOffset1);
			g2.drawLine(pageXStart, yOffset2, pageXStart+tableauWidth, yOffset2);
			g2.drawLine(pageXStart, yOffset2+1, pageXStart+tableauWidth, yOffset2+1);
			yOffset1 += rowYOffset * getTableauRowCount();
			yOffset2 += rowYOffset * getTableauRowCount();
			}
			
			// next, draw the vertical lines.

		Rectangle r = fPRTTable.getCellRect(0,0,true);	// true = include column spacing
		g2.drawLine(pageXStart+r.width,pageYStart+rowYOffset,pageXStart+r.width, pageYStart+(toRow-fromRow+2)*rowYOffset);
		r = fPRTTable.getCellRect(0,getColumnCount()-1,true);
		g2.drawLine(pageXStart+r.x,pageYStart+rowYOffset,pageXStart+r.x, pageYStart+(toRow-fromRow+2)*rowYOffset);
		if (getNumArtificialVariables()>0) {
			r = fPRTTable.getCellRect(0,getNumRegularVariables()+1,true);
			g2.drawLine(pageXStart+r.x,pageYStart+rowYOffset,pageXStart+r.x, pageYStart+(toRow-fromRow+2)*rowYOffset);
			}
			
			// finally, draw the pivot locations
						
		//Vector v = getPivotLocations();
		int numPivotstoDraw = fPivotLocations.size();
		//LPAssistant.speak("printPage: numPivotstoDraw is " + numPivotstoDraw);
		for (int i=0; i<numPivotstoDraw; i++) {
			Point pt = (Point) fPivotLocations.elementAt(i);
			if ((fromRow<=pt.x) && (pt.x<=toRow)) {
				//LPAssistant.speak("draw pivot loc at (" + pt.x + ", " + pt.y + ")");
				r = fPRTTable.getCellRect(pt.x, pt.y, false);  // false = no spacing
				r.y -=  fromRow*rowYOffset;	// offset based on which page we're on
				//LPAssistant.speak("rectangle is " + r.toString());
				g2.drawOval(r.x+pageXStart, r.y+pageYStart+rowYOffset, r.width, r.height);
				}
			}
			
		}
		
	public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
		Graphics2D g2 = (Graphics2D) g;
		
									
		LPAssistant.speak("page index is "+pageIndex);
		if (pageIndex==0) {	
		
				// determine page parameters here, just once, for this and all pages to follow
				// remember to leave room for the header row ...
				
//			LPAssistant.speak("Imageable start is ("+pf.getImageableX()+","+pf.getImageableY()+")");
//			LPAssistant.speak("Imageable height of page is "+pf.getImageableHeight());
			fNumRowsPerPage = (int) pf.getImageableHeight()/fPRTTable.getRowHeight() - 2;	// 1 row for header, 1 for page and time info at bottom
//			LPAssistant.speak("# rows per page is "+fNumRowsPerPage);
			fNumRowsPerPage -= fNumRowsPerPage % getTableauRowCount();	// adjust to be a multiple of rows in 1 tableau
//			LPAssistant.speak("adjusted # rows per page is "+fNumRowsPerPage);
			fPageCount = (int) getRowCount()/fNumRowsPerPage + 1;
			Date thisDateAndTime = new Date();
			fPrintStamp = "Page" + (pageIndex+1) + " of " + fPageCount + " printed on " +
							DateFormat.getDateInstance(DateFormat.LONG).format(thisDateAndTime) +
							" by " + fPrintUserName +
							" at " + 
							DateFormat.getTimeInstance(DateFormat.LONG).format(thisDateAndTime);
			}
		
		if (pageIndex<fPageCount) {
			Font theFont = fPRTTable.getFont();
			g2.setFont(new Font(theFont.getName(),theFont.getStyle(), theFont.getSize()));
			printPage(g2, pf, pageIndex*fNumRowsPerPage, pageIndex*fNumRowsPerPage + fNumRowsPerPage - 1);
//			Font theFont = g2.getFont();
			g2.setFont(new Font(theFont.getName(),Font.ITALIC, 9));

			g2.drawString(
				fPrintStamp, 
				(int) (pf.getImageableX()), 
				(int) (pf.getImageableY()+pf.getImageableHeight()-2));
			return PAGE_EXISTS;
			}
		else
			return NO_SUCH_PAGE;
		}


	public void setDataForExample(int theExampleorTable) {
	
			// currently the only possible values for theExampleorTable are 1 or 2; the
			// only values we have to change are those which are non-zero, since
			// a new table has all zeroes.
			
		fInputArtificialVariableRows.clear();
		fInputPivotLocations.clear();

		if (theExampleorTable==1) {
			setValueAt(new BigRational(3), 0, 0);	// basis element in first row
			setValueAt(new BigRational(-6), 0, 1);
			setValueAt(new BigRational(1), 0, 3);
			setValueAt(new BigRational(-2), 0, 4);
			setValueAt(new BigRational(2), 0, 5);
			setValueAt(new BigRational(6), 0, 6);
			
			setValueAt(new BigRational(2), 1, 0);	// basis element in second row
			setValueAt(new BigRational(-3), 1, 1);
			setValueAt(new BigRational(1), 1, 2);
			setValueAt(new BigRational(6), 1, 4);
			setValueAt(new BigRational(3), 1, 5);
			setValueAt(new BigRational(15), 1, 6);
			
			setValueAt(new BigRational(5), 2, 1);
			setValueAt(new BigRational(3), 2, 4);
			setValueAt(new BigRational(-2), 2, 5);
			setValueAt(new BigRational(-21), 2, 6);
			}
		else if (theExampleorTable==2) {
			setValueAt(new BigRational(1), 0, 1);
			setValueAt(new BigRational(-2), 0, 2);
			setValueAt(new BigRational(-3), 0, 3);
			setValueAt(new BigRational(-2), 0, 4);
			setValueAt(new BigRational(3), 0, 5);
			
			setValueAt(new BigRational(1), 1, 1);
			setValueAt(new BigRational(-1), 1, 2);
			setValueAt(new BigRational(2), 1, 3);
			setValueAt(new BigRational(1), 1, 4);
			setValueAt(new BigRational(11), 1, 5);
			
			setValueAt(new BigRational(2), 2, 1);
			setValueAt(new BigRational(-3), 2, 2);
			setValueAt(new BigRational(1), 2, 3);
			setValueAt(new BigRational(1), 2, 4);
			
			fInputArtificialVariableRows.add(new Integer(0));	// artificial variable in row 0
			fInputArtificialVariableRows.add(new Integer(1));	// artificial variable in row 1
			}
		}
	
	
	public void setLocked(boolean lock) {
		if (fNumTableaux>1)	// can't edit after pivoting has started
			return;
			
		
//		Font theFont = fPRTTable.getFont();
//		//LPAssistant.speak("Font is currently " + theFont.getName());
//		//LPAssistant.speak("its style is " + theFont.getStyle());
//		//LPAssistant.speak("its size is " + theFont.getSize());
//		if (lock) {
//			fPRTTable.clearSelection();
//			fPRTTable.setFont(new Font(theFont.getName(),Font.PLAIN, LPAssistant.PIVOTMODEFONTSIZE));
//			//fPRTTable.setFont(new Font("Serif",Font.PLAIN, LPAssistant.PIVOTMODEFONTSIZE));
//			
//			int newRowHeight = LPAssistant.PIVOTROWNOFRACTIONHEIGHT;
//			if (fAccuracy==0)
//				newRowHeight += LPAssistant.PIVOTROWFRACTIONADJUSTHEIGHT;
//			fPRTTable.setRowHeight(newRowHeight);
//			}
//		else {
//			fPRTTable.setFont(new Font(theFont.getName(),Font.PLAIN, LPAssistant.EDITMODEFONTSIZE));
//			//fPRTTable.setFont(new Font("SansSerif",Font.PLAIN, LPAssistant.EDITMODEFONTSIZE));
//			fPRTTable.setRowHeight(LPAssistant.EDITROWHEIGHT);
//			}
		fLocked = lock;
		fPRTTable.updateRowHeight();
		fPRTTable.setLocked(fLocked);
		fPRTFrame.setLocked(fLocked);
		if (fLocked) {
			recomputeDRow();	// sort of a hack, really
			}

//		fireTableCellUpdated(getRowCount()-1,0);  //shows result on-screen
		fDataChanged = true;
		}
	
	private void updateMenus() {
		fPRTFrame.updateMenus();
		}
		
// *************************************************************
// ******************* FILE INPUT AND OUTPUT *******************


	public void updateInputAVsandPivots() {
		for (int i=0; i<fInputArtificialVariableRows.size(); i++) {
			Integer intObject = (Integer) fInputArtificialVariableRows.elementAt(i);
			doAddArtificialVariable(intObject.intValue());
			}
		for (int i=0; i<fInputPivotLocations.size(); i++) {
			Point pt = (Point) fInputPivotLocations.elementAt(i);
			doPivotAt(pt.x,pt.y);
			}
		}
	
	private void readData(File fromFile) throws IOException {
		BufferedReader bReader = new BufferedReader(new FileReader(fromFile));
		
		String line = bReader.readLine();
		if (!line.equals("LP Assistant. File format of September 24, 2007."))
			throw new IOException();

		line = bReader.readLine();
		fNumConstraints = (new Integer(line)).intValue();
		line = bReader.readLine();
		fNumRegularVariables = (new Integer(line)).intValue();
		line = bReader.readLine();
		fUsingX = (line.equals("X"));
		line = bReader.readLine();
		fAccuracy = (new Integer(line)).intValue();

		setRowCount(fNumConstraints+1);
		setColumnCount(fNumRegularVariables+2);
		lastConstraintRowofLastTableau = fNumConstraints-1;

		for (int r=0; r<fNumConstraints+1; r++)
			for (int c=0; c<=fNumRegularVariables+1; c++) {
				line = bReader.readLine();
				BigRational br = new BigRational(line);
				setValueAt(br, r, c); 
				}
				
		fInputArtificialVariableRows.clear();
		line = bReader.readLine();
		int artificialVariablestoAdd = (new Integer(line)).intValue();
		for (int i=0; i<artificialVariablestoAdd; i++) {
			line = bReader.readLine();
			fInputArtificialVariableRows.add(new Integer(line));
			}

		fInputPivotLocations.clear();
		line = bReader.readLine();
		int pivotLocationstoAdd = (new Integer(line)).intValue();
		for (int i=0; i<pivotLocationstoAdd; i++) {
			line = bReader.readLine();
			int x = (new Integer(line)).intValue();
			line = bReader.readLine();
			int y = (new Integer(line)).intValue();
			fInputPivotLocations.add(new Point(x,y));
			}

		bReader.close();
		}

	private void writeData(File saveFile) throws IOException {
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(saveFile));
		
			// start with line of information so we know it's us and so
			// we can identify the file format.
		
		bWriter.write("LP Assistant. File format of September 24, 2007.");
		bWriter.newLine();

			// start with the basic parameters: number of constraints, number
			// of variables, which algorithm we use, and what accuracy 
			
		bWriter.write((new Integer(fNumConstraints)).toString());
		bWriter.newLine();
		bWriter.write((new Integer(fNumRegularVariables)).toString());
		bWriter.newLine();
		if (fUsingX)
			bWriter.write("X");
		else
			bWriter.write("Y");
		bWriter.newLine();

		bWriter.write((new Integer(fAccuracy)).toString());
		bWriter.newLine();
		
			// next add all data from the first tableau, including 
			// basic variable info from column 0.  we need not add any
			// values associated with artificial variables or the D row, 
			// if it exists, since we can recompute these later; but we do
			// need to strip out any artificial variable on the way out, because
			// the code to add an artificial variable checks for the current 
			// basis variable value before executing
						
		for (int r=0; r<fNumConstraints+1; r++) {
			int bv = getBasicVariableinRow(r);
			if (bv<=fNumRegularVariables)
				bWriter.write((new Integer(bv)).toString());
			else
				bWriter.write("0");
			bWriter.newLine();
			for (int c=1; c<=fNumRegularVariables; c++) {
				BigRational br = (BigRational) super.getValueAt(r, c); 
				bWriter.write(br.toString());
				bWriter.newLine();
				}
			BigRational br = (BigRational) super.getValueAt(r, getColumnCount()-1); 
			bWriter.write(br.toString());
			bWriter.newLine();
			}
			
			// the next thing to record is the state of any artificial variables
			// write the number we have, plus where the rows in which
			// they're located.  Note that at the moment, the AVs may come
			// back renumbered, since they are numbered according to the order
			// in which added upon recreation.  (there's no great problem at the moment ...)
			
		bWriter.write((new Integer(fNumArtificialVariables)).toString());
		bWriter.newLine();
		if (fNumArtificialVariables>0)
			for (int r=0; r<fNumConstraints; r++)
				if (getBasicVariableinRow(r)>fNumRegularVariables) {
					bWriter.write((new Integer(r)).toString());	// which row has an AV
					bWriter.newLine();
					}


			// finally, record all pivot positions so they can be recreated
			
		int pivotCount = fPivotLocations.size();
		bWriter.write((new Integer(pivotCount)).toString());
		bWriter.newLine();
		for (int i=0; i<pivotCount; i++) {
			Point pt = (Point) fPivotLocations.elementAt(i);
			bWriter.write((new Integer(pt.x)).toString());
			bWriter.newLine();
			bWriter.write((new Integer(pt.y)).toString());
			bWriter.newLine();
			}
		
		
		bWriter.close();
		}
		
		
// *************************************************************
// ********************* FONT AND SIZE CODING ******************

	public void setFontAndSize(String theFontName, int theSize) {
		fBaseFontSize = theSize;
		fFontName = theFontName;
		fPRTTable.setFontAndSize(theFontName,theSize);
		}

// *************************************************************
// ******************* PREVIOUS/NO LONGER USED *****************
	

//	public void doResize() {
//     	SliderDemo3 demo = new SliderDemo3();
//		demo.createAndShowGUI();


//		PRTTableau lastTableau = (PRTTableau) fTableauVector.lastElement();
//		PRTSizeDialog cstDlg = new PRTSizeDialog(2,3);
//		int[] results = cstDlg.showandrun();
//		LPAssistant.speak(results[0]);
//		LPAssistant.speak(results[1]);
//		
//		if (results[0]==0) // dialog dismissed or cannot parse (?)
//			return;
		
//		int deltaRows = lastTableau.getNConstraints()-results[0];
//		int deltaCols = lastTableau.getNVariables()-results[1];
//
//		if (deltaRows!=0)
//			addRows(deltaRows);
//		if (deltaCols!=0)
//			addColumns(deltaCols);
//		
//		fireTableStructureChanged();
//		updateMenus();
			
//		}

//	private void printDebugData() {
//			
// 		int numRows = getRowCount();
// 		int numCols = getColumnCount();
// 		for (int i=0; i < numRows; i++) {
// 			System.out.print("    row " + i + ":");
// 			for (int j=0; j < numCols; j++)
// 				System.out.print("  " + super.getValueAt(i,j).getClass());
// 			LPAssistant.speak();
// 			}
//		LPAssistant.speak("--------------------------");
//		}


//	public int fractionAdjust(int theBaseSize) {
//		return theBaseSize+6;
//		}

	public int getFractionTermSize() {
		int termSize;
		switch (fBaseFontSize) {
			case 9: termSize = 9; break;
			case 10: termSize = 9; break;
			case 12: termSize = 10; break;
			case 14: termSize = 12; break;
			case 18: termSize = 14; break;
			default: termSize = 10;
			}
		return termSize;
		}
		
	private int getSubscriptSize() {
		int subscriptSize;
		switch (fBaseFontSize) {
			case 9: subscriptSize = 9; break;
			case 10: subscriptSize = 9; break;
			case 12: subscriptSize = 10; break;
			case 14: subscriptSize = 10; break;
			case 18: subscriptSize = 12; break;
			default: subscriptSize = 10;
			}
		return subscriptSize;
		}

	public void paintBasicVariable(Graphics2D g2, Rectangle r, String s) {
		
		g2.setFont(new Font(fFontName, Font.ITALIC, fBaseFontSize));
		g2.setColor(Color.black); 

		FontMetrics fm = g2.getFontMetrics();
		int xcenter = r.x+r.width/2;
		int yBaseline = r.y + (r.height+fm.getAscent())/2;
		
//		LPAssistant.speak("Font size is " + fBaseFontSize);
//		LPAssistant.speak("Font ascent is " + fm.getAscent());
//		LPAssistant.speak("Font descent is " + fm.getDescent());
//		LPAssistant.speak("Font leading is " + fm.getLeading());
//		LPAssistant.speak("Font height is " + fm.getHeight());
		
		int widthXorY, widthSubscript;
		boolean startsWithX = s.startsWith("X");
		
		int k = s.length();
		if (k<=1) {
				// then we're only printing just the X or the Y, so here the
				// width of the (empty) subscript will be zero
			widthSubscript = 0;
			}
		else {
				// we strip off the first character from the string
			if (k==2)
				s = s.substring(1);
			else
				s = s.substring(1,k);			
			widthSubscript = fm.stringWidth(s);
			}
			
		
		if (startsWithX)
			widthXorY = fm.stringWidth("x");
		else
			widthXorY = fm.stringWidth("y");
		if (startsWithX)
			g2.drawString("x",xcenter-(widthXorY+widthSubscript)/2-1,yBaseline);
		else
			g2.drawString("y",xcenter-(widthXorY+widthSubscript)/2-1,yBaseline);
			
			// reduce font size for the subscript
		
		if (widthSubscript>0) {
			int offset = 3;
			if (fBaseFontSize>=12)
				offset = 4;
			g2.setFont(new Font(fFontName, Font.PLAIN, getSubscriptSize()));
			g2.drawString(s,xcenter-(widthXorY+widthSubscript)/2+widthXorY,yBaseline+offset);
			}
		}

	public void paintInteger(Graphics2D g2, Rectangle r, String s) {
		
		g2.setFont(new Font(fFontName, Font.PLAIN, fBaseFontSize));
		FontMetrics fm = g2.getFontMetrics();
		int yBaseline = r.y + (r.height+fm.getAscent())/2;
		int xcenter = r.x + r.width/2;
		int widthInteger = fm.stringWidth(s);
		g2.setColor(Color.black); 
		g2.drawString(s,xcenter-(widthInteger)/2,yBaseline);
		}

	public void paintRationalNumber(Graphics2D g2, Rectangle r, String s) {

		int ycenter = r.y + r.height/2;		// find the center of the rectangle
		int xcenter = r.x + r.width/2;

			// first sort out the pieces that are to be printed ...
			// i.e., strip off sign and remember it, then pick out numerator and denominator
			
		boolean negative = (s.startsWith("-"));
		if (negative)
			s = s.substring(1);
		int index = s.indexOf("/", 0);		
		String numerator = s.substring(0,index);		
		String denominator = s.substring(index+1,s.length());
		
		g2.setFont(new Font(fFontName, Font.PLAIN, getFractionTermSize()));
		FontMetrics fm = g2.getFontMetrics();
		int numeratorWidth = fm.stringWidth(numerator);
		int denominatorWidth = fm.stringWidth(denominator);
		int fractionWidth = numeratorWidth;
		if (denominatorWidth>numeratorWidth)
			fractionWidth = denominatorWidth;

		g2.setColor(Color.black); 

		int signWidth = 0;
		if (negative) {
//			signWidth = 5;
//			g2.drawLine(xcenter-fractionWidth/2-signWidth, ycenter, xcenter-fractionWidth/2-signWidth+3, ycenter);
			g2.setFont(new Font(fFontName, Font.PLAIN, fBaseFontSize));
			fm = g2.getFontMetrics();
			signWidth = fm.stringWidth("-")+3;	// add a little space before fraction
			g2.drawString("-", xcenter-(signWidth+fractionWidth)/2, ycenter+4); // sign is too high to match line (so, draw the line myself?)
			}

		g2.setFont(new Font(fFontName, Font.PLAIN, getFractionTermSize()));
		fm = g2.getFontMetrics();
		g2.drawString(numerator, xcenter+(signWidth-numeratorWidth)/2, ycenter-3);
		g2.drawString(denominator, xcenter+(signWidth-denominatorWidth)/2, ycenter+fm.getAscent()+2);
		g2.drawLine(xcenter+(signWidth-fractionWidth)/2, ycenter, xcenter+(signWidth+fractionWidth)/2, ycenter);
		}
	

}
