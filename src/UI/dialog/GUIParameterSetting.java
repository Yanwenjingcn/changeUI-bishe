package UI.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;

/**
 * 
* @ClassName: GUIParameterSetting
* @Description: 参数设置框
* @author Wengie Yan
* @date 2018年12月12日
 */
public class GUIParameterSetting extends Dialog {

	protected int result;
	protected Shell parameterSettingShell;
	private Text text;
	private Text text_1;
	private Text text_2;
	private Text text_3;
	private Text text_4;

	public int timeWindow;
	public int taskAverageLength;
	public int dagAverageSize;
	public int dagLevelFlag;
	public double deadLineTimes;
	public int processorNumber;
	
	//多次分析时 默认计算轮次
	public int defaultRoundTime=2;

	/**
	 * 
	 * @Title: GUIParameterSetting
	 * @Description: Create the dialog.
	 * @param: @param parent
	 * @param: @param style
	 * @throws
	 */
	public GUIParameterSetting(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * 
	 * @Title: open
	 * @Description: Open the dialog.
	 * @return:
	 * @throws
	 */
	public int open() {
		createContents();
		parameterSettingShell.open();
		parameterSettingShell.layout();
		Display display = getParent().getDisplay();
		while (!parameterSettingShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * 
	 * @Title: createContents
	 * @Description:
	 */
	private void createContents() {
		parameterSettingShell = new Shell(getParent(), getStyle());
		parameterSettingShell.setSize(440, 420);
		parameterSettingShell.setText("Parameter Setting");
		parameterSettingShell.setLayout(null);

		Label lblNewLabel = new Label(parameterSettingShell, SWT.NONE);
		lblNewLabel.setBounds(24, 30, 96, 17);
		lblNewLabel.setText("TimeWindow");

		Label lblMaxdeviationrate = new Label(parameterSettingShell, SWT.NONE);
		lblMaxdeviationrate.setText("TaskAverageLength");
		lblMaxdeviationrate.setBounds(24, 71, 113, 17);

		Label lblPricingInterval = new Label(parameterSettingShell, SWT.NONE);
		lblPricingInterval.setText("DAGAverageSize");
		lblPricingInterval.setBounds(24, 126, 113, 17);

		text_4 = new Text(parameterSettingShell, SWT.BORDER);
		text_4.setText("40");
		text_4.setBounds(190, 126, 73, 23);

		text = new Text(parameterSettingShell, SWT.BORDER);
		text.setText("40");
		text.setBounds(190, 71, 73, 23);

		text_1 = new Text(parameterSettingShell, SWT.BORDER);
		text_1.setText("40000");
		text_1.setBounds(190, 27, 73, 23);

		Label lbldaglength = new Label(parameterSettingShell, SWT.NONE);
		lbldaglength.setText("(optional:20/40/60)");
		lbldaglength.setBounds(270, 71, 156, 17);

		Label lbldagsize = new Label(parameterSettingShell, SWT.NONE);
		lbldagsize.setText("(optional:20/40/60)");
		lbldagsize.setBounds(270, 126, 156, 17);

		Label lblRuntimeDistributionType = new Label(parameterSettingShell,
				SWT.NONE);
		lblRuntimeDistributionType.setText("DAGLevelFlag");
		lblRuntimeDistributionType.setBounds(24, 175, 156, 17);

		Composite composite = new Composite(parameterSettingShell, SWT.NONE);
		composite.setBounds(190, 157, 214, 64);

		final Button btnRadioButton_2 = new Button(composite, SWT.RADIO);
		btnRadioButton_2.setBounds(120, 21, 97, 17);
		btnRadioButton_2.setText("3");

		final Button btnRadioButton_1 = new Button(composite, SWT.RADIO);
		btnRadioButton_1.setBounds(60, 21, 97, 17);
		btnRadioButton_1.setSelection(true);
		btnRadioButton_1.setText("2");

		final Button btnRadioButton = new Button(composite, SWT.RADIO);
		btnRadioButton.setBounds(0, 21, 97, 17);
		btnRadioButton.setText("1");

		Label lblSystemBandwidth = new Label(parameterSettingShell, SWT.NONE);
		lblSystemBandwidth.setText("DeadlineTimes");
		lblSystemBandwidth.setBounds(24, 234, 109, 17);

		text_2 = new Text(parameterSettingShell, SWT.BORDER);
		text_2.setText("1.1");
		text_2.setBounds(190, 231, 73, 23);

		Label lbldead = new Label(parameterSettingShell, SWT.NONE);
		lbldead.setText("(optional:1.05/1.1/1.2)");
		lbldead.setBounds(270, 231, 156, 17);

		Button btnNewButton = new Button(parameterSettingShell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				timeWindow = Integer.valueOf(text_1.getText());
				taskAverageLength = Integer.valueOf(text.getText());
				dagAverageSize = Integer.valueOf(text_4.getText());
				deadLineTimes = Double.valueOf(text_2.getText());
				processorNumber = Integer.valueOf(text_3.getText());

				if (btnRadioButton_1.getSelection())
					dagLevelFlag = 2;
				else if (btnRadioButton.getSelection())
					dagLevelFlag = 1;
				else if (btnRadioButton_2.getSelection())
					dagLevelFlag = 3;

				result = SWT.OK;

				parameterSettingShell.close();
			}
		});
		btnNewButton.setSelection(true);
		btnNewButton.setBounds(80, 330, 80, 27);
		btnNewButton.setText("OK");

		Button btnNewButton_1 = new Button(parameterSettingShell, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result = SWT.CANCEL;
				parameterSettingShell.close();
			}
		});
		btnNewButton_1.setBounds(220, 330, 80, 27);
		btnNewButton_1.setText("Cancel");

		Label lblVmSetupTime = new Label(parameterSettingShell, SWT.NONE);
		lblVmSetupTime.setText("ProcessorNumber");
		lblVmSetupTime.setBounds(24, 273, 113, 17);

		text_3 = new Text(parameterSettingShell, SWT.BORDER);
		text_3.setText("8");
		text_3.setBounds(190, 270, 73, 23);

		Label lblSeconds = new Label(parameterSettingShell, SWT.NONE);
		lblSeconds.setText("(optional:4/8/16)");
		lblSeconds.setBounds(270, 273, 156, 17);
	}
}
