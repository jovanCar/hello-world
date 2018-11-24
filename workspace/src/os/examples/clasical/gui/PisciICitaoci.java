package os.examples.clasical.gui;

import java.awt.Color;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import os.simulation.SimulationContainer;
import os.simulation.SimulationContainerLayout;
import os.simulation.SimulationThread;
import os.simulation.gui.NoAnimationPanel;
import os.simulation.gui.SimulationFrame;
import os.simulation.gui.SimulationPanel;
import os.simulation.gui.swing.SwingSimulationPanel;

public class PisciICitaoci {

    private Baza baza;


    private class Baza {

        private int brojCitaoca = 0;
        private int brojPisca = 0;

        private Lock l = new ReentrantLock();
        private Condition c = l.newCondition();
        private Condition p = l.newCondition();

        public void zauzmiP() {
            l.lock();
            try {
                while (brojPisca + brojCitaoca > 0) {
                    p.awaitUninterruptibly();
                }
                brojPisca++;
            } finally {
                l.unlock();
            }
        }

        public void oslobodiP() {
            l.lock();
            try {
                c.signalAll();
                p.signal();
            } finally {
                l.unlock();
            }
        }

        public void zauzmiC() {
            l.lock();
            try {
                while (brojPisca > 0) {
                    c.awaitUninterruptibly();
                }
                brojPisca++;
            } finally {
                l.unlock();
            }
        }

        public void oslobodiC() {
            l.lock();
            try {
                brojCitaoca--;
                if (brojCitaoca == 0) {
                    p.signal();
                }
            } finally {
                l.unlock();
            }
        }

    }

    private class Pisac extends UtilThread {

        public Pisac(int id) {
            super(id);
        }

        @Override
        protected void run() {
            while (!isStopRequested()) {
                step();
                waitWhileSuspended();
            }
        }

        @Override
        protected void step() {
            radiNestoDrugo();
            baza.zauzmiP();
            try {
                pise();
            } finally {
                baza.oslobodiP();
            }
        }
    }

    private class Citalac extends UtilThread {

        public Citalac(int id) {
            super(id);
        }

        @Override
        protected void run() {
            while (!isStopRequested()) {
                step();
                waitWhileSuspended();
            }
        }

        @Override
        protected void step() {
            radiNestoDrugo();
            baza.zauzmiC();
            try {
                cita();
            } finally {
                baza.oslobodiC();
            }
        }
    }


    // Parametri simulacije
    public static final int    BROJ_PISACA       = 3;
    public static final int    BROJ_CITALACA     = 5;
    public static final int    DUZINA_PISANJA    = 5000;
    public static final int    DUZINA_CITANJA    = 3000;
    public static final int    DUZINA_NE_PISANJA = 4000;
    public static final int    DUZINA_NE_CITANJA = 4000;

    // Boje
    public static final Color  BOJA_PISACA      = new Color(255, 192, 192);
    public static final Color  BOJA_CITALACA    = new Color(192, 192, 255);
    public static final Color  BOJA_VAN_PISCI   = new Color(128, 0, 0);
    public static final Color  BOJA_VAN_CITAOCI = new Color(0, 0, 128);
    public static final Color  BOJA_UNUTRA      = new Color(255, 255, 255);

    // Stringovi
    public static final String TEXT_VAN_PISCI           = "\u041F\u0438\u0441\u0446\u0438";
    public static final String TEXT_VAN_CITAOCI         = "\u0427\u0438\u0442\u0430\u043E\u0446\u0438";
    public static final String TEXT_UNUTRA              = "\u0411\u0430\u0437\u0430";
    public static final String TEXT_PISE                = "\u041F\u0438\u0448\u0435";
    public static final String TEXT_UPISAO              = "\u0423\u043F\u0438\u0441\u0430\u043E";
    public static final String TEXT_CITA                = "\u0427\u0438\u0442\u0430";
    public static final String TEXT_PROCITAO            = "\u041F\u0440\u043E\u0447\u0438\u0442\u0430\u043E";
    public static final String TEXT_RADI_NESTO_DRUGO    = "\u0420\u0430\u0434\u0438";
    public static final String TEXT_ZAVRSIO_NESTO_DRUGO = "\u0417\u0430\u0432\u0440\u0448\u0438\u043E";

    private class UtilThread extends SimulationThread {

        private SimulationContainer van;
        private int vreme;

        protected UtilThread(int id) {
            super();
            if (Pisac.class.isAssignableFrom(this.getClass())) {
                setName("\u041F\u0438\u0441\u0430\u0446 " + id);
                setColor(BOJA_PISACA);
                van = vanPisci;
                vreme = DUZINA_NE_PISANJA;
            }
            if (Citalac.class.isAssignableFrom(this.getClass())) {
                setName("\u0427\u0438\u0442\u0430\u043B\u0430\u0446 " + id);
                setColor(BOJA_CITALACA);
                van = vanCitaoci;
                vreme = DUZINA_NE_CITANJA;
            }
        }

        protected void pise() {
            setContainer(unutra);
            setText(TEXT_PISE);
            work(DUZINA_PISANJA, 2 * DUZINA_PISANJA);
            setText(TEXT_UPISAO);
        }

        protected void cita() {
            setContainer(unutra);
            setText(TEXT_CITA);
            work(DUZINA_CITANJA, 2 * DUZINA_CITANJA);
            setText(TEXT_PROCITAO);
        }

        protected void radiNestoDrugo() {
            setContainer(van);
            setText(TEXT_RADI_NESTO_DRUGO);
            work(vreme, 2 * vreme);
            setText(TEXT_ZAVRSIO_NESTO_DRUGO);
        }
    }

    // Glavni program
    public static void main(String[] a) {
        new PisciICitaoci();
    }

    // Stanja
    private SimulationContainer vanPisci = new SimulationContainer(TEXT_VAN_PISCI, BOJA_VAN_PISCI, SimulationContainerLayout.BOX);
    private SimulationContainer vanCitaoci = new SimulationContainer(TEXT_VAN_CITAOCI, BOJA_VAN_CITAOCI, SimulationContainerLayout.BOX);
    private SimulationContainer unutra = new SimulationContainer(TEXT_UNUTRA, BOJA_UNUTRA, SimulationContainerLayout.BOX);
    private SimulationContainer van = new SimulationContainer(SimulationContainerLayout.ROW, vanPisci, vanCitaoci);
    private SimulationContainer sve = new SimulationContainer(SimulationContainerLayout.COLUMN, van, unutra);

    public PisciICitaoci() {

        // Create frame
        SimulationPanel panel = new SwingSimulationPanel(sve);
        SimulationFrame frame = SimulationFrame.create("Pisci i citaoci", panel, new NoAnimationPanel());
        frame.display();

        // Create threads
        for (int i = 1; i <= BROJ_PISACA; i++) {
            new Pisac(i).start();
        }
        for (int i = 1; i <= BROJ_CITALACA; i++) {
            new Citalac(i).start();
        }

    }
}