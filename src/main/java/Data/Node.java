package main.java.Data;

import main.java.Data.Comparator.Sizecomparator;
import main.java.Data.Comparator.SizecomparatorInversed;
import main.java.Data.Threading.NotifyingThread;
import main.java.Data.Threading.Threadmanager;
import main.java.GUI.NodeView;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Class for representing directories
 * Created by Marcel on 05.03.2017.
 */
@SuppressWarnings({"ConstantConditions,WeakerAccess,unused"})
public class Node{
    private final String name;
    private long size=-1;
    private final Node[] subNodes;
    private final File ownPath;
    private Color ownColor;
    private Node parent;

    private double angleStart=0;
    private double angleEnd=0;

    private TreePath treePath;

    private boolean finishedCalculating=false;


    private class CalculateThread extends NotifyingThread{
        private Node node;
        private JLabel progress;

        public CalculateThread(Node node,JLabel progress){
            this.node=node;
            this.progress=progress;
            this.setListener(Threadmanager.getListener());
            this.setName(node.getName());
        }

        @Override
        public void task(){
            node.calculateSubnodesInner(progress);
        }
    }

    /**
     * Constructor using a file
     * @param file this nodes path
     */
    public Node(File file){
        this.name=file.getName();
        this.subNodes=new Node[getSubdirectoryCount(file)];
        this.ownPath=file;
    }

    /**
     * Constructor using a file and parent node
     * @param file this nodes path
     * @param parent the parent node
     */
    public Node(File file, Node parent) {
        this(file);
        this.parent = parent;
    }

    /**
     * Constructor using a file and parent node
     * @param file this nodes path
     * @param parent the parent node
     */
    public Node(File file, Node parent,double angleStart,double angleLength) {
        this(file);
        this.parent = parent;
        this.angleStart=angleStart;
        this.angleEnd=angleStart+angleLength;
    }

