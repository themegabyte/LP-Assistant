import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.*;
import java.awt.print.*;
import javax.print.attribute.*;

import javax.swing.JFrame;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.*;
import javax.swing.JComboBox;

import javax.swing.filechooser.*;
import java.io.*;

import java.net.URL;
import java.net.URI;
import java.io.IOException;

import java.util.Vector;

import java.beans.*;

public class LPAssistant extends JFrame 
					implements ActionListener, InternalFrameListener, VetoableChangeListener, WindowListener {
	
//	public static int EDITMODEFONTSIZE = 12;
//	public static int EDITROWHEIGHT = 20;
//	public static int EDITROWHEIGHT = EDITMODEFONTSIZE+8;

	public static int LABELFONTSIZE = 10;
	public static int BUTTONFONTSIZE = 9;

//	public static int PIVOTMODEFONTSIZE = 10;
//	public static int PIVOTMODEFONTSIZE = 12;
//	public static int SUBSCRIPTFONTSIZE = 9;
//	public static int FRACTIONTERMFONTSIZE = 9;
//	public static int PIVOTROWNOFRACTIONHEIGHT = 19;
//	public static int PIVOTROWFRACTIONADJUSTHEIGHT = 5;

	private JDesktopPane fDesktopPane;
	private int fWindowPositionsUsed = 0;
	private JFrame fHelpWindow;
	private JInternalFrame fFontsWindow;
	private JEditorPane fHelpEditorPane;
	private JMenuItem fNewFromWindowMenuItem,
						fCloseMenuItem,
						fSaveMenuItem,
						fSaveAsMenuItem,
						fPrintMenuItem;
	private JComboBox fFontComboBox,fSizeComboBox;
	private String fDefaultFontName = "sansserif";
	private int fDefaultFontSize = 12;
	
	public LPAssistant() {
		super("LP Assistant");
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset*2, screenSize.height-inset*2);

        //Add a Window Exit Listener to quit when desktop is closed
        addWindowListener(this);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);	// we'll see the attempt to close as a windowClosing event
//	addWindowListener(new WindowAdapter() {
//						public void windowClosing(WindowEvent e) {
//							System.exit(0);
//							}
//						});

        fDesktopPane = new JDesktopPane(); //a specialized layered pane to be used with JInternalFrames
        setContentPane(fDesktopPane);
		setJMenuBar(createMenuBar());

        //Make dragging faster by setting drag mode to Outline
        fDesktopPane.putClientProperty("JDesktopPane.dragMode", "outline");
		//fDesktopPane.setDragMode(LIVE_DRAG_MODE);
		setVisible(true);

        createNewProblem(); //Create first LP problem window
		}


