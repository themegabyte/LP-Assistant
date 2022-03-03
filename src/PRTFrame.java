import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.border.*;

import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner;
import javax.swing.JComboBox;

import javax.swing.table.*;

	
public class PRTFrame extends JInternalFrame implements AdjustmentListener {

	private LPAssistant fApplication;
	private PRTTableModel fTableModel;
	private PRTTable fPRTTable;
	
	private JMenuItem fAddConstraintMenuItem, fAddRegularVariableMenuItem, fRemoveLastTableauMenuItem;
	private JMenu fRemoveVariableSubMenu, fRemoveConstraintSubMenu;
	private JRadioButton fEditingAllowedButton, fPivotingAllowedButton;
	private JRadioButton fSimplexButton, fDualSimplexButton;
	private JRadioButton fAccButton0, fAccButton1, fAccButton2, fAccButton3;
	private JLabel fRatioLabel;
	private JScrollPane fScrollPane;
	
   static final int xPosition = 30, yPosition = 30;

	public PRTFrame() {
		super();
		}

	public PRTFrame(LPAssistant theApplication, PRTTableModel theModel, int frameNumber) {
	
		super("Untitled Problem " + frameNumber, 
                  true, //resizable
                  true, //closable
                  true, //maximizable
                  true);//iconifiable

		fApplication = theApplication;
		fTableModel = theModel;
		
		JPanel newContentPane = new JPanel();
		
	        
	    fPRTTable = new PRTTable(fTableModel);
		fTableModel.setTable(fPRTTable);
	    fPRTTable.setPreferredScrollableViewportSize(new Dimension(450, 150));
	   	//fPRTTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	   	fPRTTable.setCellSelectionEnabled(true);  // was true -- what changed?
	   	//newContentPane.setSelectionMode(table, ALLOW_ROW_SELECTION, ALLOW_COLUMN_SELECTION);
//	    table.setFillsViewportHeight(true);
//		fPRTTable.addFocusListener(fTableModel);
		addFocusListener(fTableModel);

			// needs work -- e.g., setting background color, adding lines,
			// lots of other little things too ...
			
//		for (int i=1; i<=fPRTTable.getColumnCount()-2; i++) {
//			TableColumnModel cm = fPRTTable.getColumnModel();
//			TableColumn tCol = cm.getColumn(i);
//			PRTTableCellRenderer headerRenderer = new PRTTableCellRenderer();
//			headerRenderer.setLocked(true);
//			tCol.setHeaderRenderer(headerRenderer);
//			}

		fScrollPane = new JScrollPane(fPRTTable);
//		fScrollPane.getVerticalScrollBar().setUnitIncrement(LPAssistant.EDITROWHEIGHT);
//		fScrollPane.getVerticalScrollBar().setBlockIncrement(LPAssistant.EDITROWHEIGHT);
		
//		MyAdjustmentListener listener = new MyAdjustmentListener();
//		listener.setModel(fTableModel);
//		fScrollPane.getVerticalScrollBar().addAdjustmentListener(listener);
		fScrollPane.getVerticalScrollBar().addAdjustmentListener(this);

//		GridBagConstraints c = new GridBagConstraints();
//		c.gridx = 0;
//		c.gridy = 0;
//		c.fill = GridBagConstraints.BOTH;
//		c.weightx = 1;
//		c.weighty = 1;
//		newContentPane.add(scrollPane,c);

		newContentPane.setLayout(new BorderLayout());
		newContentPane.add(fScrollPane,BorderLayout.CENTER);

			// build button group for switching edit<-->pivot mode
			
			
		

		fEditingAllowedButton = new JRadioButton("Edit");
		fEditingAllowedButton.setSelected(true);
		fEditingAllowedButton.addActionListener(fTableModel);
		fEditingAllowedButton.addKeyListener(fTableModel);

		fPivotingAllowedButton = new JRadioButton("Pivot");
		fPivotingAllowedButton.setSelected(false);
		fPivotingAllowedButton.addActionListener(fTableModel);		
		fPivotingAllowedButton.addKeyListener(fTableModel);

		ButtonGroup group = new ButtonGroup();
		group.add(fEditingAllowedButton);
		group.add(fPivotingAllowedButton);
		
		JPanel theEditPivotPanel = new JPanel();
		theEditPivotPanel.setLayout(new BoxLayout(theEditPivotPanel,BoxLayout.PAGE_AXIS));
		theEditPivotPanel.add(fEditingAllowedButton);
		theEditPivotPanel.add(fPivotingAllowedButton);
		theEditPivotPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

//		Border modeBorder = BorderFactory.createLoweredBevelBorder();
//		Border modeBorder = BorderFactory.createCompoundBorder(
//					BorderFactory.createRaisedBevelBorder(),BorderFactory.createLoweredBevelBorder());
//		theEditPivotPanel.setBorder(modeBorder);
//		theEditPivotPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		
			// build button group for switching X<-->Y algorithm mode

		fSimplexButton = new JRadioButton("Simplex");
		fSimplexButton.setSelected(true);
		fSimplexButton.addActionListener(fTableModel);
		fSimplexButton.addKeyListener(fTableModel);
		fSimplexButton.setToolTipText("Ratio will show as b/a in Pivot Mode");

		fDualSimplexButton = new JRadioButton("Dual Simplex");
		fDualSimplexButton.setSelected(false);
		fDualSimplexButton.addActionListener(fTableModel);		
		fDualSimplexButton.addKeyListener(fTableModel);
		fDualSimplexButton.setToolTipText("Ratio will show as c/a in Pivot Mode");

		ButtonGroup group2 = new ButtonGroup();
		group2.add(fSimplexButton);
		group2.add(fDualSimplexButton);
		
		JPanel theSimplexDualPanel = new JPanel();
		theSimplexDualPanel.setLayout(new BoxLayout(theSimplexDualPanel,BoxLayout.PAGE_AXIS));
		theSimplexDualPanel.add(fSimplexButton);
		theSimplexDualPanel.add(fDualSimplexButton);
//		Border dualBorder = BorderFactory.createLoweredBevelBorder();
//		theSimplexDualPanel.setBorder(dualBorder);
		theSimplexDualPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

			// build button group for switching display accuracy

		fAccButton0 = new JRadioButton("1/2");
		fAccButton0.setSelected(true);
		fAccButton0.addActionListener(fTableModel);
		fAccButton0.addKeyListener(fTableModel);
		fAccButton0.setToolTipText("Shows exact values");

		fAccButton1 = new JRadioButton("1.0");
		fAccButton1.setSelected(false);
		fAccButton1.addActionListener(fTableModel);		
		fAccButton1.addKeyListener(fTableModel);
		fAccButton1.setToolTipText("Shows values to 1 decimal place");

		fAccButton2 = new JRadioButton("1.00");
		fAccButton2.setSelected(false);
		fAccButton2.addActionListener(fTableModel);		
		fAccButton2.addKeyListener(fTableModel);
		fAccButton2.setToolTipText("Shows values to 2 decimal places");

		fAccButton3 = new JRadioButton("1.000");
		fAccButton3.setSelected(false);
		fAccButton3.addActionListener(fTableModel);		
		fAccButton3.addKeyListener(fTableModel);
		fAccButton3.setToolTipText("Shows values to 3 decimal places");

		ButtonGroup group3 = new ButtonGroup();
		group3.add(fAccButton0);
		group3.add(fAccButton1);
		group3.add(fAccButton2);
		group3.add(fAccButton3);
		
		JPanel theAccuracyPanel = new JPanel();
		theAccuracyPanel.setLayout(new BoxLayout(theAccuracyPanel,BoxLayout.PAGE_AXIS));
		theAccuracyPanel.add(fAccButton0);
		theAccuracyPanel.add(fAccButton1);
		theAccuracyPanel.add(fAccButton2);
		theAccuracyPanel.add(fAccButton3);
//		Border accuracyBorder = BorderFactory.createLoweredBevelBorder();
//		theAccuracyPanel.setBorder(accuracyBorder);
		theAccuracyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		
		
			// now build out the left-hand column
   
		JPanel theButtonPanel = new JPanel();
		theButtonPanel.setLayout(new BoxLayout(theButtonPanel,BoxLayout.PAGE_AXIS));

		JLabel myLabel = new JLabel("Mode");
		myLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		theButtonPanel.add(myLabel);

		theButtonPanel.add(theEditPivotPanel);

		JLabel myLabel2 = new JLabel("Algorithm");
		myLabel2.setAlignmentX(Component.LEFT_ALIGNMENT);
		theButtonPanel.add(myLabel2);

		theButtonPanel.add(theSimplexDualPanel);

		JLabel myLabel3 = new JLabel("Display");
		myLabel3.setAlignmentX(Component.LEFT_ALIGNMENT);
		theButtonPanel.add(myLabel3);

		theButtonPanel.add(theAccuracyPanel);

		JLabel myLabel4 = new JLabel("Ratio");
		myLabel4.setAlignmentX(Component.LEFT_ALIGNMENT);
		myLabel4.setToolTipText("Ratio b/a or c/a will show here in Pivot Mode");
		theButtonPanel.add(myLabel4);

		fRatioLabel = new JLabel("             ");
		fRatioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		fRatioLabel.setToolTipText("Ratio b/a or c/a will show here in Pivot Mode");
		fRatioLabel.setBorder(BorderFactory.createLoweredBevelBorder());
		fRatioLabel.setMinimumSize(new Dimension(100, 30));
		fRatioLabel.setMaximumSize(new Dimension(100, 30));
		fRatioLabel.setPreferredSize(new Dimension(100, 30));
		fRatioLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		fRatioLabel.setVerticalAlignment(SwingConstants.CENTER);
		theButtonPanel.add(fRatioLabel);
		
		newContentPane.add(theButtonPanel,BorderLayout.WEST);
		
		Font theFont = myLabel.getFont();
		myLabel.setFont(new Font(theFont.getName(),Font.BOLD,LPAssistant.LABELFONTSIZE));
		myLabel2.setFont(new Font(theFont.getName(),Font.BOLD,LPAssistant.LABELFONTSIZE));
		myLabel3.setFont(new Font(theFont.getName(),Font.BOLD,LPAssistant.LABELFONTSIZE));
		myLabel4.setFont(new Font(theFont.getName(),Font.BOLD,LPAssistant.LABELFONTSIZE));
		fEditingAllowedButton.setFont(new Font(theFont.getName(),Font.PLAIN,
			LPAssistant.BUTTONFONTSIZE));
		fPivotingAllowedButton.setFont(new Font(theFont.getName(),Font.PLAIN,
			LPAssistant.BUTTONFONTSIZE));
		fSimplexButton.setFont(new Font(theFont.getName(),Font.PLAIN,
			LPAssistant.BUTTONFONTSIZE));
		fDualSimplexButton.setFont(new Font(theFont.getName(),Font.PLAIN,
			LPAssistant.BUTTONFONTSIZE));
		fAccButton0.setFont(new Font(theFont.getName(),Font.PLAIN,
			LPAssistant.BUTTONFONTSIZE));
		fAccButton1.setFont(new Font(theFont.getName(),Font.PLAIN,
			LPAssistant.BUTTONFONTSIZE));
		fAccButton2.setFont(new Font(theFont.getName(),Font.PLAIN,
			LPAssistant.BUTTONFONTSIZE));
		fAccButton3.setFont(new Font(theFont.getName(),Font.PLAIN,
			LPAssistant.BUTTONFONTSIZE));
		fRatioLabel.setFont(new Font(theFont.getName(), Font.PLAIN, LPAssistant.LABELFONTSIZE));

		
				
/// *******************  EXPERIMENTING WITH A SPINNER AND A COMBO BOX TO SEE
/// *******************  WHAT THEY LOOK LIKE; BUT THE PROBLEM IS THAT WHILE YOU
/// *******************  CAN CHANGE THEIR SIZE, THE SIZE OF THE UP/DOWN REMAINS
/// *******************  TOO BIG.  MORE RESEARCH IS NEEDED ?

		
//		SpinnerNumberModel model = new SpinnerNumberModel(2, 1, 50, 1); 
//		JSpinner spinner = new JSpinner(model);	
//		JSpinner.DefaultEditor anEditor = new JSpinner.DefaultEditor(spinner);
//		spinner.setEditor(anEditor);
//		spinner.setAlignmentX(Component.LEFT_ALIGNMENT);
//		spinner.setFont(new Font(theFont.getName(),Font.PLAIN,9));
//		anEditor.getTextField().setColumns(3);
//		anEditor.getTextField().setFont(new Font(theFont.getName(),Font.PLAIN,9));
//		theButtonPanel.add(spinner);
//		
//		JComboBox theCBox = new JComboBox(new String[] {"1/2", "1.0", "1.00", "1.000"});
//		theButtonPanel.add(theCBox);
//		theCBox.setAlignmentX(Component.LEFT_ALIGNMENT);
//		theCBox.setFont(new Font(theFont.getName(),Font.PLAIN,9));
		
/// *******************
		
	    newContentPane.setOpaque(true); //content panes must be opaque
	    setContentPane(newContentPane);
		//fPRTTable.requestFocusInWindow(); // DOES NOT WORK HERE -- NOT YET VISIBLE
		pack();

	    fPRTTable.setScrollPane(fScrollPane);
		fPRTTable.updateRowHeight();
		
		addMenus();
		setLocation(xPosition*frameNumber, yPosition*frameNumber);
		
		updateMenus();
	}