    /**
     * Calculates the subnodes of this node
     */
    public void calculateSubnodes(JLabel progress){
        //Checking if interrupted so thread stops
        if(Thread.interrupted()) {
            return;
        }        File[] subDirectories=getSubdirectories(this.ownPath);
        progress.setText("Mapping path: "+this.ownPath.getAbsolutePath());
        for(int i=0;i<subNodes.length;i++){
            subNodes[i]=new Node(subDirectories[i], this);
        }
        if(this.subNodes.length<1){
            this.size=getSizeofFiles(this.ownPath);
        }else{
            for (Node subNode : subNodes) {
                //Checking if interrupted so thread stops
                if(Thread.interrupted()) {
                    return;
                }
                if(Settings.multiThreadingProcessing) {
                    Threadmanager.addThreadProcessing(new CalculateThread(subNode,progress));
                }else{
                    subNode.calculateSubnodesInner(progress);
                }
            }
        }
        boolean flag=true;
        while(flag) {
            //Checking if interrupted so thread stops
            if(Thread.interrupted()) {
                return;
            }
            flag=false;
            for (Node n : subNodes) {
                if (!n.finishedCalculating) {
                    flag=true;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
        finishedCalculating=true;
    }

    private void calculateSubnodesInner(JLabel progress){
        //Checking if interrupted so thread stops
        if(Thread.interrupted()) {
           return;
        }
        File[] subDirectories=getSubdirectories(this.ownPath);
        progress.setText("Mapping path: "+this.ownPath.getAbsolutePath());
        if(subDirectories!=null && subDirectories.length!=0) {
            for (int i = 0; i < subNodes.length; i++) {
                if (Thread.interrupted()) {
                    return;
                }
                try {
                    subNodes[i] = new Node(subDirectories[i], this);
                }catch(ArrayIndexOutOfBoundsException ex){
                    System.err.println("Out of bounds Exception with path: "+this.getName()+" , subnode :"
                    +subDirectories[i]+" index: "+ i+" length: "+subDirectories.length);
                }
            }
        }
        if(this.subNodes.length<1){
            this.size=getSizeofFiles(this.ownPath);
        }else{
            for (Node subNode : subNodes) {
                //Checking if interrupted so thread stops
                if(Thread.interrupted()) {
                    return;
                }
                subNode.calculateSubnodesInner(progress);
            }
        }
        this.finishedCalculating=true;
    }

    /**
     * Calculates the size of this node
     */
    public void calculateSize() {
        //Checking if interrupted so thread stops
        if(Thread.interrupted()) {
            return;
        }
        if (this.size == -1){
            this.size = 0;
            for (Node n : subNodes) {
                if (n.getSize() == -1) {
                    n.calculateSize();
                }
                this.size += n.getSize();
            }
            this.size += getSizeofFiles(this.ownPath);
        }
    }

    /**
     * Creates a window that shows all information about this Node
     */
    public void createDetailedViewWindow(){
        new NodeView(this);
    }

    /***
     * Checks how many files the folder contains.
     * @param folder Folder to get the count of files from.
     * @return Count of files in the given folder.
     */
    public static int getFilesCount(File folder) {
        int files = 0;

        try {
            for (File f : folder.listFiles()) {
                if (f.isFile()) {
                    files++;
                }
            }
        }catch (NullPointerException ignored){}
        return files;
    }

    /***
     * Checks how many files the folder contains.
     * @return Count of files in the given folder.
     */
    public int getFilesCountRecursively() {
        int files = 0;

        try {
            for (File f : this.getOwnPath().listFiles()) {
                if (f.isFile()) {
                    files++;
                }
            }
        }catch (NullPointerException ignored){}
        for (Node subNode : this.subNodes) {
            files += subNode.getFilesCountRecursively();
        }
        return files;
    }

    public double getAngleEnd() {
        return angleEnd;
    }

    public double getAngleStart() {
        return angleStart;
    }

    public String getName() {
        return name;
    }

    public File getOwnPath() {
        return ownPath;
    }

    public Color getOwnColor(){
        return ownColor;
    }

    public Node getParent() {
        return parent;
    }

    public long getSize() {
        return size;
    }


    /**
     * Returns the size of files in a given directory
     * @param f the file representing the directory
     * @return the size in byte (ONLY FILES, NO DIRECTORIES!)
     */
    public static long getSizeofFiles(File f){
        long size=0;
        try {
            for (File cur : f.listFiles()) {
                if (cur.isFile()) {
                    try {
                        size += Files.size(cur.toPath());
                    } catch (IOException  e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch (NullPointerException ignored){

        }
        return size;
    }

    /**
     * Returns all files that are a subdirectory of the given file
     * @param f the file who's subdirectories are being returned
     * @return the array of files that are subdirectories
     */
    public static File[] getSubdirectories(File f){

        return f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                try {
                    return file.isDirectory()&&!isSymlink(file);
                } catch (IOException e) {
                    return file.isDirectory();
                }
            }
        });
    }

    /**
     * Returns the number of subdirectories.
     * If the listfiles method returns null 0 is returned
     * @param f the file to check
     * @return the number of subdirectories
     */
    public static int getSubdirectoryCount(File f){
        try {
            return f.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    try {
                        return file.isDirectory()&&!isSymlink(file);
                    } catch (IOException e) {
                        return file.isDirectory();
                    }
                }
            }).length;
        }catch(NullPointerException ignored){
            return 0;
        }
    }

    public Node[] getSubNodes() {
        return subNodes;
    }

    /**
     * Returns how much of the parents size is being used by this node
     * @return the percentage of how much this node is accounting for the parent node
     */
    public double getUsagePercentOfParent() {
        if(parent == null) return 1;
        long parentSize = parent.getSize();
        long ownSize = getSize();
        return (double)ownSize/parentSize;
    }

    /**
     * Determines if the given file is a link.
     * @param file the file to be checked
     * @return true if file is a symlink
     * @throws IOException IOException when path cant be resolved
     */
    public static boolean isSymlink(File file) throws IOException {
        if (file == null)
            throw new NullPointerException("File must not be null");
        File canon;
        if (file.getParent() == null) {
            canon = file;
        } else {
            File canonDir = file.getParentFile().getCanonicalFile();
            canon = new File(canonDir, file.getName());
        }
        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }

    /**
     * Opens this nodes file in the OS's default file viewer (e.g. Explorer in Windows)
     */
    public void openInOSFileviewer(){
        try {
            if(Desktop.isDesktopSupported())
            Desktop.getDesktop().open(this.ownPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Rounds a number using the default round method (round down till 4, round up after 4).
     * The number will be round to two digits
     * @param value the value
     * @return the rounded number
     */
    public static double round(double value) {
        long factor = (long) Math.pow(10, 2);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public void setAngleEnd(double angleEnd) {
        this.angleEnd = angleEnd;
    }

    public void setAngleStart(double angleStart) {
        this.angleStart = angleStart;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setOwnColor(Color ownColor){
        this.ownColor=ownColor;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
    /**
     * Returns the size of the node with the suitable Byte prefix at the end e.g.:
     * size : 1048576
     * returned string : "1 MB"
     * @return the formated string
     */
    public String sizeFormated(){
        return sizeFormated(this.size);
    }

    /***
     * Calls the sizeFormated method with the given size and not the size of the node.
     * @param size Size to format.
     * @return the formated string
     */
    public static String sizeFormated(long size) {
        if(size<Math.pow(1024,1)){
            return size+"B";
        }if(size<Math.pow(1024,2)) {
            return round((double) size / (Math.pow(1024, 1))) + "KB";
        }if(size<Math.pow(1024,3)) {
            return round((double) size / (Math.pow(1024, 2))) + "MB";
        }if(size<Math.pow(1024,4)) {
            return round((double) size / (Math.pow(1024, 3))) + "GB";
        }if(size<Math.pow(1024,5)) {
            return round((double) size / (Math.pow(1024, 4))) + "TB";
        }if(size<Math.pow(1024,6)) {
            return round((double) size / (Math.pow(1024, 5))) + "PB";
        }else{
            return size+"B";
        }
    }

    /**
     * Sorts the node with a given Comparator
     * @param sc the comparator to use
     */
    public void sortNodes(Comparator sc){
        Arrays.sort(subNodes,sc);
        if(subNodes.length!=0) {
            for (Node n : subNodes) {
                n.sortNodes(sc);
            }
        }
    }

    /**
     * Sorts the nodes after size where the first node is the smallest one
     */
    public void sortNodesSize(){
        Sizecomparator sc=new Sizecomparator();
        Arrays.sort(subNodes,sc);
        for(Node n:subNodes){
            n.sortNodes(sc);
        }
    }

    /**
     * Sorts the nodes after size where the first node is the largest one
     */
    public void sortNodesSizeReversed() {
        SizecomparatorInversed sc=new SizecomparatorInversed();
        Arrays.sort(subNodes,sc);
        for(Node n:subNodes) {
            n.sortNodes(sc);
        }
    }

    /***
     * Needed for the DefaultMutableTreeNodes in TreeviewPanel.
     * @return Name to display for the tree.
     */
    @Override
    public String toString() {
        return getName();
    }

    public TreePath getTreePath() {
        return treePath;
    }

    public void setTreePath(TreePath treePath) {
        this.treePath = treePath;
    }
}
