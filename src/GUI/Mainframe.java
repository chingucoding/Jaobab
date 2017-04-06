package GUI;

import Data.Node;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Created by Marcel on 06.03.2017.
 */
public class Mainframe extends JFrame {


    private JButton chooseDirectoy;
    private JPanel rootPanel;
    private TreeviewPanel treeview;
    private SunviewPanel sunview;
    private JButton analyzeButton;
    private JLabel currentPathLabel;
    private JLabel progressLabel;
    private JPanel displayPanel;
    private JLabel pathLabelStatic;

    private static Node supernode;

    private boolean threadStarted = false;
    private boolean pathChangedSinceLastAnalysis = false;

    private static Mainframe instance;

    public Mainframe() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setContentPane(this.rootPanel);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setMinimumSize(new Dimension(800, 540));
        this.setPreferredSize(new Dimension(960, 540));
        this.setSize(new Dimension(960, 540));
        this.pack();
        this.setVisible(true);
        instance=this;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }

    private void createUIComponents() {
        rootPanel = new JPanel();
        chooseDirectoy = new JButton();
        chooseDirectoy.setFont(GraphicsConstants.standardFont);
        chooseDirectoy.addActionListener(e -> {
            JFileChooser fs = new JFileChooser(new File("c:"));
            fs.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fs.setDialogTitle("save");
            //fs.setFileFilter(new FileNameExtensionFilter("Image", "jpeg","png"));
            int returnVal = fs.showSaveDialog(null);
            switch (returnVal) {
                case JFileChooser.APPROVE_OPTION:
                    File input = fs.getSelectedFile();
                    if (input.exists()) {
                        Node n = new Node(input);
                        // check if the node hasn't changed
                        if (supernode != null) {
                            if (n.getOwnPath().getAbsolutePath().equals(supernode.getOwnPath().getAbsolutePath())) {
                                // path hasn't changed
                            } else {
                                // path changed
                                pathChangedSinceLastAnalysis = true;
                            }
                        } else {
                            // path changed
                            pathChangedSinceLastAnalysis = true;
                        }
                        supernode = new Node(input);
                    } else {
                    }
                    fs.setVisible(false);
                    break;
                case JFileChooser.CANCEL_OPTION:
                    fs.setVisible(false);
                    break;
            }
            currentPathLabel.setText(supernode.getOwnPath().getAbsolutePath());
        });

        analyzeButton = new JButton();
        analyzeButton.setFont(GraphicsConstants.standardFont);
        analyzeButton.addActionListener(e -> {
            if (supernode == null) {
                JFileChooser fs = new JFileChooser(new File("c:"));
                fs.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fs.setDialogTitle("save");
                //fs.setFileFilter(new FileNameExtensionFilter("Image", "jpeg","png"));
                int returnVal = fs.showSaveDialog(null);
                switch (returnVal) {
                    case JFileChooser.APPROVE_OPTION:
                        File input = fs.getSelectedFile();
                        if (input.exists()) {
                            supernode = new Node(input);
                            pathChangedSinceLastAnalysis = true;
                        } else {
                        }
                        fs.setVisible(false);
                        break;
                    case JFileChooser.CANCEL_OPTION:
                        fs.setVisible(false);
                        break;
                }
            }
            if (!pathChangedSinceLastAnalysis) return;
            pathChangedSinceLastAnalysis = false;
            currentPathLabel.setText(supernode.getOwnPath().getAbsolutePath());
            Thread background = new Thread(() -> {
                sunview.displayClaculatingMesssage();
                sunview.setNodeInformation(supernode.getName(), "-calculating-", supernode.getOwnPath().getAbsolutePath());
                treeview.displayClaculatingMesssage();
                if (supernode != null) {
                    supernode.calculateSubnodes(this.progressLabel);
                    this.progressLabel.setText("Calculating sizes");
                    supernode.calculateSize();
                    this.progressLabel.setText("Sorting nodes");
                    supernode.sortNodesSizeReversed();
                    this.progressLabel.setText("Preparing visualization");
                    SunviewPanel.setColorsBasedOnAngle(supernode);
                }
                treeview.showNode(supernode);
                sunview.drawNode(supernode);
                sunview.setNodeInformation(supernode.getName(), supernode.sizeFormated(), supernode.getOwnPath().getAbsolutePath());
                threadStarted = false;
                this.progressLabel.setText("Ready");
            });
            if (!threadStarted) {
                threadStarted = true;
                background.start();
            }
        });

        displayPanel=new JPanel();
        displayPanel.setPreferredSize(new Dimension(GraphicsConstants.treeviewPrefferedFULLHDX+GraphicsConstants.sunviewPrefferedFULLHDX,
                GraphicsConstants.treeviewPrefferedFULLHDY+GraphicsConstants.sunviewPrefferedFULLHDY));
        treeview = new TreeviewPanel();
        treeview.getRootPanel().setPreferredSize(new Dimension(GraphicsConstants.treeviewPrefferedFULLHDX,
                (GraphicsConstants.treeviewPrefferedFULLHDY)));

        sunview = new SunviewPanel(treeview);
        sunview.getRootPanel().setPreferredSize(new Dimension(GraphicsConstants.sunviewPrefferedFULLHDX,
                (GraphicsConstants.sunviewPrefferedFULLHDY)));
        currentPathLabel = new JLabel();
        currentPathLabel.setFont(GraphicsConstants.standardFont);

        progressLabel = new JLabel();
        this.progressLabel.setMinimumSize(new Dimension(this.rootPanel.getWidth() - 20, 15));
        this.progressLabel.setPreferredSize(new Dimension(this.rootPanel.getWidth() - 20, 15));
        this.progressLabel.setMaximumSize(new Dimension(this.rootPanel.getWidth() - 20, 15));
        progressLabel.setFont(GraphicsConstants.standardFont);
        progressLabel.setText("Ready");

        pathLabelStatic = new JLabel();
        pathLabelStatic.setFont(GraphicsConstants.standardFont);
    }

    public static Node getSupernode() {
        return supernode;
    }

    public static void processNode(Node node){
        if (node==supernode) return;
        instance.currentPathLabel.setText(supernode.getOwnPath().getAbsolutePath());
        Thread background = new Thread(() -> {
            supernode=node;
            instance.sunview.displayClaculatingMesssage();
            instance. sunview.setNodeInformation(supernode.getName(), "-calculating-", supernode.getOwnPath().getAbsolutePath());
            instance.treeview.displayClaculatingMesssage();
            if (supernode != null) {
                supernode.calculateSubnodes(instance.progressLabel);
                instance.progressLabel.setText("Calculating sizes");
                supernode.calculateSize();
                instance.progressLabel.setText("Sorting nodes");
                supernode.sortNodesSizeReversed();
                instance.progressLabel.setText("Preparing visualization");
                SunviewPanel.setColorsBasedOnAngle(supernode);
            }
            instance.treeview.showNode(supernode);
            instance.sunview.drawNode(supernode);
            instance.sunview.setNodeInformation(supernode.getName(), supernode.sizeFormated(), supernode.getOwnPath().getAbsolutePath());
            instance.threadStarted = false;
            instance.progressLabel.setText("Ready");
        });
        if (!instance.threadStarted) {
            instance.threadStarted = true;
            background.start();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }
        Mainframe mf = new Mainframe();
        return;
    }

}