	public void adjustmentValueChanged(AdjustmentEvent evt) {
		fTableModel.fireTableDataChanged();	// cheap fix -- just draw everything!
		}

	public PRTTableModel getPRTTableModel() {
		return fTableModel;
		}
		
	public void updateRatioLabel(String newRatio) {
		fRatioLabel.setText(newRatio);
		}

	protected void addMenus() {

	   	JMenuBar menuBar;
	   	JMenu menu, submenu;
	   	JMenuItem menuItem;
	   	JRadioButtonMenuItem rbMenuItem;
	   	JCheckBoxMenuItem cbMenuItem;

	   	menuBar = new JMenuBar();

    	menu = new JMenu(" Tableau ");
		menu.setMnemonic(KeyEvent.VK_T);
    	menuBar.add(menu);

			// this group of "add" things should stay together ... and any time they're executed,
			// all need to be reexamined for state and submenu contents
			
    	menu.addSeparator();
		
		fAddConstraintMenuItem = new JMenuItem("Add Constraint");
    	menu.add(fAddConstraintMenuItem);
		fAddConstraintMenuItem.addActionListener(fTableModel);
		fAddConstraintMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,ActionEvent.META_MASK));
		
		fAddRegularVariableMenuItem = new JMenuItem("Add Regular Variable");
    	menu.add(fAddRegularVariableMenuItem);
		fAddRegularVariableMenuItem.addActionListener(fTableModel);
		fAddRegularVariableMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,ActionEvent.META_MASK));
		
    	menu.addSeparator();
		fRemoveVariableSubMenu = new JMenu("Remove Variable ...");
    	menu.add(fRemoveVariableSubMenu);

		fRemoveConstraintSubMenu = new JMenu("Remove Constraint ...");
    	menu.add(fRemoveConstraintSubMenu);

    	menu.addSeparator();
		fRemoveLastTableauMenuItem = new JMenuItem("Remove Last Tableau");
    	menu.add(fRemoveLastTableauMenuItem);
    	fRemoveLastTableauMenuItem.addActionListener(fTableModel);
		fRemoveLastTableauMenuItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_R,ActionEvent.META_MASK));

		setJMenuBar(menuBar);
		}

	public void updateMenus() {
	
			// most of this is determined simply by the number of tableaux, so we'll 
			// turn off everything first, basically assuming that we have more than one tableau
			// (in which case only the remove last tableau item is useable)

		fAddConstraintMenuItem.setEnabled(false);
		fAddRegularVariableMenuItem.setEnabled(false);
		fRemoveVariableSubMenu.removeAll();
		fRemoveVariableSubMenu.setEnabled(false);
		fRemoveConstraintSubMenu.removeAll();
		fRemoveConstraintSubMenu.setEnabled(false);
		fRemoveLastTableauMenuItem.setEnabled(true);
		fEditingAllowedButton.setEnabled(false);
		
			// now, if we guessed right, we're done ...
		
		if (fTableModel.getNumTableaux()>1)
			return;
		
			// so we have only one tableau.  now let's turn on the obvious stuff, then
			// go through the special cases ...
			
		fEditingAllowedButton.setEnabled(true);		// you can always swith to edit mode with 1 tableau
		fAddConstraintMenuItem.setEnabled(true);		// you can always add a constraint
		fAddRegularVariableMenuItem.setEnabled(true);	// you can always add a regular variable
		fRemoveLastTableauMenuItem.setEnabled(false);	// but you cannot remove the last (=first) tableau
			
			// for removing regular variables, you have to have at least 2 regular variables
			// in the problem (one variable problems are not very interesting).  and you can't remove
			// artificial variables if there aren't any.
			
		boolean canRemoveRegularVariables = (fTableModel.getNumRegularVariables()>2);
		boolean canRemoveArtificialVariables = (fTableModel.getNumArtificialVariables()>=1);
		if (canRemoveRegularVariables || canRemoveArtificialVariables) {
			fRemoveVariableSubMenu.setEnabled(true);
			int howManyVariables = fTableModel.getNumVariables();
			for (int i=1; i<=howManyVariables; i++) {
				JMenuItem menuItem = new JMenuItem("Variable "+i);
				fRemoveVariableSubMenu.add(menuItem);
				menuItem.addActionListener(fTableModel);
				}
			}

			// finally, for removing constraints, you can't have less than 1 constraint;
			// but otherwise, everything's removable
			
		if (fTableModel.getNumConstraints()>1) {
			fRemoveConstraintSubMenu.setEnabled(true);
			int howManyRows = fTableModel.getNumConstraints();
			for (int i=0; i<howManyRows; i++) {
				JMenuItem menuItem = new JMenuItem("From Row "+(i+1));
				fRemoveConstraintSubMenu.add(menuItem);
				menuItem.addActionListener(fTableModel);
				}
			}

		}

		public void doPrint() {
			fTableModel.doPrint();
			}
		

	public void setUseX(boolean useX) {
		fSimplexButton.setSelected(useX);
		fDualSimplexButton.setSelected(!useX);
		}

	public void setLocked(boolean lock) {
		fEditingAllowedButton.setSelected(!lock);
		fPivotingAllowedButton.setSelected(lock);
		fPRTTable.updateRowHeight();
//		if (lock) {
//			int newRowHeight = LPAssistant.PIVOTROWNOFRACTIONHEIGHT;
//			if (fTableModel.getAccuracy()==0)
//				newRowHeight += LPAssistant.PIVOTROWFRACTIONADJUSTHEIGHT;
//			fScrollPane.getVerticalScrollBar().setUnitIncrement(newRowHeight);
//			fScrollPane.getVerticalScrollBar().setBlockIncrement(newRowHeight);
//			}
//		else {
//			fScrollPane.getVerticalScrollBar().setUnitIncrement(LPAssistant.EDITROWHEIGHT);
//			fScrollPane.getVerticalScrollBar().setBlockIncrement(LPAssistant.EDITROWHEIGHT);
//			}
		}

	public void setAccuracy(int theAccuracy) {
		fAccButton0.setSelected(theAccuracy==0);
		fAccButton1.setSelected(theAccuracy==1);
		fAccButton2.setSelected(theAccuracy==2);
		fAccButton3.setSelected(theAccuracy==3);
		}

	public void scrollToLastTableau() {
	
		// the problem seems to be that the JTable hasn't gone through a paint()
		// yet (assuming we're calling this right after a pivot), so the 
		// JScrollbar hasn't seen the new measurements.  we should be able to
		// queue the scrollToLastTableau() call ???
	
//		fPRTTable.paintImmediately(fPRTTable.getBounds());
//		JScrollBar theScrollBar = fScrollPane.getVerticalScrollBar();
//		int deltaValue = fTableModel.getTableauRowCount()*fPRTTable.getRowHeight();
//		theScrollBar.setValue(theScrollBar.getValue()+deltaValue);
		
//		LPAssistant.speak("scroll bar value is " + theScrollBar.getValue());
//		LPAssistant.speak("scroll bar max is " + theScrollBar.getMaximum());
//		LPAssistant.speak("scroll bar height is " + theScrollBar.getHeight());
//		
//		BoundedRangeModel theModel = theScrollBar.getModel();
//		LPAssistant.speak("model min is " + theModel.getMinimum());
//		LPAssistant.speak("model value is " + theModel.getValue());
//		LPAssistant.speak("model extent is " + theModel.getExtent());
//		LPAssistant.speak("model max is " + theModel.getMaximum());
		}
	}
	
