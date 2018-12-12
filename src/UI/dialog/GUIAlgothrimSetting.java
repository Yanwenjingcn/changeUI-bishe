package UI.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;


/**
 * 
* @ClassName: GUIAlgothrimSetting
* @Description: 选择参与比较的算法
* @author Wengie Yan
* @date 2018年12月12日
 */
public class GUIAlgothrimSetting extends Dialog {

    protected int result;
    protected Shell shlAgfafd;

    public int FIFOFlag = 0;
    public int EDFFlag = 0;
    public int STFFlag = 0;
    public int EFTFFlag = 0;
    public int WorkflowbasedFlag = 0;
    
    public GUIAlgothrimSetting(Shell parent, int style) {
        super(parent, style);
        setText("SWT Dialog");
    }

    /**
     * @throws
     * @Title: open
     * @Description: Open the dialog.
     * @return:
     */
    public int open() {
        createContents();
        shlAgfafd.open();
        shlAgfafd.layout();
        Display display = getParent().getDisplay();
        while (!shlAgfafd.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    /**
     * @Title: createContents
     * @Description:
     */
    private void createContents() {
    	
		 shlAgfafd = new Shell(getParent(), getStyle());
	        shlAgfafd.setSize(440, 420);
	        shlAgfafd.setText("Parameter Setting");
	        shlAgfafd.setLayout(null);

	        Label FIFOLabel = new Label(shlAgfafd, SWT.NONE);
	        FIFOLabel.setText("FIFO");
	        FIFOLabel.setBounds(69, 32, 58, 17);
	        Composite FIFOcomposite = new Composite(shlAgfafd, SWT.NONE);
	        FIFOcomposite.setBounds(214, 23, 167, 39);
	        final Button FIFOButton = new Button(FIFOcomposite, SWT.RADIO);
	        FIFOButton.setBounds(0, 10, 90, 17);
	        FIFOButton.setText("FIFO");


	        Label EDFLabel = new Label(shlAgfafd, SWT.NONE);
	        EDFLabel.setText("EDF");
	        EDFLabel.setBounds(69, 99, 88, 17);
	        Composite EDFcomposite = new Composite(shlAgfafd, SWT.NONE);
	        EDFcomposite.setBounds(214, 86, 100, 39);
	        final Button EDFButton = new Button(EDFcomposite, SWT.RADIO);
	        EDFButton.setBounds(0, 10, 97, 17);
	        EDFButton.setText("EDF");


	        Label STFLabel = new Label(shlAgfafd, SWT.NONE);
	        STFLabel.setText("STF");
	        STFLabel.setBounds(69, 177, 88, 17);
	        Composite STFcomposite = new Composite(shlAgfafd, SWT.NONE);
	        STFcomposite.setBounds(210, 173, 214, 38);
	        final Button STFButton = new Button(STFcomposite, SWT.RADIO);
	        STFButton.setBounds(0, 10, 97, 17);
	        STFButton.setText("STF");


	        Label EFTFLabel = new Label(shlAgfafd, SWT.NONE);
	        EFTFLabel.setText("EFTF");
	        EFTFLabel.setBounds(69, 252, 88, 17);
	        Composite EFTFcomposite = new Composite(shlAgfafd, SWT.NONE);
	        EFTFcomposite.setBounds(210, 231, 214, 47);
	        final Button EFTFButton = new Button(EFTFcomposite, SWT.RADIO);
	        EFTFButton.setBounds(0, 21, 97, 17);
	        EFTFButton.setText("EFTF");

	        Label WorkflowbasedLabel = new Label(shlAgfafd, SWT.NONE);
	        WorkflowbasedLabel.setText("Workflowbased");
	        WorkflowbasedLabel.setBounds(69, 310, 114, 17);
	        Composite Workflowbasedcomposite = new Composite(shlAgfafd, SWT.NONE);
	        Workflowbasedcomposite.setBounds(210, 302, 214, 39);
	        final Button WorkflowbasedButton = new Button(Workflowbasedcomposite, SWT.RADIO);
	        WorkflowbasedButton.setBounds(0, 10, 126, 17);
	        WorkflowbasedButton.setText("Workflowbased");
	        

	        //确定按钮
	        Button btnNewButton = new Button(shlAgfafd, SWT.NONE);
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

	                shlAgfafd.close();
	            }
	        }); 
	        
	        btnNewButton.setSelection(true);
	        btnNewButton.setBounds(69, 354, 80, 27);
	        btnNewButton.setText("OK");


	        //取消按钮
	        Button btnNewButton_1 = new Button(shlAgfafd, SWT.NONE);
	        btnNewButton_1.addSelectionListener(new SelectionAdapter() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	                result = SWT.CANCEL;
	                shlAgfafd.close();
	            }
	        });
	        btnNewButton_1.setBounds(244, 354, 80, 27);
	        btnNewButton_1.setText("Cancel");

    }
}
