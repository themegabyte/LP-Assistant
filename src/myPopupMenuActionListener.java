import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class myPopupMenuActionListener implements ActionListener {
	
	private final static int ADD_AV = 0;
	private final static int REMOVE_AV = 1;
	private final static int CHANGE_BV = 2;
	private final static int CLEAR_BV = 3;
	private final static int REMOVE_CONSTRAINT = 4;
	
	private int fParameter;
	private PRTTableModel fModel;
	private int fOperation;  // 0 = add AV, 1 = remove AV, 
							// 2 = change basis element, 3 = clear basic var in row
	
	public myPopupMenuActionListener() {
		super();
		}
	
	public void actionPerformed(ActionEvent e) {
		switch (fOperation) {
		
			case ADD_AV:
					fModel.doAddArtificialVariable(fParameter);
					break;
			
			case REMOVE_AV:
					fModel.doRemoveVariable(fParameter);
					break;
			
			case CHANGE_BV:
					String actionName = e.getActionCommand();
					int k = actionName.length();
					String str;
					if (k==2)
						str = actionName.substring(1); 
					else
						str = actionName.substring(1,k);
					int newBasisVariable = Integer.valueOf(str).intValue();
					fModel.setBasicVariableinRow(fParameter,newBasisVariable);
					break;

			case CLEAR_BV:
					fModel.clearBasicVariableinRow(fParameter);
					break;
			
			case REMOVE_CONSTRAINT:
					fModel.doRemoveConstraint(fParameter);
					break;
			
			}
		}
		
	public void setReferencesforAdd(PRTTableModel whichModel, int whichVar) {
		fParameter = whichVar;
		fModel = whichModel;
		fOperation = ADD_AV;
		}
				
	public void setReferencesforRemove(PRTTableModel whichModel, int whichVar) {
		fParameter = whichVar;
		fModel = whichModel;
		fOperation = REMOVE_AV;
		}
				
	public void setReferencesforChange(PRTTableModel whichModel, int whichVar) {
		fParameter = whichVar;
		fModel = whichModel;
		fOperation = CHANGE_BV;
		}
				
	public void setReferencesforClear(PRTTableModel whichModel, int whichRow) {
		fParameter = whichRow;
		fModel = whichModel;
		fOperation = CLEAR_BV;
		}
				
	public void setReferencesforRemoveConstraint(PRTTableModel whichModel, int whichRow) {
		fParameter = whichRow;
		fModel = whichModel;
		fOperation = REMOVE_CONSTRAINT;
		}
				
	}

