package UI.muxAnaly;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.generate.util.CommonParametersUtil;

import UI.parameters.UICommonParameters;

public class MuxAnalyUI {

	protected Object result;
	protected Shell shell;
	//上半部表格
	private static Table tableUp;
	//下半部表格
	private Table tableDown;


	/**
	 * Launch the application.
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void openOne() throws Throwable {
		try {
			//1、先进行多伦计算，获取计算结果
			BatchCalculate batchAnaly = new BatchCalculate(CommonParametersUtil.defaultRoundTime);
			batchAnaly.roundAnaly();
			
			//2、将计算平均结果值翻入到UI的全局结果参数中
			UICommonParameters.resultMap = batchAnaly.getResult();

			//3、打开UI进行结果填充
			MuxAnalyUI muxAnalyUI = new MuxAnalyUI();
			muxAnalyUI.open();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the dialog.
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {

		shell = new Shell();
		shell.setSize(800, 500);
		shell.setText("SWT Application");
		shell.setLayout(new FillLayout(SWT.VERTICAL));

		Composite compositeUp = new Composite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		compositeUp.setLayout(new FillLayout(SWT.HORIZONTAL));

		tableUp = new Table(compositeUp, SWT.BORDER | SWT.FULL_SELECTION);

		Composite compositeDown = new Composite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		compositeDown.setLayout(new FillLayout(SWT.HORIZONTAL));
		tableDown = new Table(compositeDown, SWT.BORDER | SWT.FULL_SELECTION);
		
		
		tableDown.setHeaderVisible(true);
		tableDown.setLinesVisible(true);
		tableUp.setHeaderVisible(true);
		tableUp.setLinesVisible(true);

		// 向上表插入数据
		insertDataInTableUp();

		// 向下标插入数据
		insertDataInTableDown();
		
		

	}

	/**
	 * 
	 * @Title: insertDataInTableDown
	 * @Description: 构建下表格并向其中插入数据
	 * @return void
	 */
	private void insertDataInTableDown() {
		// 创建下表头的字符串数组
		String[] tableDownHeader = { "比较项", "算法名", "结果值", "结论" };
		for (int i = 0; i < tableDownHeader.length; i++) {
			TableColumn tableColumn = new TableColumn(tableDown, SWT.NONE);
			tableColumn.setText(tableDownHeader[i]);
			// 设置表头可移动，默认为false
			tableColumn.setMoveable(true);
		}

		//往下表格中添加内容
		MuxAnalyConclusion.setConclusion(tableDown,UICommonParameters.resultMap);
		
		
		// 重新布局表格
		for (int i = 0; i < tableDownHeader.length; i++) {
			tableDown.getColumn(i).pack();
		}
		
	}
	
	/**
	 * 
	 * @Title: insertDataInTableUp
	 * @Description: 构建上表格并向其中插入数据
	 * @return void
	 */
	public static void insertDataInTableUp() {

		// 创建上表内容表头的字符串数组
		String[] tableUpHeader = { "算法名", "处理器利用率", "处理器有效利用率", "完成率", "执行时间", "简介" };
		for (int i = 0; i < tableUpHeader.length; i++) {
			TableColumn tableColumn = new TableColumn(tableUp, SWT.NONE);
			tableColumn.setText(tableUpHeader[i]);
			// 设置表头可移动，默认为false
			tableColumn.setMoveable(true);
		}
		if (CommonParametersUtil.FIFO == 1) {
			insertDataUp("FIFO", UICommonParameters.resultMap.get("FIFO"));
		}
		if (CommonParametersUtil.EDF == 1) {
			insertDataUp("EDF", UICommonParameters.resultMap.get("EDF"));
		}

		if (CommonParametersUtil.STF == 1) {
			insertDataUp("STF", UICommonParameters.resultMap.get("STF"));
		}

		if (CommonParametersUtil.EFTF == 1) {
			insertDataUp("EFTF", UICommonParameters.resultMap.get("EFTF"));
		}

		if (CommonParametersUtil.Workflowbased == 1) {
			insertDataUp("Semple", UICommonParameters.resultMap.get("WorkflowBased"));
		}
		
		/**
		 * 添加新算法时这里也要添加
		 */

		// 重新布局表格
		for (int i = 0; i < tableUpHeader.length; i++) {
			tableUp.getColumn(i).pack();
		}
	}

	private static void insertDataUp(String algoName, String data) {
		TableItem item;
		item = new TableItem(tableUp, SWT.NONE);
		String ER = data.split(",")[0];
		String UER = data.split(",")[1];
		String CR = data.split(",")[2];
		String ET = data.split(",")[3];
		item.setText(new String[] { algoName, ER, UER, CR, ET });
	}
}