//class MyAdjustmentListener implements AdjustmentListener {
//
//	private PRTTableModel fModel;
//
//	public void setModel(PRTTableModel theModel) {
//		fModel = theModel;
//		}
//		
//	// This method is called whenever the value of a scrollbar is changed,
//	// either by the user or programmatically.
//	public void adjustmentValueChanged(AdjustmentEvent evt) {
//		//LPAssistant.speak(evt.paramString());
//		
////		Adjustable source = evt.getAdjustable();
//
//		fModel.fireTableDataChanged();	// cheap fix -- just draw everything!
//
//		// getValueIsAdjusting() returns true if the user is currently
//		// dragging the scrollbar's knob and has not picked a final value
////		if (evt.getValueIsAdjusting()) {
////			// The user is dragging the knob
////			return;
////		}
////		
////
////		// Determine which scrollbar fired the event
////		int orient = source.getOrientation();
////		if (orient == Adjustable.HORIZONTAL) {
////			// Event from horizontal scrollbar
////		} else {
////			// Event from vertical scrollbar
////		}
////
////		// Determine the type of event
////		int type = evt.getAdjustmentType();
////		switch (type) {
////		  case AdjustmentEvent.UNIT_INCREMENT:
////			  LPAssistant.speak("UNIT_INCREMENT");
////			  break;
////		  case AdjustmentEvent.UNIT_DECREMENT:
////			  LPAssistant.speak("UNIT_DECREMENT");
////			  break;
////		  case AdjustmentEvent.BLOCK_INCREMENT:
////			  LPAssistant.speak("BLOCK_INCREMENT");
////			  break;
////		  case AdjustmentEvent.BLOCK_DECREMENT:
////			  LPAssistant.speak("BLOCK_DECREMENT");
////			  break;
////		  case AdjustmentEvent.TRACK:
////			  LPAssistant.speak("TRACK");
////			  break;
////		}
//
//		// Get current value
//		int value = evt.getValue();
//	}
//}

