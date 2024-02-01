package main.java.ass03_parte2.View;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SimulationView {

    private static final int FRAME_ROWS = 3;
    private static final int FRAME_COLUMNS = 3;
    private static final int PANEL_ROWS = 3;
    private static final int PANEL_COLUMNS = 2;
    private static final int CONTROL_FRAME_HEIGHT = 300;
    private static final int WIDTH = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / FRAME_COLUMNS;
    private static final int CONTROL_FRAME_WIDTH = WIDTH * FRAME_COLUMNS;
    private static final int SPACE_FOR_HEIGHT = 27;
    private static final double HEIGHT = (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - CONTROL_FRAME_HEIGHT) / FRAME_ROWS ;

    private static final int CONTROL_PANEL_RESUME_WIDTH = CONTROL_FRAME_WIDTH / 6;
    private static final int CONTROL_PANEL_WIDTH = (CONTROL_FRAME_WIDTH - CONTROL_PANEL_RESUME_WIDTH) / (FRAME_ROWS*FRAME_COLUMNS);

    private final HashMap<Integer, ArrayList<JPanel>> mapSensor = new HashMap<>();
    private final HashMap<Integer, JFrame> mapZone = new HashMap<>();
    private final ArrayList<JPanel> panelControl = new ArrayList<>();
    private final ArrayList<JTextArea> resumeTextAreas = new ArrayList<>();

    public ArrayList<JPanel> getPanelControl() {
        return panelControl;
    }

    public SimulationView() {}

    public HashMap<Integer, ArrayList<JPanel>> getMap() {
        return this.mapSensor;
    }


    public void createFrames() {
        int area = 0;
        int contSensori = 0;
        ArrayList<JPanel> panels;

        //Creazione 6 aree.
        for(int cont = 0; cont < FRAME_ROWS; cont++) {
            for (int cont1 = 0; cont1 < FRAME_COLUMNS; cont1++) {
                JFrame frame = new JFrame("Area " + area + " - FREE");
                frame.setResizable(false);
                frame.getContentPane().setLayout(null);
                frame.setSize(WIDTH, (int) HEIGHT - SPACE_FOR_HEIGHT);
                frame.setLocation(cont1 * WIDTH,  (cont * frame.getHeight()) + SPACE_FOR_HEIGHT);

                panels = new ArrayList<>();

                //Creazioni 6 pannelli dentro ogni area.
                for(int j = 0; j < PANEL_ROWS; j++){
                    for(int i = 0; i < PANEL_COLUMNS; i++){
                        JPanel p = new JPanel();
                        JTextArea textArea = new JTextArea("Area [" + area + "]\n" + "Sensore [" + contSensori + "]");
                        textArea.setOpaque(false);
                        textArea.setEditable(false);
                        p.add(textArea);
                        p.setName("" + cont);
                        p.setSize(frame.getWidth() / PANEL_COLUMNS,  (frame.getHeight() - SPACE_FOR_HEIGHT) / PANEL_ROWS);
                        p.setBackground(new Color(185, 181, 182));
                        p.setBorder(BorderFactory.createLineBorder(Color.black));
                        p.setLocation(i * (frame.getWidth() / PANEL_COLUMNS), j * ((frame.getHeight() - SPACE_FOR_HEIGHT)  / PANEL_ROWS));
                        frame.getContentPane().add(p);
                        panels.add(p);

                        contSensori++;
                    }
                }

                this.mapSensor.put(area, panels);
                this.mapZone.put(area, frame);
                area++;
                contSensori = 0;
                frame.setVisible(true);
            }
        }
    }

    public void createControl() {
        JFrame controlFrame = new JFrame("Control Panel");
        controlFrame.setResizable(false);
        controlFrame.getContentPane().setLayout(null);
        controlFrame.setSize(CONTROL_FRAME_WIDTH, CONTROL_FRAME_HEIGHT - SPACE_FOR_HEIGHT);
        controlFrame.setLocation(0, (int) (FRAME_ROWS * (HEIGHT - SPACE_FOR_HEIGHT) + SPACE_FOR_HEIGHT));

        for(int i = 0; i < FRAME_ROWS*FRAME_COLUMNS; i++) {
            JPanel panel = new JPanel();
            panel.setSize(CONTROL_PANEL_WIDTH, CONTROL_FRAME_HEIGHT);
            panel.setLocation(i * CONTROL_PANEL_WIDTH, 0);
            panel.setBorder(BorderFactory.createLineBorder(Color.black));
            panel.setBackground(new Color(121, 159, 81));

            JTextArea textArea = new JTextArea("Firestation " + i);
            textArea.setOpaque(false);
            textArea.setEditable(false);

            JButton manageButton = new JButton("Manage Alarm");
            manageButton.setName("" + i);
            manageButton.setLocation((CONTROL_PANEL_WIDTH / 3) * (i+1), 30);
            manageButton.setEnabled(false);
            JButton resolveButton = new JButton("Resolve Alarm");
            resolveButton.setName("" + i);
            resolveButton.setLocation((CONTROL_PANEL_WIDTH / 3) * (i+1), 50);
            resolveButton.setEnabled(false);

            manageButton.addActionListener(e -> {
                int areaID = Integer.parseInt(manageButton.getName());

                // Intestazione JFrame con sensori
                this.mapZone.get(areaID).setTitle("Area " + areaID + " - BUSY");
                // Resume text area
                this.resumeTextAreas.get(areaID).setText("Firestation " + areaID + ": busy" );
                this.resumeTextAreas.get(areaID).setForeground(Color.RED);
                // Colore control panel
                this.panelControl.get(areaID).setBackground(new Color(238, 200, 79));

                // Disabilito manage + abilito resolve
                this.getPanelControl().get(areaID).getComponent(1).setEnabled(false);
                this.getPanelControl().get(areaID).getComponent(2).setEnabled(true);

                // Coloro di giallo i sensori rossi
                for (JPanel p: this.mapSensor.get(areaID)) {
                    if(p.getBackground().equals(new Color(196, 77, 77))){
                        p.setBackground(new Color(238, 200, 79));
                    }
                }
            });


            resolveButton.addActionListener(e -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                int areaID = Integer.parseInt(resolveButton.getName());

                // Intestazione JFrame con sensori
                this.mapZone.get(areaID).setTitle("Area " + areaID + " - FREE");
                // Resume text area
                this.resumeTextAreas.get(areaID).setText("Firestation " + areaID + ": free" );
                this.resumeTextAreas.get(areaID).setForeground(Color.BLACK);
                // Colore control panel
                this.panelControl.get(areaID).setBackground(new Color(121, 159, 81));
                // Disabilito resolve
                this.getPanelControl().get(areaID).getComponent(2).setEnabled(false);

                // Coloro di grigio i sensori gialli
                for (JPanel p: this.mapSensor.get(areaID)) {
                    if (p.getBackground().equals(new Color(238, 200, 79))) {
                        p.setBackground(new Color(185, 181, 182));
                    }
                }
            });

            panel.add(textArea);
            panel.add(manageButton);
            panel.add(resolveButton);

            this.panelControl.add(panel);

            controlFrame.getContentPane().add(panel);
        }


        JTextArea jTextArea = new JTextArea("Resume of firestations");
        jTextArea.setFont(jTextArea.getFont().deriveFont(Font.BOLD, jTextArea.getFont().getSize()));
        jTextArea.setOpaque(false);
        jTextArea.setEditable(false);
        jTextArea.setLocation(FRAME_ROWS*FRAME_COLUMNS * CONTROL_PANEL_WIDTH + 30, 10);
        jTextArea.setSize(CONTROL_PANEL_RESUME_WIDTH, 100);

        controlFrame.getContentPane().add(jTextArea);


        for(int i = 0; i < FRAME_ROWS*FRAME_COLUMNS; i++) {
            JTextArea textArea = new JTextArea();
            textArea.setOpaque(false);
            textArea.setEditable(false);
            textArea.setText("Firestation " + i + ": free" );
            textArea.setLocation(FRAME_ROWS*FRAME_COLUMNS * CONTROL_PANEL_WIDTH + 30, i * 20 + 30);
            textArea.setSize(CONTROL_PANEL_RESUME_WIDTH, 100);
            textArea.setBackground(new Color(i * 20, i * 20, 100));

            this.resumeTextAreas.add(textArea);
            controlFrame.getContentPane().add(textArea);
        }

        controlFrame.setVisible(true);
    }
}
