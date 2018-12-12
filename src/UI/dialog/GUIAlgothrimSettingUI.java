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

public class GUIAlgothrimSettingUI extends Dialog {

	protected int result;
	protected Shell shell;
	
	 public int FIFOFlag = 0;
	    public int EDFFlag = 0;
	    public int STFFlag = 0;
	    public int EFTFFlag = 0;
	    public int WorkflowbasedFlag = 0;
	    

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public GUIAlgothrimSettingUI(Shell parent, int style) {
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
	private void createContents() {

		shell = new Shell(getParent(), getStyle());
        shell.setSize(410, 420);
        shell.setText("Parameter Setting");
        shell.setLayout(null);

        Label FIFOLabel = new Label(shell, SWT.NONE);
        FIFOLabel.setText("FIFO");
        FIFOLabel.setBounds(70, 30, 58, 17);
        



        Label EDFLabel = new Label(shell, SWT.NONE);
        EDFLabel.setText("EDF");
        EDFLabel.setBounds(70, 80, 88, 17);


        Label STFLabel = new Label(shell, SWT.NONE);
        STFLabel.setText("STF");
        STFLabel.setBounds(70, 130, 88, 17);


        Label EFTFLabel = new Label(shell, SWT.NONE);
        EFTFLabel.setText("EFTF");
        EFTFLabel.setBounds(70, 180, 88, 17);
        
        

        Label WorkflowbasedLabel = new Label(shell, SWT.NONE);
        WorkflowbasedLabel.setText("Workflowbased");
        WorkflowbasedLabel.setBounds(70, 230, 114, 17);

        
        /**
         * 每行的纵左边都是间隔50
         */
        final Button FIFOButton = new Button(shell, SWT.CHECK);
        FIFOButton.setBounds(220, 30, 90, 17);
        FIFOButton.setText("FIFO");
        final Button EDFButton = new Button(shell, SWT.CHECK);
        EDFButton.setBounds(220, 80, 97, 17);
        EDFButton.setText("EDF");
        final Button STFButton = new Button(shell, SWT.CHECK);
        STFButton.setBounds(220, 130, 97, 17);
        STFButton.setText("STF");
        final Button EFTFButton = new Button(shell, SWT.CHECK);
        EFTFButton.setBounds(220, 180, 97, 17);
        EFTFButton.setText("EFTF");
        final Button WorkflowbasedButton = new Button(shell, SWT.CHECK);
        WorkflowbasedButton.setBounds(220, 230, 126, 17);
        WorkflowbasedButton.setText("Workflowbased");
        
        //确定按钮
        Button btnNewButton = new Button(shell, SWT.NONE);
        btnNewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                if (FIFOButton.getSelection()){
                    FIFOFlag = 1;
                }else {
                	FIFOFlag =0;
				}
                	
                if (EDFButton.getSelection()){
                	EDFFlag = 1;
                }else {
                	EDFFlag = 0;
				}
                    
                if (STFButton.getSelection()){
                	STFFlag = 1;
                }else {
                	STFFlag = 0;
				}
                    
                if (EFTFButton.getSelection()){
                	EFTFFlag = 1;
                }else {
                	EFTFFlag = 0;
				}
                if (WorkflowbasedButton.getSelection()){
                	WorkflowbasedFlag = 1;
                }else {
                	WorkflowbasedFlag = 0;
				}           
                
                
                /**
                 * 添加新的算法这里要改写
                 */

                
                result = SWT.OK;

                shell.close();
            }
        }); 
        
        btnNewButton.setSelection(true);
        btnNewButton.setBounds(69, 354, 80, 27);
        btnNewButton.setText("OK");


        //取消按钮
        Button btnNewButton_1 = new Button(shell, SWT.NONE);
        btnNewButton_1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                result = SWT.CANCEL;
                shell.close();
            }
        });
        btnNewButton_1.setBounds(244, 354, 80, 27);
        btnNewButton_1.setText("Cancel");
       

	}

}
