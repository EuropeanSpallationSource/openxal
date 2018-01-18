package xal.extension.jels.matcher;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;

import xal.extension.widgets.beaneditor.SimpleBeanEditor;
import xal.model.IAlgorithm;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.smf.Accelerator;

public class MatcherDialog extends SimpleBeanEditor<Matcher> {

    private static final long serialVersionUID = 1L;

    private JProgressBar progressBar = new JProgressBar();
    private JButton match = new JButton("Match");
    private JButton abort = new JButton("Abort");

    private static final Logger LOGGER = Logger.getLogger(MatcherDialog.class.getName());

    public MatcherDialog(final Frame owner, final Matcher conf, boolean visible) {
        //Set JDialog's owner, title, and modality
        super(owner, "Matcher", null, conf, false, false);

        setModal(false);

        Box bottomPane = new Box(BoxLayout.X_AXIS);
        bottomPane.add(progressBar);
        bottomPane.add(new JSeparator());
        bottomPane.add(match);
        bottomPane.add(abort);

        progressBar.setVisible(false);
        abort.setEnabled(false);

        match.addActionListener((ActionEvent e) -> {
            match.setEnabled(false);
            setEnabled(false);

            publishToBean();

            final Thread matcher = new Thread(getBean());

            matcher.start();
            //progressBar.setIndeterminate(true);

            progressBar.setMaximum(100);
            progressBar.setValue(0);
            progressBar.setVisible(true);
            abort.setEnabled(true);

            new Thread() {
                public void run() {
                    try {
                        while (matcher.isAlive()) {
                            matcher.join(500);
                            progressBar.setValue((int) (getBean().getProgress() * 100));
                        }
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.SEVERE, "Interrupted exception.", e);
                    } finally {
                        progressBar.setVisible(false);
                        reloadBean();
                        setEnabled(true);
                        match.setEnabled(true);
                        abort.setEnabled(false);
                    }
                }
            }.start();
        });

        abort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getBean().abort();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                getBean().abort();
                getBean().dispose();
                dispose();
            }
        });

        add(bottomPane, BorderLayout.SOUTH);

        setVisible(true);
    }

    public static void main(String args[]) throws InstantiationException {
        Accelerator accelerator = Matcher.loadAccelerator();

        IAlgorithm tracker = AlgorithmFactory.createEnvelopeTracker(accelerator);

        EnvelopeProbe probe = ProbeFactory.getEnvelopeProbe(accelerator.getSequence("MEBT"), tracker);

        new MatcherDialog(null, new Matcher(accelerator, probe), true);
    }
}
