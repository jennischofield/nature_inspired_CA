package src;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;

import java.io.File;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.awt.Color; 
import java.awt.BasicStroke; 
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public class XYLinePlot extends ApplicationFrame{
    JFreeChart xylineChart;
    public XYLinePlot(String applicationTitle , String plotTitle, int[][] x, int[][]y, String[] datasetName ) {
        super(applicationTitle);
        XYDataset dataset = createDataset(x, y, datasetName);
        xylineChart = ChartFactory.createXYLineChart(
           plotTitle,
           "Number of Fitness Evaluations","Fitness",
           dataset,
           PlotOrientation.VERTICAL,
           true,true,false);
           
           ChartPanel chartPanel = new ChartPanel( xylineChart );
           chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
            XYPlot plot = xylineChart.getXYPlot( );
           
           XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
           Color[] colourList = {Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY,Color.GRAY,Color.GREEN,Color.LIGHT_GRAY,Color.MAGENTA,Color.orange,Color.PINK,Color.red,Color.YELLOW};
           for(int i = 0; i < datasetName.length; i++){
            renderer.setSeriesPaint( i , colourList[i%colourList.length]);
            renderer.setSeriesStroke( i , new BasicStroke( 4.0f ) );

           }
           plot.setRenderer( renderer ); 
           setContentPane( chartPanel ); 
     }
     private XYDataset createDataset(int[][]x, int[][]y, String[] datasetName ) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for(int i = 0; i < x[0].length; i++){
                XYSeries xyLine = new XYSeries(datasetName[i]);
                for(int j = 0; j<x.length; j++){
                    xyLine.add(x[i][j], y[i][j]);
                }
                dataset.addSeries(xyLine);
                
            
        }
        return dataset;
     }
     public void savePlot(String pathname){
   
         int width = 640;    /* Width of the image */
         int height = 480;   /* Height of the image */ 
         File lineChartFile = new File(pathname); 
         try{
         ChartUtilities.saveChartAsJPEG(lineChartFile ,xylineChart, width ,height);
         }catch (Exception e){
            System.out.println("An issue has occured:\n" + e.toString());
         }
     }
     
}
