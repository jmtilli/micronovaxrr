import java.io.*;
import java.util.*;
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

public class Fitter {
    private Thread t;
    private Oct oct;
    private JPlotArea light;
    private LayerStack stack;
    private Image green, yellow;
    private LayerTask endTask;
    private LayerTask plotTask;
    private Runnable errTask;
    private GraphData data;
    private Algorithm algo;
    private volatile boolean closing = false;
    private int iterations;


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
     * @throws OctException If an Octave error has occurred during the preparation for the fitting
     *
     */

    public Fitter(JPlotArea light, Oct oct, GraphData data, LayerTask endTask, LayerTask plotTask, Runnable errTask, LayerStack stack, int popsize, int iterations, double firstAngle, double lastAngle, Image green, Image yellow,
            Algorithm algo, FitnessFunction func, double dBthreshold, int       pNorm) throws OctException {
        stack = stack.deepCopy();
        data = data.normalize(stack).convertToLinear();
        this.data = data;
        this.green = green;
        this.yellow = yellow;
        this.light = light;
        this.oct = oct;
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
        synchronized(oct) {
            oct.putRowVector("alpha_0",data.alpha_0);
            oct.putRowVector("meas",data.meas);
            oct.putScalar("lambda",stack.getLambda());

            if(XRRSimul.isUniformlySpaced(data.alpha_0))
                oct.putScalar("stddevrad",stack.getStdDev().getExpected());
            else
                oct.putScalar("stddevrad",0);

            oct.execute("[fitdeg,fitmeas] = crop(alpha_0,meas,"+firstAngle+","+lastAngle+")");

            String dCmd = "g_d = [0,";
            String edCmd = "g_ed = [0,";
            String ddCmd = "g_dd = [0,";
            String rCmd = "g_r = [";
            String erCmd = "g_er = [";
            String drCmd = "g_dr = [";
            String rhoeCmd = "g_rho_e = [0,";
            String erhoeCmd = "g_erho_e = [0,";
            String drhoeCmd = "g_drho_e = [0,";
            String betaCoeffCmd = "g_beta_coeff = [0,";

            int size = stack.getSize();
            for(int i=0; i<stack.getSize(); i++) {
                Layer l = stack.getElementAt(i);
                Compound c = l.getXRRCompound();

                edCmd += l.getThickness().getExpected()+",";
                if(l.getThickness().getEnabled()) {
                  dCmd += ((l.getThickness().getMax()+l.getThickness().getMin())/2)+",";
                  ddCmd += ((l.getThickness().getMax()-l.getThickness().getMin())/2)+",";
                } else {
                  dCmd += l.getThickness().getExpected()+",";
                  ddCmd += "0,";
                }

                erCmd += l.getRoughness().getExpected()+",";
                if(l.getRoughness().getEnabled()) {
                  rCmd += ((l.getRoughness().getMax()+l.getRoughness().getMin())/2)+",";
                  drCmd += ((l.getRoughness().getMax()-l.getRoughness().getMin())/2)+",";
                } else {
                  rCmd += l.getRoughness().getExpected()+",";
                  drCmd += "0,";
                }

                erhoeCmd += c.getRhoEPerRho()*l.getDensity().getExpected()+",";
                if(l.getDensity().getEnabled()) {
                  rhoeCmd += c.getRhoEPerRho()*((l.getDensity().getMax()+l.getDensity().getMin())/2)+",";
                  drhoeCmd += c.getRhoEPerRho()*((l.getDensity().getMax()-l.getDensity().getMin())/2)+",";
                } else {
                  rhoeCmd += c.getRhoEPerRho()*l.getDensity().getExpected()+",";
                  drhoeCmd += "0,";
                }

                betaCoeffCmd += c.getBetaPerDelta()+",";
            }
            dCmd += "]";
            edCmd += "]";
            ddCmd += "]";
            rCmd += "0]";
            erCmd += "0]";
            drCmd += "0]";
            rhoeCmd += "]";
            erhoeCmd += "]";
            drhoeCmd += "]";
            betaCoeffCmd += "]";

            oct.execute(dCmd);
            oct.execute(rCmd);
            oct.execute(rhoeCmd);
            oct.execute(ddCmd);
            oct.execute(drCmd);
            oct.execute(drhoeCmd);
            oct.execute(edCmd);
            oct.execute(erCmd);
            oct.execute(erhoeCmd);
            oct.execute(betaCoeffCmd);


            if(stack.getProd().getEnabled()) {
                oct.execute("g_prodfactor_min = "+stack.getProd().getMin());
                oct.execute("g_prodfactor = "+stack.getProd().getExpected());
                oct.execute("g_prodfactor_max = "+stack.getProd().getMax());
            } else {
                oct.execute("g_prodfactor_min = "+stack.getProd().getExpected());
                oct.execute("g_prodfactor = "+stack.getProd().getExpected());
                oct.execute("g_prodfactor_max = "+stack.getProd().getExpected());
            }
            if(stack.getSum().getEnabled()) {
                oct.execute("g_sumterm_min = "+stack.getSum().getMin());
                oct.execute("g_sumterm = "+stack.getSum().getExpected());
                oct.execute("g_sumterm_max = "+stack.getSum().getMax());
            } else {
                oct.execute("g_sumterm_min = "+stack.getSum().getExpected());
                oct.execute("g_sumterm = "+stack.getSum().getExpected());
                oct.execute("g_sumterm_max = "+stack.getSum().getExpected());
            }

            oct.sync();

            this.algo = algo;
            oct.execute("g_g.pnorm = "+pNorm);
            oct.execute("g_g.threshold = 10^(("+dBthreshold+")/10)");
            oct.execute("g_ctx = fitDE_initXRR(fitdeg*pi/180, fitmeas, g_beta_coeff, lambda, stddevrad, g_d-g_dd, g_ed, g_d+g_dd, g_rho_e-g_drho_e, g_erho_e, g_rho_e+g_drho_e, g_r-g_dr, g_er, g_r+g_dr, g_prodfactor_min, g_prodfactor, g_prodfactor_max, g_sumterm_min, g_sumterm, g_sumterm_max, '"+oct.escape(algo.octName)+"', "+popsize+",@"+func.octName+", g_g)");
            oct.sync();
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
                String msg;
                synchronized(oct) {
                    oct.sync();
                    double bestfit, medianfit;
                    oct.execute("g_ctx = fitDE(g_ctx)");
                    oct.execute("fitresults = fitDE_best(g_ctx)");
                    oct.execute("bestfitness = fitDE_best_fitness(g_ctx)");
                    oct.execute("medianfitness = fitDE_median_fitness(g_ctx)");
                    bestfit = oct.getMatrix("bestfitness")[0][0];
                    medianfit = oct.getMatrix("medianfitness")[0][0];
                    msg = "iteration = "+(round+1) + ", bestfit = " + String.format(Locale.US,"%.4g",bestfit) + ", medianfit = "+String.format(Locale.US,"%.4g",medianfit);
                    results = oct.getMatrix("fitresults")[0];
                    assert(results.length == 3*(stack.getSize()+1)+2);
                }
                int size = stack.getSize();
                for(int i=0; i<size; i++) {
                    Layer l = stack.getElementAt(i);

                    Compound c;
                    c = l.getXRRCompound();

                    double rho_e = results[2+1*(size+1)+i+1];
                    double rho = rho_e/c.getRhoEPerRho();
                    double d = results[2+0*(size+1)+i+1];
                    double r = results[2+2*(size+1)+i];


                    /* Debugging code: we don't use it because of the limited accuracy of floating point arithmetic */
                    /*
                    if(d < l.getThickness().getMin() || d > l.getThickness().getMax())
                        System.out.println("thickness not in range, min = "+l.getThickness().getMin()+
                                ", val = "+d+", max = "+l.getThickness().getMax()+", layer = "+l);
                    if(rho < l.getDensity().getMin() || rho > l.getDensity().getMax())
                        System.out.println("density not in range, min = "+l.getDensity().getMin()+
                                ", val = "+rho+", max = "+l.getDensity().getMax()+", layer = "+l);
                    if(r < l.getRoughness().getMin() || r > l.getRoughness().getMax())
                        System.out.println("roughness not in range, min = "+l.getRoughness().getMin()+
                                ", val = "+r+", max = "+l.getRoughness().getMax()+", layer = "+l);
                    */

                    if(l.getThickness().getEnabled())
                      l.getThickness().setExpected(d);
                    if(l.getDensity().getEnabled())
                      l.getDensity().setExpected(rho);
                    if(l.getRoughness().getEnabled())
                      l.getRoughness().setExpected(r);
                }

                if(stack.getProd().getEnabled())
                    stack.getProd().setExpected(results[0]);
                if(stack.getSum().getEnabled())
                    stack.getSum().setExpected(results[1]);


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
        catch(OctException ex) {
            SwingUtilities.invokeLater(errTask);
            light.newImage(green);
            return;
        }
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
