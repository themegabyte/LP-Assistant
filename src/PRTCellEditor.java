import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import java.util.EventObject;

public class PRTCellEditor extends DefaultCellEditor {

	public PRTCellEditor(JTextField textField) {
		super(textField);
		}
	
	public boolean shouldSelectCell(EventObject anEvent) {
		LPAssistant.speak("cell editor received event");
		//LPAssistant.speak(anEvent.toString());
		return super.shouldSelectCell(anEvent);
		}
		
	}