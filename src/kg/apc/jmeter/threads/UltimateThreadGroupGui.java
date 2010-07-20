// TODO: сделать чтобы при переходе между контролами обновлялся график
package kg.apc.jmeter.threads;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import kg.apc.jmeter.charting.AbstractGraphRow;
import kg.apc.jmeter.vizualizers.DateTimeRenderer;
import kg.apc.jmeter.charting.GraphPanelChart;
import kg.apc.jmeter.charting.GraphRowExactValues;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;
import org.apache.jorphan.collections.HashTree;

public class UltimateThreadGroupGui
      extends AbstractThreadGroupGui
{
   protected ConcurrentHashMap<String, AbstractGraphRow> model;
   private GraphPanelChart chart;
   private JTextField initialDelay;
   private JTextField incUserCount;
   private JTextField incUserPeriod;
   private JTextField flightTime;
   private JTextField decUserCount;
   private JTextField decUserPeriod;
   private JTextField totalThreads;
   private LoopControlPanel loopPanel;
   private PowerTableModel tableModel;
   private JTable paramTable;

   public UltimateThreadGroupGui()
   {
      super();
      init();
   }

   protected final void init()
   {
      JPanel containerPanel = new VerticalPanel();

      containerPanel.add(createParamsPanel(), BorderLayout.NORTH);

      chart = new GraphPanelChart();
      model = new ConcurrentHashMap<String, AbstractGraphRow>();
      chart.setRows(model);
      chart.setDrawFinalZeroingLines(true);
      chart.setxAxisLabelRenderer(new DateTimeRenderer("HH:mm:ss"));
      containerPanel.add(chart, BorderLayout.CENTER);
      add(containerPanel, BorderLayout.CENTER);
      
      // this magic LoopPanel provides functionality for thread loops
      // TODO: find a way without magic
      createControllerPanel();
   }

   private JPanel createParamsPanel()
   {
      JPanel panel = new JPanel(new BorderLayout(5, 5));
      panel.setBorder(BorderFactory.createTitledBorder("Threads Scheduling Parameters"));

      JScrollPane scroll = new JScrollPane(createGrid());
      scroll.setPreferredSize(scroll.getMinimumSize());
      panel.add(scroll, BorderLayout.CENTER);


      return panel;
   }

   private JTable createGrid()
   {
      initTableModel();
      paramTable = new JTable(tableModel);
      // paramTable.setRowSelectionAllowed(true);
      // paramTable.setColumnSelectionAllowed(true);
      paramTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      // paramTable.setCellSelectionEnabled(true);
      //paramTable.setFillsViewportHeight(true);
      //paramTable.setMinimumSize(new Dimension(100, 70));

      return paramTable;
   }

   protected void initTableModel()
   {
      tableModel = new PowerTableModel(
            // column names
            new String[]
            {
               "Start Threads Count", "Initial Delay, sec", "RampUp Time, sec", "Hold Load For, sec"
            },
            // column classes
            new Class[]
            {
               Integer.class, Integer.class, Integer.class, Integer.class
            });
   }

   public String getLabelResource()
   {
      return this.getClass().getSimpleName();
   }

   @Override
   public String getStaticLabel()
   {
      return "Ultimate Thread Group";
   }

   public TestElement createTestElement()
   {
      SteppingThreadGroup tg = new SteppingThreadGroup();
      modifyTestElement(tg);
      return tg;
   }

   public void modifyTestElement(TestElement tg)
   {
      super.configureTestElement(tg);
      /*
      tg.setProperty(SteppingThreadGroup.NUM_THREADS, totalThreads.getText());
      tg.setProperty(SteppingThreadGroup.THREAD_GROUP_DELAY, initialDelay.getText());
      tg.setProperty(SteppingThreadGroup.INC_USER_COUNT, incUserCount.getText());
      tg.setProperty(SteppingThreadGroup.INC_USER_PERIOD, incUserPeriod.getText());
      tg.setProperty(SteppingThreadGroup.DEC_USER_COUNT, decUserCount.getText());
      tg.setProperty(SteppingThreadGroup.DEC_USER_PERIOD, decUserPeriod.getText());
      tg.setProperty(SteppingThreadGroup.FLIGHT_TIME, flightTime.getText());
      if (tg instanceof SteppingThreadGroup)
      {
      updateChart((SteppingThreadGroup) tg);
      ((AbstractThreadGroup) tg).setSamplerController((LoopController) loopPanel.createTestElement());
      }
       * 
       */
   }

   @Override
   public void configure(TestElement tg)
   {
      super.configure(tg);
      /*
      totalThreads.setText(Integer.toString(tg.getPropertyAsInt(SteppingThreadGroup.NUM_THREADS)));
      initialDelay.setText(Integer.toString(tg.getPropertyAsInt(SteppingThreadGroup.THREAD_GROUP_DELAY)));
      incUserCount.setText(Integer.toString(tg.getPropertyAsInt(SteppingThreadGroup.INC_USER_COUNT)));
      incUserPeriod.setText(Integer.toString(tg.getPropertyAsInt(SteppingThreadGroup.INC_USER_PERIOD)));
      decUserCount.setText(Integer.toString(tg.getPropertyAsInt(SteppingThreadGroup.DEC_USER_COUNT)));
      decUserPeriod.setText(Integer.toString(tg.getPropertyAsInt(SteppingThreadGroup.DEC_USER_PERIOD)));
      flightTime.setText(Integer.toString(tg.getPropertyAsInt(SteppingThreadGroup.FLIGHT_TIME)));
       *
       */

      TestElement te = (TestElement) tg.getProperty(AbstractThreadGroup.MAIN_CONTROLLER).getObjectValue();
      if (te != null)
      {
         loopPanel.configure(te);
      }
   }

   private void updateChart(SteppingThreadGroup tg)
   {
      model.clear();
      GraphRowExactValues row = new GraphRowExactValues();
      row.setColor(Color.RED);
      row.setDrawLine(true);
      row.setMarkerSize(AbstractGraphRow.MARKER_SIZE_SMALL);

      final HashTree hashTree = new HashTree();
      hashTree.add(new LoopController());
      JMeterThread thread = new JMeterThread(hashTree, null, null);

      // test start
      row.add(System.currentTimeMillis(), 0);
      row.add(System.currentTimeMillis() + tg.getThreadGroupDelay(), 0);

      // users in
      for (int n = 0; n < tg.getNumThreads(); n++)
      {
         thread.setThreadNum(n);
         tg.scheduleThread(thread);
         row.add(thread.getStartTime(), n + 1);
      }

      // users out
      for (int n = 0; n < tg.getNumThreads(); n++)
      {
         thread.setThreadNum(n);
         tg.scheduleThread(thread);
         row.add(thread.getEndTime(), tg.getNumThreads() - n);
      }

      // final point
      row.add(thread.getEndTime() + tg.getOutUserPeriod() * 1000, 0);

      model.put("Expected parallel users count", row);
      chart.repaint();
   }

   private JPanel createControllerPanel()
   {
      loopPanel = new LoopControlPanel(false);
      LoopController looper = (LoopController) loopPanel.createTestElement();
      looper.setLoops(-1);
      looper.setContinueForever(true);
      loopPanel.configure(looper);
      return loopPanel;
   }
}
