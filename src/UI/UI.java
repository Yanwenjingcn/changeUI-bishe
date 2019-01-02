package UI;

import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.ToolItem;
import org.generate.DagFlowGenerater;
import org.generate.util.CommonParametersUtil;

import org.schedule.algorithm.Makespan;

import org.temp.Semple;

import UI.dialog.GUIAddRoundTimesSettingUI;
import UI.dialog.GUIAlgothrimSettingUI;
import UI.dialog.GUIParameterSettingUI;
import UI.muxAnaly.MuxAnalyUI;
import UI.parameters.UICommonParameters;
import UI.singleAnaly.SingleAnalyUI;
import swing2swt.layout.BorderLayout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;



/**
 * 
 * 需要改进的地方：
 * 
 * 1、按钮的可见与不可见应该与tabfloder相反 2、每次点击还原后tabfloder应该是重新生成的
 * 3、tabfloder的可适应性变化 4、多次分析的内容完善（建议还是创建新的类吧）
 * 
 * 
 */

/**
 * 
 * @ClassName: UI
 * @Description: TODO
 * @author Wengie Yan
 * @date 2018年12月12日
 */
class UI {

	protected Shell shell;
	protected Display display;

	protected TabFolder tabFolder;

	ToolItem danci, duoci;
	protected Composite composite;
	ScrolledComposite scrolledComposite;

	static boolean flag = false;

	private static int[][] color = new int[500][3];
	private int leftmargin = 110;
	private int maxheight = 1000;
	private int width;
	private int height;
	private int timewind;
	int dagnummax = 10000;
	int mesnum = 5;

	DagFlowGenerater dagbuilder = new DagFlowGenerater();

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UI window = new UI();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
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
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		UICommonParameters.shell = shell;
		shell.setSize(800, 500);
		shell.setText("RTWSim");
		shell.setLayout(new BorderLayout(0, 0));

