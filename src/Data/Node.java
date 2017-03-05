package Data;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Marcel on 05.03.2017.
 */
public class Node {
    private final String name;
    private long size=-1;
    private final Node[] subNodes;
    private final File ownPath;

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Node[] getSubNodes() {
        return subNodes;
    }

    public File getOwnPath() {
        return ownPath;
    }

    public Node(File file){
        this.name=file.getName();
        this.subNodes=new Node[getSubdirectoryCount(file)];
        this.ownPath=file;
    }


    public void calculateSubnodes(){
        File[] subDirectories=getSubdirectories(this.ownPath);
        for(int i=0;i<subNodes.length;i++){
            subNodes[i]=new Node(subDirectories[i]);
        }
        if(this.subNodes.length<1){
            this.size=getSizefromPath(this.ownPath);
        }else{
            for(int i=0;i<subNodes.length;i++){
                subNodes[i].calculateSubnodes();
            }
        }
    }



    public void calculateSize(){
        for(Node n:subNodes){
            if(n.getSize()==-1){
                n.calculateSize();
            }
            this.size+=n.getSize();
        }
        this.size+=getSizeofFiles(this.ownPath);

    }

    public static int getSubdirectoryCount(File f){
        try {
            return f.listFiles(File::isDirectory).length;
        }catch(NullPointerException ignored){
            return 0;
        }
    }

    public static File[] getSubdirectories(File f){
        return f.listFiles(file -> file.isDirectory());
    }

    public static long getSizefromPath(File f) {
        final AtomicLong size = new AtomicLong(0);
        Path path=f.toPath();
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    size.addAndGet(attrs.size());
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
        }

        return size.get();
    }

    public static long getSizeofFiles(File f){
        long size=0;
        for(File cur:f.listFiles()){
            if(cur.isFile()){
                try {
                    size+=Files.size(cur.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return size;
    }

    public static void main(String[] args){
        JFileChooser fs = new JFileChooser(new File("c:\\documents"));
        fs.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fs.setDialogTitle("save");
        //fs.setFileFilter(new FileNameExtensionFilter("Image", "jpeg","png"));
        int returnVal = fs.showSaveDialog(null);
        switch (returnVal) {
            case JFileChooser.APPROVE_OPTION:
                File input = fs.getSelectedFile();
                if (input.exists()) {
                    Node node=new Node(input);
                    node.calculateSubnodes();
                    node.calculateSize();
                } else {
                }
                fs.setVisible(false);
                break;
            case JFileChooser.CANCEL_OPTION:
                fs.setVisible(false);
                break;
        }
    }

}