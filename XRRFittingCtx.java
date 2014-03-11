import java.util.concurrent.*;
import java.util.*;
public class XRRFittingCtx {
  private GraphData gd;
  private LayerStack s;
  private FittingErrorFunc func;
  private ExecutorService exec;
  private DECtx.CostFunc cost_func;
  private DECtx de_ctx;
  public XRRFittingCtx(LayerStack new_s, GraphData new_gd, boolean cov_on,
                       int npop, FittingErrorFunc new_func,
                       ExecutorService exec)
  {
    this.s = new_s.deepCopy();
    this.gd = new_gd;
    this.func = new_func;
    this.cost_func = new DECtx.CostFunc() {
      public double calculate(double[] p) throws Exception
      {
        LayerStack s2 = s.deepCopy();
        s2.setFitValues(p);
        GraphData gd2 = gd.simulate(s2).normalize(s2);
        return func.getError(gd2.meas, gd2.simul);
      }
    };
    this.de_ctx = new DECtx(
        this.cost_func,
        this.s.getFitValuesForFitting(FitValue.FitValueType.MIN),
        this.s.getFitValuesForFitting(FitValue.FitValueType.MAX),
        this.s.getFitValuesForFitting(FitValue.FitValueType.EXPECTED),
        cov_on, npop, exec);
  }
  public void iteration()
  {
    this.de_ctx.iteration();
  }
  public double[] bestIndividual()
  {
    return this.de_ctx.bestIndividual();
  }
  public double[] medianIndividual()
  {
    return this.de_ctx.medianIndividual();
  }
  public double bestFittingError()
  {
    return this.de_ctx.bestFittingError();
  }
  public double medianFittingError()
  {
    return this.de_ctx.medianFittingError();
  }

  static class TestLookup implements LookupTable {
    private static final Map<String,Element> table;
    static {
        final Map<String,Element> table2 = new HashMap<String,Element>();
        table2.put("Al",new Element(13+0.21, 0.2416, 26.982));
        table2.put("Si",new Element(14+0.26, 0.3249, 28.086));
        table2.put("O",new Element(8+0.052, 3.3705e-2, 15.999));
        table = Collections.unmodifiableMap(table2);
    }
    public Element lookup(String name, double lambda) throws ElementNotFound {
        final double Cu_K_alpha = 1.5405600e-10; /* Cu K_alpha */
        Element e;
        if(Math.abs(lambda - Cu_K_alpha) > 0.001e-10)
            throw new ElementNotFound("Only Cu K_alpha wavelength is supported");
        e = table.get(name);
        if(e == null)
            throw new ElementNotFound("Element "+name+" not found");
        return e;
    }
  }
  public static void main(String[] args) throws Throwable
  {
    double[] alpha_0 = new double[2000];
    LookupTable table = new TestLookup();
    final double lambda = 1.5405600e-10; // Cu K_alpha
    for(int i=0; i<alpha_0.length; i++)
    {
        alpha_0[i] = 3.0*i/alpha_0.length;
    }
    LayerStack layers = new LayerStack(lambda,table);
    layers.add(new Layer("Substrate", new FitValue(0,0,0),
               new FitValue(2.26e3,2.33e3,2.4e3), new FitValue(0,3e-9,3e-9),
               new ChemicalFormula("Si"),new ChemicalFormula("Si"),0,table,lambda));
    layers.add(new Layer("Native oxide", new FitValue(0e-9,7e-9,8e-9),
               new FitValue(1e3,2.1e3,3e3), new FitValue(0,2e-9,2e-9),
               new ChemicalFormula("Si"),new ChemicalFormula("O"),2.0/3,table,lambda));
    layers.add(new Layer("Thin film", new FitValue(40e-9,55e-9,70e-9),
               new FitValue(1e3,3.4e3,4e3), new FitValue(0,1e-9,1e-9),
               new ChemicalFormula("Al"),new ChemicalFormula("O"),3/5.0,table,lambda));
    GraphData gd = new GraphData(alpha_0, new double[2000], new double[2000]);
    double[] simul = gd.simulate(layers).simul;
    gd = new GraphData(alpha_0, simul, simul, false);
    int cpus = Runtime.getRuntime().availableProcessors();
    ThreadPoolExecutor exec =
            new ThreadPoolExecutor(cpus, cpus,
                                   1, TimeUnit.SECONDS,
                                   new LinkedBlockingDeque<Runnable>());
    XRRFittingCtx init = new XRRFittingCtx(layers, gd, true, 40,
                                           new LogFittingErrorFunc(2),
                                           exec);
    for (int i = 0; i < 100; i++)
    {
      init.iteration();
      System.out.println(init.medianFittingError());
      System.out.println(init.medianIndividual()[2]);
      System.out.println("--");
    }
    exec.shutdown();
  }
};