// **********************************************************************
// ************************ WINDOW EVENT LISTENING **********************

	public void windowActivated(WindowEvent e) {
		}
		
	public void windowClosed(WindowEvent e)  {
		}
		
	public void windowClosing(WindowEvent e) {
		//speak("window closing event received");
		handleQuitCommand();
		}
		
	public void windowDeactivated(WindowEvent e)  {
		}
		
	public void windowDeiconified(WindowEvent e)  {
		}
		
	public void windowIconified(WindowEvent e) {
		}
		
	public void windowOpened(WindowEvent e)  {
		}
		

	
	public static void main(String[] args) {
        try {
            String cn = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(cn); // Use the native L&F
			//UIManager.put("swing.boldMetal", Boolean.FALSE);
			}
		catch (Exception cnf) {
			}
			
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() { 
				LPAssistant frame = new LPAssistant(); 
				}
			});
		
        //LPAssistant frame = new LPAssistant();
        //frame.setVisible(true);
		}
		
    public void actionPerformed(ActionEvent e) {
	
		//LPAssistant.speak("Event received: " + e.getActionCommand());
		
		if (e.getActionCommand()=="Apply") {
			applyFontChange();
			return;
			}
		if (e.getActionCommand()=="Help") {
			showHelpWindow();
			return;
			}
		if (e.getActionCommand()=="Font and Size Selection") {
			showFontsWindow();
			return;
			}
		if (e.getActionCommand()=="Print Help Window") {
			printHelpWindow();
			return;
			}
		if (e.getActionCommand()=="Quit LP Assistant") {
			handleQuitCommand();
			return;
			}
		if (e.getActionCommand()=="New") {
			createNewProblem();
			return;
			}
		if (e.getActionCommand()=="Open...") {
			handleOpenCommand();
			return;
			}
		if (e.getActionCommand()=="Table 3.1") {	// eventually, this will be cleaner ??
			createNewProblemFromText(1);
			return;
			}
		if (e.getActionCommand()=="Example 3.6.1 (Table 3.7)") {
			createNewProblemFromText(2);
			return;
			}
						
			// for anything else, we will need to know who's on top ...
			// ... and let's be sure there is someone on top
			
        JInternalFrame theIFrame = fDesktopPane.getSelectedFrame();
		if (theIFrame==null)
			return;
		
		if (e.getActionCommand()=="Save As...")
			handleSaveAsCommand((PRTFrame)theIFrame);
		else if (e.getActionCommand().startsWith("Save"))
			handleSaveCommand((PRTFrame)theIFrame);
		else if (e.getActionCommand().startsWith("New from "))
			createNewProblem((PRTFrame)theIFrame);		
		else if (e.getActionCommand().startsWith("Close")) {
			handleCloseCommand(theIFrame);
			}	
		else if (e.getActionCommand().startsWith("Print"))
			((PRTFrame)theIFrame).doPrint();
		}

	private void applyFontChange() {
	
//	fDefaultFontName = "Sans Serif";
//	fDefaultFontSize = 12;

		String fontName = fFontComboBox.getSelectedItem().toString();
		String sizeName = fSizeComboBox.getSelectedItem().toString();
		speak("Font name is "+fontName);
		speak("Size is "+sizeName);
		
			// getAllFrames() returns Font and Size Selection at index 0, 
			// then and problem windows in order front to back
			
		JInternalFrame[] theFrames = fDesktopPane.getAllFrames(); 
		if (theFrames.length==1) { // i.e., Font and Size Selection window is the only one there
			fDefaultFontName = fontName;
			fDefaultFontSize = (new Integer(sizeName)).intValue();
			}
		else {
			PRTFrame theTopFrame = (PRTFrame) theFrames[1];
			theTopFrame.getPRTTableModel().setFontAndSize(fontName,(new Integer(sizeName)).intValue());
			}
		}
		
	private void createFontsWindow() {
		fFontsWindow = new JInternalFrame("Font and Size Selection", false, true);
		fFontsWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // dispose is important for seeing window events properly!
		fFontsWindow.setResizable(false);
//		fFontsWindow.setSize(new Dimension(420, 100));
		
		JPanel theContentPanel = new JPanel();
		theContentPanel.setLayout(new BoxLayout(theContentPanel,BoxLayout.LINE_AXIS));
		theContentPanel.setAlignmentX(Component.TOP_ALIGNMENT);
		theContentPanel.setOpaque(true); //content panes must be opaque
	    fFontsWindow.add(theContentPanel);
		fFontsWindow.setContentPane(theContentPanel);

			// inside this content panel, first put in a panel to hold the comboboxes 
			// for font and size, together with a label
		
		//theContentPanel.add(new JLabel("Font:"));
		
		GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String envfonts[] = gEnv.getAvailableFontFamilyNames();
		Vector v1 = new Vector();
		for (int i = 1; i < envfonts.length; i++) {
		  v1.addElement(envfonts[i]);
			}
		fFontComboBox = new JComboBox(v1);
		theContentPanel.add(fFontComboBox);
		
		Vector v2 = new Vector();
		v2.addElement("9");
		v2.addElement("10");
		v2.addElement("12");
		v2.addElement("14");
		v2.addElement("18");
		fSizeComboBox = new JComboBox(v2);
		theContentPanel.add(fSizeComboBox);

		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(this);
		//applyButton.setMaximumSize(new Dimension(100,20));
		theContentPanel.add(applyButton);
		
		fFontsWindow.pack();

		fFontsWindow.addVetoableChangeListener(this);
		fFontsWindow.addInternalFrameListener(this);
		Dimension theSize = fDesktopPane.getSize();
		Dimension theSize2 = fFontsWindow.getSize();
//		speak("Fonts window width = " + theSize2.width + ", height = " + theSize2.height);
//		speak("apply button height is " + applyButton.getSize().height);
		fFontsWindow.setLocation(10,theSize.height-theSize2.height-10); // 10 pixels in, 10 pixels from bottom (?)
		fFontsWindow.setVisible(true);

		fDesktopPane.add(fFontsWindow);		
        try {
            fFontsWindow.setSelected(true);
			} 
		catch (java.beans.PropertyVetoException e) {
			}
		}
	
	private void createHelpWindow() {
		fHelpWindow = new JFrame("Help for LP Assistant");
		fHelpWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		//fHelpWindow.setLayout(new BorderLayout());
		fHelpEditorPane = new JEditorPane();
        fHelpEditorPane.setEditable(false);
		fHelpEditorPane.setContentType("text/html");

        java.net.URL helpURL = LPAssistant.class.getResource("LPAssistantHelp.html");
        if (helpURL != null) {
            try {
                fHelpEditorPane.setPage(helpURL);
            } catch (IOException e) {
                speak("Attempted to read a bad URL: " + helpURL);
            }
        } else {
			fHelpEditorPane.setText("<br><br><font size=+2>Help file not present in Java Archive. Please add.</font>");
        }

        JScrollPane editorScrollPane = new JScrollPane(fHelpEditorPane);
        editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(new Dimension(500, 300));
        editorScrollPane.setMinimumSize(new Dimension(100, 100));
		
		fHelpWindow.add(editorScrollPane);
		fHelpWindow.pack();

        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        fHelpWindow.setBounds(screenSize.width-300-inset, inset, 300, 500);
		}
	
	
	protected JMenuBar createMenuBar() {

	   	JMenuBar menuBar;
	   	JMenu menu;
	   	JMenuItem menuItem;

	   	menuBar = new JMenuBar();

	   	menu = new JMenu(" Problem ");
		menu.setMnemonic(KeyEvent.VK_M);
		
		menuItem = new JMenuItem("New");
    	menu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.META_MASK));

		fNewFromWindowMenuItem = new JMenuItem("New from Window");
    	menu.add(fNewFromWindowMenuItem);
		fNewFromWindowMenuItem.addActionListener(this);
		fNewFromWindowMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.META_MASK+ActionEvent.SHIFT_MASK));
		
		fCloseMenuItem = new JMenuItem("Close");
    	menu.add(fCloseMenuItem);
		fCloseMenuItem.addActionListener(this);
		fCloseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,ActionEvent.META_MASK));

		menu.addSeparator();
		
		menuItem = new JMenuItem("Open...");
    	menu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.META_MASK));
		
		fSaveMenuItem = new JMenuItem("Save");
    	menu.add(fSaveMenuItem);
		fSaveMenuItem.addActionListener(this);
		fSaveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.META_MASK));
		
		fSaveAsMenuItem = new JMenuItem("Save As...");
    	menu.add(fSaveAsMenuItem);
		fSaveAsMenuItem.addActionListener(this);
		fSaveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.META_MASK+ActionEvent.SHIFT_MASK));

		menu.addSeparator();

		fPrintMenuItem = new JMenuItem("Print...");
    	menu.add(fPrintMenuItem);
		fPrintMenuItem.addActionListener(this);
		fPrintMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,ActionEvent.META_MASK));

		menu.addSeparator();

		menuItem = new JMenuItem("Quit LP Assistant");
    	menu.add(menuItem);
    	menuItem.addActionListener(this);
		
			// no key command for quit: when run on the Mac, we're running inside a Mac shell in which
			// the Q=quit key belongs to the shell
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,ActionEvent.META_MASK+ActionEvent.SHIFT_MASK));
	   	menuBar.add(menu);


	   	menu = new JMenu(" Useful Aids ");
		menu.setMnemonic(KeyEvent.VK_A);
				
		menuItem = new JMenuItem("Help");
    	menu.add(menuItem);
		menuItem.addActionListener(this);
		
		// there's a lot more work to do on printing the help window ...
		// the current code "does it," but doesn't know about pages and
		// doesn't resize to fit the page, although it does seem to know
		// how to render the html ???
		
