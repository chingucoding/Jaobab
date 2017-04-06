package GUI;

import Data.Node;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Marcel on 06.03.2017.
 */
public class TreeviewPanel implements DataVisualizer {
    private JPanel rootPanel;
    private Tree tree;

    public JPanel getRootPanel() {
        return rootPanel;
    }

    private void createUIComponents() {
        rootPanel = new JPanel(new GridLayout(1, 1));
        // rootPanel.setPreferredSize(new Dimension(GraphicsConstants.treeviewPrefferedFULLHDX,
        //         GraphicsConstants.treeviewPrefferedFULLHDY));
    }

    /***
     * This method shows a JTree with the information from the given node.
     * @param node Node that should be shown.
     */
    public void showNode(Node node) {
        new Thread(() -> {
            // clear rootPanel because there might be another JTree from an analysis before
            rootPanel.removeAll();
            rootPanel.revalidate();
            rootPanel.repaint();
            // create the first DefaultMutableTreeNode for the given supernode
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);
            // add the subnodes, their subnodes etc. and their files
            addSubnodesToTree(node, treeNode);
            // add the files of the supernode
            addFilesToTree(node, treeNode);
            // create the JTree with the DefaultMutableTreeNode of the supernode
            tree = new Tree(treeNode);
            tree.setFont(GraphicsConstants.standardFont);
            tree.setRowHeight(GraphicsConstants.treeRowHeight);
            // it should be possible to scroll when the tree is too long
            JScrollPane scrollTree = new JScrollPane(tree);
            scrollTree.setViewportView(tree);

            // add the scrollable tree to the rootPanel
            rootPanel.add(scrollTree);
            rootPanel.validate();
        }).start();
    }

    /***
     * Adds the subnodes and files of the given Node to the given DefaultMutableTreeNode.
     * When a subnode contains subnodes too these also get added.
     * @param node Node which subnodes should be added.
     * @param treeNode DefaultMutableTreeNode where the nodes should be added to.
     */
    private void addSubnodesToTree(Node node, DefaultMutableTreeNode treeNode) {
        // iterate through every subnode of the given node
        for (Node n : node.getSubNodes()) {
            // create a DefaultMutableTreeNode object for the subnode
            DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(n);
            // add the DefaultMutableTreeNode to the upper tree node
            treeNode.add(newTreeNode);
            n.setTreePath(getPath(newTreeNode));
            // if the subnode contains subnodes add them too
            if (n.getSubNodes().length > 0) {
                addSubnodesToTree(n, newTreeNode);
            }
            // add the information about the files in the subnode
            addFilesToTree(n, newTreeNode);
        }
    }

    /***
     * Counts the files of the node and gets the size of them.
     * A DefaultMutableTreeNode with information about the files get
     * added to the given DefaultMutableTreeNode.
     * @param node Node to get the information of the files.
     * @param treeNode DefaultMutableTreeNode to add the files information to.
     */
    private void addFilesToTree(Node node, DefaultMutableTreeNode treeNode) {
        int filesCount = Node.getFilesCount(node.getOwnPath());
        long sizeOfFiles = Node.getSizeofFiles(node.getOwnPath());
        treeNode.add(new DefaultMutableTreeNode(filesCount + " Files with size " + Node.sizeFormated(sizeOfFiles)));
    }

    @Override
    public void displayClaculatingMesssage() {
        rootPanel.removeAll();
        rootPanel.revalidate();
        rootPanel.repaint();
        Graphics2D g = (Graphics2D) rootPanel.getGraphics();
        g.setColor(Color.darkGray);
        g.setFont(new Font("Arial", Font.BOLD, 35));
        g.drawString("Calculating Node", rootPanel.getWidth() / 2 - 100, rootPanel.getHeight() / 2 - 70);
        rootPanel.repaint();
    }

    public void expandPath(Node node) {
        TreePath path = node.getTreePath();
        tree.expandPath(path);
        tree.scrollPathToVisible(path);
    }

    public static TreePath getPath(TreeNode treeNode) {
        ArrayList<TreeNode> nodes = new ArrayList<>();
        if (treeNode != null) {
            nodes.add(treeNode);
            treeNode = treeNode.getParent();
            while (treeNode != null) {
                nodes.add(0, treeNode);
                treeNode = treeNode.getParent();
            }
        }

        return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
    }

}
