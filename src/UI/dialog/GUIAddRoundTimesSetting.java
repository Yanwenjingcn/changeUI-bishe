package UI.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.generate.util.CommonParametersUtil;

public class GUIAddRoundTimesSetting extends Dialog {

	protected int result;
	protected Shell shell;
	private Text roundTimesText;

	public int defaultRoundTime;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public GUIAddRoundTimesSetting(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public int open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private  void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(440, 196);
		shell.setText("Parameter Setting");
		shell.setLayout(null);
		
		

		Label roundTimesLabel = new Label(shell, SWT.NONE);
		roundTimesLabel.setBounds(24, 40, 96, 17);
		roundTimesLabel.setText("RoundTimes");

		roundTimesText = new Text(shell, SWT.BORDER);
		roundTimesText.setText("2");
		roundTimesText.setBounds(150, 37, 73, 23);

		Label info = new Label(shell, SWT.NONE);
		info.setText("(default roundtimes is 2)");
		info.setBounds(268, 40, 156, 17);

		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				CommonParametersUtil.defaultRoundTime = Integer.valueOf(roundTimesText.getText());
				
				result = SWT.OK;
				
				shell.close();
			}
		});
		btnNewButton.setSelection(true);
		btnNewButton.setBounds(80, 110, 80, 27);
		btnNewButton.setText("OK");

		Button btnNewButton_1 = new Button(shell, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result = SWT.CANCEL;
				shell.close();
			}
		});
		btnNewButton_1.setBounds(255, 110, 80, 27);
		btnNewButton_1.setText("Cancel");
	}

}