//		menuItem = new JMenuItem("Print Help Window");
//    	menu.add(menuItem);
//		menuItem.addActionListener(this);
		
		menu.addSeparator();

		menuItem = new JMenuItem("Font and Size Selection");
    	menu.add(menuItem);
		menuItem.addActionListener(this);

		menu.addSeparator();

		menuItem = new JMenuItem("Table 3.1");
    	menu.add(menuItem);
		menuItem.addActionListener(this);
		
		menuItem = new JMenuItem("Example 3.6.1 (Table 3.7)");
    	menu.add(menuItem);
		menuItem.addActionListener(this);
		
		menu.addSeparator();

		menuItem = new JMenuItem("Release of December 13");
		menuItem.setEnabled(false);
    	menu.add(menuItem);

	   	menuBar.add(menu);

		return menuBar;

		}

	public void createNewProblem() {
	
			// create a new PRTTableModel and a new PRTFrame to hold the PRTTable
			// in which it will be displayed.  add necessary links here so the table knows its
			// model by class, and model knows its frame by class because that's where
			// its menus live and its commands will be handled
			
		PRTTableModel tableModel = new PRTTableModel(2, 3);
		PRTFrame frame = new PRTFrame(this,tableModel,++fWindowPositionsUsed); 
		tableModel.setFrame(frame);	
		frame.pack();
		frame.setVisible(true);
		frame.addVetoableChangeListener(this);
		frame.addInternalFrameListener(this);
//		tableModel.setFontAndSize("Sans Serif",12);
		//LPAssistant.speak("asked for focus " + tableModel.getTable().requestFocusInWindow());

		fDesktopPane.add(frame);		
        try {
            frame.setSelected(true);
			} 
		catch (java.beans.PropertyVetoException e) {
			}

		//LPAssistant.speak("asked for focus " + tableModel.getTable().requestFocusInWindow());
		tableModel.getTable().requestFocusInWindow();
		tableModel.setFontAndSize(fDefaultFontName,fDefaultFontSize);
		}

	public void createNewProblem(File fromFile) {
	
			// opens an existing PRTTableModel from data in a text file,
			// and a new PRTFrame to hold the PRTTable
			// in which it will be displayed.  
			
		PRTTableModel tableModel = new PRTTableModel(fromFile);
		PRTFrame frame = new PRTFrame(this,tableModel,++fWindowPositionsUsed); 
		frame.setTitle(fromFile.getName());
		tableModel.setFrame(frame);	
		tableModel.setLocked(true);  // bring up in pivot mode
		tableModel.updateInputAVsandPivots();
		tableModel.setAccuracy(tableModel.getAccuracy());
		tableModel.setUseX(tableModel.getUseX());
//		tableModel.setFontAndSize("Sans Serif",12);
		frame.pack();
		frame.setVisible(true);
		frame.addVetoableChangeListener(this);
		frame.addInternalFrameListener(this);
		
		fDesktopPane.add(frame);		
        try {
            frame.setSelected(true);
			} 
		catch (java.beans.PropertyVetoException e) {
			}
		tableModel.getTable().requestFocusInWindow();		
		tableModel.setFontAndSize(fDefaultFontName,fDefaultFontSize);
		}

	public void createNewProblem(PRTFrame fromFrame) {
	
		PRTTableModel fromModel = fromFrame.getPRTTableModel();
			
		PRTTableModel tableModel = new PRTTableModel(fromModel);
		PRTFrame frame = new PRTFrame(this,tableModel,++fWindowPositionsUsed); 
		tableModel.setFrame(frame);	
		tableModel.setLocked(true);  // bring up in pivot mode
		tableModel.setAccuracy(fromModel.getAccuracy());
		tableModel.setUseX(fromModel.getUseX());
		
			// before leaving, we remove all artificial variables ... you really should not be
			// moving forward on a problem until you've gotten rid of the artificial variables.
			// of course, if you choose to do so, that's your choice !
			
			// programming note: do this in reverse, since every removed variable causes renumbering
						
		for (int artVar=tableModel.getNumVariables(); artVar>tableModel.getNumRegularVariables();  artVar--) {
			tableModel.doRemoveVariable(artVar);
			}

		frame.pack();
		frame.setVisible(true);
		frame.addVetoableChangeListener(this);
		frame.addInternalFrameListener(this);
		
		fDesktopPane.add(frame);		
        try {
            frame.setSelected(true);
			} 
		catch (java.beans.PropertyVetoException e) {
			}
		tableModel.getTable().requestFocusInWindow();
		tableModel.setFontAndSize(fDefaultFontName,fDefaultFontSize);
		}

	public void createNewProblemFromText(int theExampleorTable) {
	
			// the following seems to have some hope ... we can at least find the file
			// if it's packaged with the java archive ... but we seem then not to be
			// able to read the file from the archive.  we'll have to look more
			// carefully at what can be found and "read" from things in the archive
			
//        java.net.URL helpURL = LPAssistant.class.getResource("Table3-1.txt");
//        if (helpURL != null) {
//            speak("found Table3-1.txt at URL (" + helpURL.getPath() + ")");
//			File fromFile = new File(helpURL.getPath());
//			speak("File object is " + fromFile.toString());
//			createNewProblem(fromFile);
//			return;
//            }
//		else {
//			speak("could not find Table3-1.txt");
//			}

			// a hack, for the moment ... not clear right away that we'll use it
			// currently the only possible arguments for theExampleorTable are 1 and 2

		PRTTableModel tableModel;
		if (theExampleorTable==1)
			tableModel = new PRTTableModel(2, 5);
		else
			tableModel = new PRTTableModel(2, 4);
		tableModel.setDataForExample(theExampleorTable);
		PRTFrame frame = new PRTFrame(this,tableModel,++fWindowPositionsUsed); 
		tableModel.setFrame(frame);	
		tableModel.setLocked(true);  // bring up in pivot mode
		tableModel.updateInputAVsandPivots();
		tableModel.setAccuracy(tableModel.getAccuracy());
		tableModel.setUseX(tableModel.getUseX());
//		tableModel.setFontAndSize("Sans Serif",12);
		frame.pack();
		frame.setVisible(true);
		frame.addVetoableChangeListener(this);
		frame.addInternalFrameListener(this);
		
		fDesktopPane.add(frame);		
        try {
            frame.setSelected(true);
			} 
		catch (java.beans.PropertyVetoException e) {
			}
		tableModel.getTable().requestFocusInWindow();
		tableModel.setFontAndSize(fDefaultFontName,fDefaultFontSize);
		}

	private boolean handleCloseCommand(JInternalFrame whichFrame) {
		boolean successful = true;
		try {
			whichFrame.setClosed(true);
			}
		catch (java.beans.PropertyVetoException ve) {
			successful = false;
			}
		return successful;
		}
	
	private void handleOpenCommand() {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new TextFilter());
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			createNewProblem(fc.getSelectedFile());
			//speak("Would open file (" + file.getName() + ")");
			} 
		else
			speak("Open command cancelled.");
		}
		
	private void handleQuitCommand() {
		JInternalFrame theFrame;
		boolean continueQuitting = true;
		while( ((theFrame=fDesktopPane.getSelectedFrame())!=null) && (continueQuitting) ) {
			//speak("attempt to close " + theFrame.getTitle());
			continueQuitting = handleCloseCommand(theFrame);
			//speak("status of close call was " + continueQuitting);
			}
		
		if (continueQuitting)
			System.exit(0);
		}

	private void handleSaveCommand(PRTFrame whichFrame) {
		whichFrame.getPRTTableModel().doSave();
		}

	private void handleSaveAsCommand(PRTFrame whichFrame) {
	
		PRTTableModel theModel = whichFrame.getPRTTableModel();
		if (theModel.hasFile()) {
			handleSaveCommand(whichFrame);
			return;
			}
		
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;
			
		//LPAssistant.speak("handleSaveAsCommand: showSaveDialog approved");
		File theFile = fc.getSelectedFile(); // be sure it's not a directory ... but it should not be
		//LPAssistant.speak("file name is " + theFile.getName());
//		if (!theFile.isFile())
//			return;
		//LPAssistant.speak("passed .isFile() ");
			
//		if (theFile.exists()) {
//			// see if it's OK to overwrite it ... but shouldn't JFileChooser do this?
//			}
			
		//speak("handleSaveAsCommand: calling doSave()");
		theModel.doSave(theFile);
		updateMenus();	// the "Save" item will now be enabled with a name
		}
		

	public void internalFrameClosing(InternalFrameEvent e) {

		speak("Window is closing.");
		JInternalFrame theIFrame = e.getInternalFrame();
		//speak("Name fo Frame is " + theIFrame.getTitle());
		}
		
	public void internalFrameClosed(InternalFrameEvent e) {
//		speak("Window closed.");
		updateMenus();
		
			// if it's the font window that's closing, it's about
			// to be disposed of, and we need to know about that ...
			
		JInternalFrame theIFrame = e.getInternalFrame();
		if (theIFrame.getTitle().equals("Font and Size Selection"))
			fFontsWindow = null;

		//speak("Name fo Frame is " + theIFrame.getTitle());
		}
		
	public void internalFrameDeactivated(InternalFrameEvent e) {
//		speak("Window has been deactivated");
//		JInternalFrame theIFrame = e.getInternalFrame();
//		speak("Name of Frame is " + theIFrame.getTitle());
		}
		
	public void internalFrameActivated(InternalFrameEvent e) {
//		speak("Window has been activated");
//		JInternalFrame theIFrame = e.getInternalFrame();
//		speak("Name of Frame is " + theIFrame.getTitle());
		updateMenus();
		}
		
	public void internalFrameDeiconified(InternalFrameEvent e) {
		//LPAssistant.speak("Window has been deiconified");
		}
		
	public void internalFrameIconified(InternalFrameEvent e) {
		//LPAssistant.speak("Window has been iconified");
		}
		
	public void internalFrameOpened(InternalFrameEvent e) {
		//LPAssistant.speak("Window has been opened");
		updateMenus();
		}

