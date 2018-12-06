package UI.muxAnaly;

import java.util.HashMap;
import java.util.Map;

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
import UI.utils.MuxAnalyConclusion;

public class MuxAnalyUI {

	protected Object result;
	protected Shell shell;
	private static Table tableUp;
	private Table tableDown;


	/**
	 * Launch the application.
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void openOne() throws Throwable {
		try {
			BatchCalculate batchAnaly = new BatchCalculate(CommonParametersUtil.defaultRoundTime);
			batchAnaly.roundAnaly();
			UICommonParameters.resultMap = batchAnaly.getResult();

			MuxAnalyUI window = new MuxAnalyUI();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the dialog.
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

		Composite composite = new Composite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));

		Table checkboxTableViewer = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
		tableUp = checkboxTableViewer;

		Composite composite_1 = new Composite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));

		tableDown = new Table(composite_1, SWT.BORDER | SWT.FULL_SELECTION);
		tableDown.setHeaderVisible(true);
		tableDown.setLinesVisible(true);
		tableUp.setHeaderVisible(true);
		tableUp.setLinesVisible(true);

		// 向上表插入数据
		insertDataInTableUp();

		// 向下标插入数据
		insertDataInTableDown();
		
		

	}

	private void insertDataInTableDown() {
		// 创建下表头的字符串数组
		String[] tableDownHeader = { "比较项", "算法名", "结果值", "结论" };
		for (int i = 0; i < tableDownHeader.length; i++) {
			TableColumn tableColumn = new TableColumn(tableDown, SWT.NONE);
			tableColumn.setText(tableDownHeader[i]);
			// 设置表头可移动，默认为false
			tableColumn.setMoveable(true);
		}

		//设置内容
		MuxAnalyConclusion.setConclusion(tableDown,UICommonParameters.resultMap);
		// 重新布局表格
		for (int i = 0; i < tableDownHeader.length; i++) {
			tableDown.getColumn(i).pack();
		}
	}

	/**
	 * 往上表格插入数据
	 * 
	 * @param tableHeader
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
			insertDataUp("FIFO", UICommonParameters.resultMap.get("fifo.txt"));
		}
		if (CommonParametersUtil.EDF == 1) {
			insertDataUp("EDF", UICommonParameters.resultMap.get("edf.txt"));
		}

		if (CommonParametersUtil.STF == 1) {
			insertDataUp("STF", UICommonParameters.resultMap.get("stf.txt"));
		}

		if (CommonParametersUtil.EFTF == 1) {
			insertDataUp("EFTF", UICommonParameters.resultMap.get("eftf.txt"));
		}

		if (CommonParametersUtil.Workflowbased == 1) {
			insertDataUp("Semple", UICommonParameters.resultMap.get("semple.txt"));
		}

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
