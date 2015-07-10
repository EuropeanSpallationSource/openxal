package se.lu.esss.ics.jels.matcher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Formatter;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JFrame;

import se.lu.esss.ics.jels.JElsDemo;
import xal.extension.solver.AlgorithmSchedule;
import xal.extension.solver.AlgorithmScheduleListener;
import xal.extension.solver.Problem;
import xal.extension.solver.ScoreBoard;
import xal.extension.solver.Solver;
import xal.extension.solver.Stopper;
import xal.extension.solver.Trial;
import xal.extension.solver.Variable;
import xal.extension.solver.market.AlgorithmStrategy;
import xal.extension.widgets.olmplot.EnvelopeCurve;
import xal.extension.widgets.olmplot.PLANE;
import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.model.IAlgorithm;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.xml.LatticeXmlWriter;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.tools.annotation.AProperty.NoEdit;
import xal.tools.annotation.AProperty.Units;

public class Matcher implements Runnable, Stopper {
	
	/*private InitialBeamParameters initialParameters = new InitialBeamParameters();	
	private Accelerator accelerator = loadAccelerator();*/
	//private OnlineModelEvaluator evaluator = new MinimiseOscillations(accelerator, initialParameters);
	private OnlineModelEvaluator evaluator; // = new PhaseAdvEvaluator(accelerator, initialParameters);
	
	
	private ModelEvaluatorEnum criteria = ModelEvaluatorEnum.PhaseAdvance;
	
	private double timeLimit = 1.;
	
	private InitialBeamParameters initialBeamParameters;
	
	private boolean showScore;
	private boolean showSimulation;
	
	private String outputInitialRun = null;
	private String outputBestRun = null;
	
	private AcceleratorSeq accelerator;
	
	private boolean aborted;

	private Vector<EnvelopeCurve> data = new Vector<>(2);
	private FunctionGraphsJPanel plot;

	private JFrame scoreFrame; 
	private BasicGraphData scorePlot;
	
	
	public Matcher(AcceleratorSeq accelerator, EnvelopeProbe probe) {
		this.accelerator = accelerator;
		this.initialBeamParameters = new InitialBeamParameters(probe);
	}

	public ModelEvaluatorEnum getCriteria() {
		return criteria;
	}
	
	public void setCriteria(ModelEvaluatorEnum criteria) {
		this.criteria = criteria;
	}	
	
	@Units("min")
	public double getTimeLimit() {
		return timeLimit;
	}
	
	public void setTimeLimit(double timeLimit) {
		this.timeLimit = timeLimit;
	}
	
	public InitialBeamParameters getInitialBeamParameters() {
		return initialBeamParameters;
	}
	
	public boolean isShowScore() {
		return showScore;
	}
	
	public void setShowScore(boolean showScore) {
		this.showScore = showScore;
	}
	
/*	public boolean isShowSimulation() {
		return showSimulation;
	}
	
	public void setShowSimulation(boolean showSimulation) {
		this.showSimulation = showSimulation;
	}*/
	
	@NoEdit
	public AcceleratorSeq getAccelerator()
	{
		return accelerator;
	}
	
	
	public static Accelerator loadAccelerator() {
		/* Loading SMF model */				
		Accelerator accelerator = XMLDataManager.acceleratorWithUrlSpec(JElsDemo.class.getResource("main.xal").toString());
				
		if (accelerator == null)
		{			
			throw new Error("Accelerator is empty. Could not load the default accelerator.");
		} 			
		return accelerator;
	}
	