// THIS ONE NEEDS LOTS OF WORK, ALTHOUGH WE'VE GOTTEN A START FROM SOME INTERNET SCAVENGING ...

	private void printHelpWindow() {
		PrintUtilities.printComponent(fHelpEditorPane);
//		PrinterJob printJob = PrinterJob.getPrinterJob();
//		PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
//		PageFormat thePageFormat = printJob.pageDialog(attributes);
//		printJob.setPrintable(fHelpEditorPane,thePageFormat);
//		if ((thePageFormat!=null) && printJob.printDialog())
//			try {
//				printJob.print();
//				} 
//			catch(PrinterException pe) {
//				speak("Error printing: " + pe);
//				}
		}
	
	
	private void showFontsWindow() {
        if (fFontsWindow==null)
			createFontsWindow();
		Dimension theSize = fDesktopPane.getSize();
		fFontsWindow.setLocation(10,theSize.height-60); // lower, left corner of desktop pane
		fFontsWindow.show();
		}
		
	private void showHelpWindow() {
        if (fHelpWindow==null)
			createHelpWindow();
		fHelpWindow.show();
		}
		
	public static void speak(String str) {
		//System.out.println(str);
		}

		
	private void updateMenus() {
	
		JInternalFrame theFrame = fDesktopPane.getSelectedFrame();
		if (theFrame==null) { 
			fNewFromWindowMenuItem.setText("New from");
			fNewFromWindowMenuItem.setEnabled(false);
			fCloseMenuItem.setText("Close");
			fCloseMenuItem.setEnabled(false);
			fSaveMenuItem.setText("Save");
			fSaveMenuItem.setEnabled(false);
			fSaveAsMenuItem.setEnabled(false);
			fPrintMenuItem.setText("Print");
			fPrintMenuItem.setEnabled(false);
			return;
			}
			
		String theTitle = theFrame.getTitle();
		if (theTitle.equals("Font and Size Selection")) {
			fNewFromWindowMenuItem.setText("New from");
			fNewFromWindowMenuItem.setEnabled(false);
			fCloseMenuItem.setText("Close Font and Size Selection");
			fCloseMenuItem.setEnabled(true);
			fSaveMenuItem.setText("Save");
			fSaveMenuItem.setEnabled(false);
			fSaveAsMenuItem.setEnabled(false);
			fPrintMenuItem.setText("Print");
			fPrintMenuItem.setEnabled(false);
			return;
			}
			
		fNewFromWindowMenuItem.setText("New from " + theTitle);
		fNewFromWindowMenuItem.setEnabled(true);
		fCloseMenuItem.setText("Close " + theTitle);
		fCloseMenuItem.setEnabled(true);
		if (((PRTFrame)theFrame).getPRTTableModel().hasFile())
			fSaveMenuItem.setText("Save " + theTitle);
		else {
			fSaveMenuItem.setText("Save");
			fSaveMenuItem.setEnabled(false);
			}
		fSaveAsMenuItem.setEnabled(true);
		fPrintMenuItem.setEnabled(true);
		fPrintMenuItem.setText("Print " + theTitle);
		
		}

	public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
	
			// we should be seeing only "selected" and "closed" changes coming through from PRTFrames and the Font window.
			// we're looking only for a "closed" change event, changing from the Boolean object 
			// with value false to the Boolean object having value true.
			// if so, we'll do the dialog here and throw a PropertyVetoException if the user
			// cancels the dialog
			
