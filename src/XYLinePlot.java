package src;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;

import java.io.File;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
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
    public XYLinePlot(String applicationTitle , String plotTitle, double[][] x, double[][]y, String[] datasetName, int lowerYBound ) {
        super(applicationTitle);
        XYDataset dataset = createDataset(x, y, datasetName);
        xylineChart = ChartFactory.createXYLineChart(
           plotTitle,
           "Number of Fitness Evaluations","Fitness",
           dataset,
           PlotOrientation.VERTICAL,
           true,true,false);
           
           ChartPanel chartPanel = new ChartPanel( xylineChart );
           chartPanel.setPreferredSize( new java.awt.Dimension( 1080 , 960 ) );
            XYPlot plot = xylineChart.getXYPlot( );
           
           XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
           Color[] colourList = {Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY,Color.GRAY,Color.GREEN,Color.LIGHT_GRAY,Color.MAGENTA,Color.orange,Color.PINK,Color.red,Color.YELLOW};
           for(int i = 0; i < datasetName.length; i++){
            renderer.setSeriesPaint( i , colourList[i%colourList.length]);
            renderer.setSeriesStroke( i , new BasicStroke( 1.0f ) );

           }
           //To change the lower bound of Y-axis       
           NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
            yAxis.setLowerBound(lowerYBound);
           plot.setRenderer( renderer ); 
           setContentPane( chartPanel ); 
     }
     private XYDataset createDataset(double[][]x, double[][]y, String[] datasetName ) {
        XYSeriesCollection dataset = new XYSeriesCollection();
       //int i = 0;
        for(int i = 0; i < x.length; i++){
                
                XYSeries xyLine = new XYSeries(datasetName[i]);
                for(int j = 1; j<x[0].length; j++){
                    //if(j%100 == 0){
                    xyLine.add(x[i][j], y[i][j]);}
                    
               // }
                dataset.addSeries(xyLine);
                
            
        }
        

        return dataset;
     }
     public void savePlot(String folder, String pathname){
   
         int width = 1080;    /* Width of the image */
         int height = 960;   /* Height of the image */ 
         File lineChartFile = new File(folder, pathname);
         try{
         ChartUtilities.saveChartAsPNG(lineChartFile ,xylineChart, width ,height);
         }catch (Exception e){
            System.out.println("An issue has occured:\n" + e.toString());
         }
     }
     
}