		final Button button = new Button(shell, SWT.NONE);
		final Button button_1 = new Button(shell, SWT.NONE);
		button.setBounds(260, 114, 200, 47);
		button_1.setBounds(260, 290, 200, 47);

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		//--------------------选择算法按钮------------------------
		MenuItem Chooseslgorithm = new MenuItem(menu, SWT.NONE);
		Chooseslgorithm.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				ChooseAlgorithm();
			}
		});
		Chooseslgorithm.setText("ChooseAlgorithm");

		//----------------------构建参数按钮---------------------
		MenuItem buildPeremetersMenu = new MenuItem(menu, SWT.CASCADE);
		buildPeremetersMenu.setText("BuilderParameters");
		
		Menu menu_2 = new Menu(buildPeremetersMenu);
		buildPeremetersMenu.setMenu(menu_2);
		
		MenuItem mntmBuildcommomparameters = new MenuItem(menu_2, SWT.NONE);
		mntmBuildcommomparameters.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				BuildParameters();
			}
		});
		mntmBuildcommomparameters.setText("BuildCommomParameters");
		
		MenuItem mntmBuildroundtimes = new MenuItem(menu_2, SWT.NONE);
		mntmBuildroundtimes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				BuildRoundTimesParameters();
			}
		});
		mntmBuildroundtimes.setText("BuildRoundTimes");

		// -----------------还原按钮------------------
		MenuItem mntmHuanyuan = new MenuItem(menu, SWT.NONE);
		mntmHuanyuan.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {

				// 点击后就设置这个按钮可见
				button.setVisible(true);
				button_1.setVisible(true);
				flag = true;
				Checkbox();

				// 关闭上一次的选项卡
				tabFolder.dispose();
				// 开启一次新的选项卡进行操作
				tabFolder = new TabFolder(shell, SWT.NONE);
				tabFolder.setBounds(0, 0, 780, 440);
				tabFolder.setLayoutData(BorderLayout.CENTER);

				clearAlgoFlag();

			}

			private void clearAlgoFlag() {
				CommonParametersUtil.FIFO = 0;
				CommonParametersUtil.EDF = 0;
				CommonParametersUtil.STF = 0;
				CommonParametersUtil.EFTF = 0;
				CommonParametersUtil.Workflowbased = 0;
				
				//还清除已经生成的文件（现在可以不用了，因为平均值无所谓）
				
			}

			private void Checkbox() {
				// 按钮的可见与不可见应该与tab相反
				if (flag == true) {
					tabFolder.setVisible(false);
				} else {
					tabFolder.setVisible(true);
				}
			}
		});
		mntmHuanyuan.setText("Restore");
		
		//----------------------工具按钮-----------------------
		MenuItem mntmTools = new MenuItem(menu, SWT.CASCADE);
		mntmTools.setText("Tools");

		Menu menu_1 = new Menu(mntmTools);
		mntmTools.setMenu(menu_1);

		MenuItem mntmHelp = new MenuItem(menu_1, SWT.NONE);
		mntmHelp.setText("Help");

		MenuItem mntmAbouttools = new MenuItem(menu_1, SWT.NONE);
		mntmAbouttools.setText("AboutTools");

		// ------------------单次分析--------------------------

		

		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				// 点击后就设置这个按钮不可见
				button.setVisible(false);
				button_1.setVisible(false);
				tabFolder.setVisible(true);

				// 生成DAG
				dagbuilder.DAGFlowGenerate();

				// 计算常规的算法
				Makespan ms = new Makespan();
				try {
					ms.runMakespan_xml();
				} catch (Throwable throwable) {
					throwable.printStackTrace();
				}

				// 计算附加的新算法
				exeOtherAlgorithms();

				// 设置要展示的窗口大小以及高度
				width = (int) (CommonParametersUtil.timeWindow / CommonParametersUtil.processorNumber) + leftmargin
						+ 20;
				height = CommonParametersUtil.processorNumber * 50 + 100;
				timewind = (int) (CommonParametersUtil.timeWindow / CommonParametersUtil.processorNumber);

				UICommonParameters.width = width;
				UICommonParameters.height = height;
				UICommonParameters.timewind = timewind;

				// 随机产生新的颜色
				randomColor();
				UICommonParameters.color = color;

				// 展示生成DAG页面（concole）
				SingleAnalyUI.getConsoleTabItem(tabFolder, display, "console");

				// 展示所选择算法的单个 结果页面
				displayChooseAlgo();

			}
		});
		button.setText("单次分析");

		// -----------------多次分析--------------

		button_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					MuxAnalyUI.openOne();
					clearAlgoFlag();

				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			private void clearAlgoFlag() {
				CommonParametersUtil.FIFO = 0;
				CommonParametersUtil.EDF = 0;
				CommonParametersUtil.STF = 0;
				CommonParametersUtil.EFTF = 0;
				CommonParametersUtil.Workflowbased = 0;
				CommonParametersUtil.defaultRoundTime=2;
			}
		});
		button_1.setText("多次分析");

		tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.setBounds(0, 0, 780, 440);
		tabFolder.setLayoutData(BorderLayout.CENTER);

	}

	private void displayChooseAlgo() {

		if (CommonParametersUtil.FIFO == 1) {
			DisplayNewResult("FIFO");
		}
		if (CommonParametersUtil.EDF == 1) {
			DisplayNewResult("EDF");
		}

		if (CommonParametersUtil.STF == 1) {
			DisplayNewResult("STF");
		}

		if (CommonParametersUtil.EFTF == 1) {
			DisplayNewResult("EFTF");
		}

		if (CommonParametersUtil.Workflowbased == 1) {
			DisplayNewResult("Workflowbased");
		}
	}

	/**
	 * @throws @Title:
	 *             randomColor
	 * @Description: 随机生成任务块的颜色
	 */
	public void randomColor() {
		Random random = new Random();
		int max = 230;
		int min = 30;
		for (int i = 0; i < DagFlowGenerater.finishDagList.size(); i++) {
			color[i][0] = random.nextInt(max) % (max - min + 1) + min;
			color[i][1] = random.nextInt(max) % (max - min + 1) + min;
			color[i][2] = random.nextInt(max) % (max - min + 1) + min;
		}

	}

	public int[] getcolor(int dagcount) {
		return color[dagcount];
	}

	public void exeOtherAlgorithms() {

		Semple fi = new Semple();
		//IncreaseSubmitTime fi=new IncreaseSubmitTime();

		String schedulePath = System.getProperty("user.dir") + "\\DAG_XML\\";

		try {
			fi.runMakespan(schedulePath, "E:\\DagCasesResult\\");

			MessageBox box = new MessageBox(shell);
			box.setMessage("Complete!");
			box.open();

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Title: ChooseAlgorithm
	 * @Description: 选择要参与比较的算法
	 * @return void
	 */
	public void ChooseAlgorithm() {

		// 构建一个新的页面（填写参数的那种）
		GUIAlgothrimSettingUI algorithmDialog = new GUIAlgothrimSettingUI(new Shell(), SWT.TITLE);

		if (algorithmDialog.open() != SWT.OK)
			return;

		// 接收里面获得的数据结果,是否选择了这个算法
		CommonParametersUtil.FIFO = algorithmDialog.FIFOFlag;
		CommonParametersUtil.EDF = algorithmDialog.EDFFlag;
		CommonParametersUtil.STF = algorithmDialog.STFFlag;
		CommonParametersUtil.EFTF = algorithmDialog.EFTFFlag;
		CommonParametersUtil.Workflowbased = algorithmDialog.WorkflowbasedFlag;
		
		//System.out.println(CommonParametersUtil.FIFO+" "+CommonParametersUtil.EDF+" "+CommonParametersUtil.STF+" "+CommonParametersUtil.EFTF+" "+CommonParametersUtil.Workflowbased+" ");

	}

	
	/**
	 * 
	 * @Title: BuildParameters
	 * @Description:设置参数
	 * @return void
	 */
	public void BuildParameters() {

		GUIParameterSettingUI pasetdialog = new GUIParameterSettingUI(new Shell(), SWT.TITLE);

		if (pasetdialog.open() != SWT.OK)
			return;
		CommonParametersUtil.timeWindow = pasetdialog.timeWindow;
		CommonParametersUtil.taskAverageLength = pasetdialog.taskAverageLength;
		CommonParametersUtil.dagAverageSize = pasetdialog.dagAverageSize;
		CommonParametersUtil.dagLevelFlag = pasetdialog.dagLevelFlag;
		CommonParametersUtil.deadLineTimes = pasetdialog.deadLineTimes;
		CommonParametersUtil.processorNumber = pasetdialog.processorNumber;
		
	//	System.out.println("CommonParametersUtil.timeWindow="+CommonParametersUtil.timeWindow+"*****************CommonParametersUtil.taskAverageLength="+CommonParametersUtil.taskAverageLength);
	}

	
	
	public void BuildRoundTimesParameters() {

		GUIAddRoundTimesSettingUI roundTimesdialog = new GUIAddRoundTimesSettingUI(new Shell(), SWT.TITLE);
		if (roundTimesdialog.open() != SWT.OK)
			return;
		System.out.println("CommonParametersUtil.defaultRoundTime="+CommonParametersUtil.defaultRoundTime);

	}

	public void DisplayNewResult(final String itemname) {
		try {
			Display display = Display.getDefault();
			display.syncExec(new Runnable() {
				public void run() {

					// 构建TabItem
					if (itemname.equals("FIFO")) {
						SingleAnalyUI.getAlgorithmTabItem(tabFolder, itemname);
					}
					if (itemname.equals("EDF")) {
						SingleAnalyUI.getAlgorithmTabItem(tabFolder, itemname);
					}
					if (itemname.equals("STF")) {
						SingleAnalyUI.getAlgorithmTabItem(tabFolder, itemname);
					}
					if (itemname.equals("EFTF")) {
						SingleAnalyUI.getAlgorithmTabItem(tabFolder, itemname);
					}
					if (itemname.equals("Workflowbased")) {
						SingleAnalyUI.getAlgorithmTabItem(tabFolder, itemname);
					}

				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