//		speak("property change event received");
//		speak("source has class " + evt.getSource().getClass());
//		speak("property Name: " + evt.getPropertyName());
//		if (evt.getOldValue()!=null) {
//			speak("old value has class " + evt.getOldValue().getClass());
//			speak("old value: " + evt.getOldValue().toString());
//			}
//		if (evt.getNewValue()!=null)
//			speak("new value: " + evt.getNewValue().toString());

		if ( (evt.getOldValue()==null) 
				|| (evt.getNewValue()==null) 
				|| (!evt.getPropertyName().equals("closed"))	// is there a constant string we should use?
				|| (!evt.getOldValue().toString().equals("false")) 
				|| (!evt.getNewValue().toString().equals("true"))	)
			return;
				
		JInternalFrame theIFrame = (JInternalFrame) evt.getSource();
		if (theIFrame.getTitle().equals("Font and Size Selection"))
			return;
			
		PRTFrame theFrame = (PRTFrame) theIFrame;
		PRTTableModel theModel = theFrame.getPRTTableModel();
		if (!theModel.askToSave())
			return;
			
		int userChoice = JOptionPane.showConfirmDialog(
								theFrame, 
								"Do you want to Save " + theFrame.getTitle() + "\nbefore Closing its Window?",
								"Confirm Save",
								JOptionPane.YES_NO_CANCEL_OPTION);
		if ((userChoice == JOptionPane.CANCEL_OPTION) || (userChoice == JOptionPane.CLOSED_OPTION)) {
			throw new java.beans.PropertyVetoException("User Canceled Save dialog",null);
					//new java.beans.PropertyChangeEvent(theModel,"Save",null,null));
			}
		if (userChoice == JOptionPane.NO_OPTION)
			return;

		if (theModel.hasFile()) {
			handleSaveCommand(theFrame);
			return;
			}
			
			// ideally, just call handleSaveAs(); but a cancel of this dialog is different   
			// than canceling the dialog of a Save As command ... perhaps come back and
			// rewrite SaveAs to return a boolean to avoid this code duplication
			
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(this);
		if (returnVal==JFileChooser.CANCEL_OPTION)
			throw new java.beans.PropertyVetoException("User Canceled Save dialog",null);
		File theFile = fc.getSelectedFile(); 
		theModel.doSave(theFile);
		}

		

}