	static void saveLattice(Lattice lattice, String file) {		
		lattice.setAuthor(System.getProperty("user.name", "ESS"));		
		try {
			LatticeXmlWriter.writeXml(lattice, file);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
	}

			
	public void showSimulationPlot()
	{
		final JFrame frame = new JFrame();
		plot = new FunctionGraphsJPanel();
     	plot.setVisible(true);
     	
     	data.add(new EnvelopeCurve(PLANE.HOR));
     	data.add(new EnvelopeCurve(PLANE.VER));
     	//data.add(new EnvelopeCurve(PLANE.LNG));
    
  	 	plot.addGraphData(data);
  	 	
     	plot.setAxisNames("position", "sigma");
     	
     	plot.refreshGraphJPanel();
     	frame.setSize(500,500);
        frame.add(plot);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	public void updateSimulationPlot(Trajectory<EnvelopeProbeState> t)
	{
		Vector<EnvelopeCurve> data = new Vector<>(2);
		EnvelopeCurve cx = new EnvelopeCurve(PLANE.HOR, t);
     	data.add(cx);
     	
     	EnvelopeCurve cy = new EnvelopeCurve(PLANE.VER, t);
     	data.add(cy);
     	
     	plot.removeGraphData(0);
     	plot.addGraphData(data);		
	}
	
	public void showScorePlot(boolean show)
	{
		if ((scoreFrame == null || !scoreFrame.isDisplayable()) && show) {
			scoreFrame = new JFrame(); 
			scorePlot = new BasicGraphData();

			FunctionGraphsJPanel plot = new FunctionGraphsJPanel();
		
			plot.setVisible(true);
	  	 	plot.addGraphData(scorePlot);
	     	plot.setAxisNames("trial", "score");	     	
	     	plot.refreshGraphJPanel();
			
			scoreFrame.setSize(500,500);
			scoreFrame.add(plot);
			scoreFrame.setVisible(true);
			scoreFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		}
		
		if (show) 
			scorePlot.removeAllPoints();
		
		if (!show && scoreFrame != null) {
			scoreFrame.dispose();
			scoreFrame = null;
			scorePlot = null;
		}
		
	}
	
	private double progress = 0.;
	
	public double getProgress() {
		return progress;
	}
	
	
	public void run()
	{
		aborted = false;
		progress = 0.;
		
		try {
			evaluator = getCriteria().getEvaluatorClass().getConstructor(Matcher.class).newInstance(this);
		} catch (IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		if (showSimulation) {
			showSimulationPlot();
		
			evaluator.addEvaluationListener(new EvaluationListener() {
				@Override
				public void onEvaluation(Trajectory<EnvelopeProbeState> t) {
					updateSimulationPlot(t);
				}
			});
		}
		
		showScorePlot(showScore);
		
		
		InitialBeamParameters initialParameters = getInitialBeamParameters();
		for (Variable v : initialParameters.getVariables()) {
			System.out.printf("%s: %f\n", v.getName(), v.getInitialValue());
		}
		
		Problem problem = new Problem(evaluator.getObjectives(), initialParameters.getVariables(), evaluator);
		
		final Solver solver = new Solver((Stopper)this);
		
		solver.getAlgorithmSchedule().addAlgorithmScheduleListener(new AlgorithmScheduleListener() {
			int i = 0;
			
			@Override
			public void trialVetoed(AlgorithmSchedule algorithmSchedule, Trial trial) {
			}
			
			@Override
			public void trialScored(AlgorithmSchedule algorithmSchedule, Trial trial) {
				System.out.printf("score: %f algo: %s\n", trial.getSatisfaction(), trial.getAlgorithm().getClass());			
				if (showScore) scorePlot.addPoint(i++, trial.getSatisfaction());
				progress = solver.getScoreBoard().getElapsedTime() / (timeLimit*60); 

			}
			
			@Override
			public void strategyWillExecute(AlgorithmSchedule schedule,
					AlgorithmStrategy strategy, ScoreBoard scoreBoard) {
			}
			
			@Override
			public void strategyExecuted(AlgorithmSchedule schedule,
					AlgorithmStrategy strategy, ScoreBoard scoreBoard) {
			}
		});
		
		if (outputInitialRun != null) evaluator.printSolution(outputInitialRun, problem.generateInitialTrialPoint());
		
		solver.solve(problem);
		
		if (outputBestRun != null) {
			evaluator.printSolution(outputBestRun, solver.getScoreBoard().getBestSolution().getTrialPoint());
			try {
				Formatter f1  = new Formatter(outputBestRun+".txt", "UTF8", Locale.ENGLISH);
				
				for (Variable v : initialParameters.getVariables()) {					
					f1.format("%s: %f\n", v.getName(), solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(v));
				}
				
				System.out.printf("best score: %f\n", solver.getScoreBoard().getBestSolution().getSatisfaction());
				f1.format("best score: %f\n", solver.getScoreBoard().getBestSolution().getSatisfaction());
				f1.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	
		// set back final values to initial
		initialParameters.setInitialProbe(initialParameters.getProbe(solver.getScoreBoard().getBestSolution().getTrialPoint()));
		for (Variable v : initialParameters.getVariables()) {
			System.out.printf("%s: %f\n", v.getName(), solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(v));
		}
	}
		
	public static void main(String args[]) throws ModelException, InstantiationException
	{
		Accelerator accelerator = loadAccelerator();
		//IAlgorithm tracker = AlgorithmFactory.createEnvTrackerAdapt( accelerator );
		IAlgorithm tracker = AlgorithmFactory.createEnvelopeTracker( accelerator );
		
		EnvelopeProbe probe = ProbeFactory.getEnvelopeProbe( accelerator.getSequence("MEBT"), tracker );
				
		Matcher me = new Matcher(accelerator, probe);
		me.run();
	}

	public void abort() {
		aborted = true;
	}

	@Override
	public boolean shouldStop(Solver solver) {
		return aborted || solver.getScoreBoard().getElapsedTime() >= timeLimit*60;
	}
	
	public void dispose()
	{
		if (scoreFrame != null) {
			scoreFrame.dispose();
			scoreFrame = null;
		}
	}
}


enum ModelEvaluatorEnum 
{
	PhaseAdvance(PhaseAdvEvaluator.class),
	MinimiseOscillations(MinimiseOscillationsEvaluator.class); 
	
	
	private  Class<? extends OnlineModelEvaluator> c;
	
	ModelEvaluatorEnum(Class<? extends OnlineModelEvaluator> c) {
		this.c = c;
	}
	
	public Class<? extends OnlineModelEvaluator> getEvaluatorClass()
	{
		return c;
	}
}

