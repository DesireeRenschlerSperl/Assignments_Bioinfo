
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;



public class TimePopSampleSizePlotter extends ApplicationFrame {

    /**
     *
     * @param title  the frame title.
     */
    public TimePopSampleSizePlotter(final String title, int[][][] popSizeTimeMCRA) {

        super(title);
        final XYSeriesCollection data = new XYSeriesCollection();



        for (int j = 0; j < popSizeTimeMCRA.length; j++) {

            final XYSeries series = new XYSeries("Time finding MCRA for given Pop. Size with sample size k=" + (j+PopGenSimulator.MIN_SAMPLESIZE));

            for (int i = 0; i < popSizeTimeMCRA[j].length; i++) {
                series.add(popSizeTimeMCRA[j][i][0], -popSizeTimeMCRA[j][i][1]);
            }

            data.addSeries(series);
        }

        final JFreeChart chart = ChartFactory.createXYLineChart(
                "Time finding MCRA for given Pop. Size",
                "pop. size",
                "time [in generations]",
                data,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);

    }
}