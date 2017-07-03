import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.jfree.data.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;



/** Thread for automatic fitting.
 *
 * This is the high-level interface to Octave fitting code. It does the actual
 * interfacing in a separate thread in order to allow normal use of the program
 * while an automatic fitting is in progress. Results are reported to tasks
 * that are invoked in the event thread.
 */

public class JavaFitter implements FitterInterface {
    private Thread t;
    private ExecutorService exec;
    private JPlotArea light;
    private LayerStack stack;
    private Image green, yellow;
    private LayerTask endTask;
    private LayerTask plotTask;
    private Runnable errTask;
    private Algorithm algo;
    private volatile boolean closing = false;
    private int iterations;
    private XRRFittingCtx ctx;


    /** Constructor.
     *
     * Fit the layer model represented by the parameter stack to the
     * measurement data in the parameter data in another thread. An internal
     * copy of the stack and the data is made, so they can be used without
     * having to worry about thread synchronization.
     *
     * The fitting is implemented in Octave code, so the fitting thread needs
     * an exclusive access to oct. Before accessing oct, a lock must always be
     * obtained.
     *
     * Progress is reported by periodically calling plotTask so that fitting
     * progress can be plotted in the user interface.
     *
     * After the fitting is completed, endTask is called. In the case of an
     * error, errTask is called instead.
     *
     * @param light A light which is either yellow (during fitting) or green.
     *              The color is changed automatically by this thread.
     * @param green A green image for the light
     * @param yellow A yellow image for the light
     * @param oct Octave
     * @param data The measurement data used to fit the layer model
     * @param stack The layer model to fit
     *
     * @param endTask Called after the fitting is complete
     * @param plotTask Called periodically to report fitting progress
     * @param errTask Called when an Octave error has occurred during the fitting
     *
     * @param popsize Option for the fitting code: population size
     * @param iterations Option for the fitting code: the number of iterations
     * @param firstAngle Option for the fitting code: the minimum angle to include in fitting
     * @param lastAngle Option for the fitting code: the maximum angle to include in fitting
     * @param algo Option for the fitting code: the algorithm to use
     *
     */

    public JavaFitter(JPlotArea light, GraphData data, LayerTask endTask, LayerTask plotTask, Runnable errTask, LayerStack stack, int popsize, int iterations, double firstAngle, double lastAngle, Image green, Image yellow,
            Algorithm algo, FitnessFunction func, double dBthreshold, int pNorm,
            AdvancedFitOptions opts) throws FittingNotStartedException {
        FittingErrorFunc func2;
        stack = stack.deepCopy();
        data = data.normalize(stack).convertToLinear();
        data = data.crop(firstAngle, lastAngle);
        if (data.alpha_0.length < 2)
        {
            throw new FittingNotStartedException();
        }
        this.green = green;
        this.yellow = yellow;
        this.light = light;
        this.endTask = endTask;
        this.plotTask = plotTask;
        this.errTask = errTask;
        this.iterations = iterations;
        this.stack = stack;
        closing = false;
        t = new Thread(new Runnable() {
            public void run() {
                runThread();
            }
        });
        int cpus = Runtime.getRuntime().availableProcessors();
        boolean ok = false;
        this.exec = new ThreadPoolExecutor(cpus, cpus,
                                           1, TimeUnit.SECONDS,
                                           new LinkedBlockingDeque<Runnable>());
        switch (func)
        {
          case relchi2:
            func2 = new RelChi2FittingErrorFunc(Math.exp(Math.log(10)*dBthreshold/10));
            break;
          case logfitness:
            func2 = new LogFittingErrorFunc(pNorm);
            break;
          case sqrtfitness:
            func2 = new SqrtFittingErrorFunc(pNorm);
            break;
          case chi2:
            func2 = new Chi2FittingErrorFunc();
            break;
          default:
            throw new IllegalArgumentException();
        }
        try {
            this.ctx = new XRRFittingCtx(stack, data,
                                         algo == Algorithm.JavaCovDE,
                                         algo != Algorithm.JavaEitherOrDE,
                                         popsize, func2, exec, opts);
            ok = true;
        }
        finally
        {
            if (!ok)
            {
                exec.shutdown();
            }
        }
        t.start();
    }

    /** Stop the fitting without waiting. */
    public void closeWithoutWaiting() {
        closing = true;
    }

    /** Stop the fitting and wait. */
    public void close() {
        boolean ok = false;
        closing = true;
        while(!ok) {
            try {
                t.join();
                ok = true;
            }
            catch(InterruptedException e) {}
        }
    }


    /* This methods runs in another thread.
     * It acquires the following locks:
     * - oct, when octave is called
     */
    private void runThread() {
        light.newImage(yellow);
        try {
            for(int round = 0; round < iterations && !closing; round++) {
                double[] results;
                double bestfit, medianfit;
                String msg;
                ctx.iteration();
                bestfit = ctx.bestFittingError();
                medianfit = ctx.medianFittingError();
                results = ctx.bestIndividual();
                stack.setFitValues(results);
                msg = "iteration = "+(round+1) + ", bestfit = " + String.format(Locale.US,"%.4g",bestfit) + ", medianfit = "+String.format(Locale.US,"%.4g",medianfit);
                final String msg2 = msg;

                final LayerStack stackToPlot = stack.deepCopy();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if(plotTask != null)
                            plotTask.run(stackToPlot,msg2);
                    }
                });
            }
        }
        catch(Throwable t) {
            SwingUtilities.invokeLater(errTask);
            light.newImage(green);
            this.exec.shutdown();
            return;
        }
        this.exec.shutdown();
        final LayerStack stackToReturn = stack.deepCopy();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(plotTask != null)
                    plotTask.run(stackToReturn,"");
                if(endTask != null)
                    endTask.run(stackToReturn,"");
            }
        });
        light.newImage(green);
    }
}