class TextFilter extends javax.swing.filechooser.FileFilter {
    
    public void TextFilter() {
		}
		
	public boolean accept(File f) {
        if (f.isDirectory())
            return true;

        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1) {
            String extension = s.substring(i+1).toLowerCase();
            return (extension.equals("txt"));
			} 
		else
			return false;
    }
    
    public String getDescription() {
        return "Text Files (.txt) for LP Assistant";
		}
	}
	
	 
class PrintUtilities implements Printable {
  private Component componentToBePrinted;

  public static void printComponent(Component c) {
    new PrintUtilities(c).print();
  }
  
  public PrintUtilities(Component componentToBePrinted) {
    this.componentToBePrinted = componentToBePrinted;
  }
  
  public void print() {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    printJob.setPrintable(this);
    if (printJob.printDialog())
      try {
        printJob.print();
      } catch(PrinterException pe) {
        System.out.println("Error printing: " + pe);
      }
  }

  public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
    if (pageIndex > 0) {
      return(NO_SUCH_PAGE);
    } else {
      Graphics2D g2d = (Graphics2D)g;
      g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
      disableDoubleBuffering(componentToBePrinted);
      componentToBePrinted.paint(g2d);
      enableDoubleBuffering(componentToBePrinted);
      return(PAGE_EXISTS);
    }
  }

  public static void disableDoubleBuffering(Component c) {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(false);
  }

  public static void enableDoubleBuffering(Component c) {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(true);
  }
}