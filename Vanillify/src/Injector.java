import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
public class Injector{
    public static void main(String[] args) throws IOException{
        //get the file containing the Vanillify repository
        File vanillifyInjectorRoot = new File("").getAbsoluteFile();
        File vanillifyProjectRoot = vanillifyInjectorRoot.getParentFile();
        File root = vanillifyProjectRoot.getParentFile();
        File cfg = new File(vanillifyProjectRoot, "injector.cfg");
        if(!cfg.exists()){
            System.out.println("Could not find configuration file at "+cfg.getAbsolutePath()+"! Vanillify injector will now exit.");
            return;
        }
        System.out.println("Root: "+root.getAbsolutePath());
        ArrayList<String> specifiedJarfiles = new ArrayList<>();
        for(String line : Files.readAllLines(cfg.toPath())){
            specifiedJarfiles.add(new File(root, line).getAbsolutePath());
        }
        File targetBuildFolder = new File(vanillifyInjectorRoot, "build/classes");
        HashMap<File, String> toInject = new HashMap<>();
        toInject.put(new File("build/classes/com/thizthizzydizzy/vanillify/version/VersionMatcher.class"), "com/thizthizzydizzy/vanillify/version/VersionMatcher.class");
        toInject.put(new File("build/classes/net/wesjd/anvilgui/version/VersionMatcher.class"), "net/wesjd/anvilgui/version/VersionMatcher.class");
        for(File f : vanillifyProjectRoot.listFiles()){
            if(f.isDirectory()&&f.getName().startsWith("VS")){
                String version = f.getName().substring(2);
                System.out.println("Found "+f.getName());
                File buildFolder = new File(f, "build/classes");
                File vanillify = new File(buildFolder, "com/thizthizzydizzy/vanillify/version");
                //versionmatcher
                for(File van : vanillify.listFiles()){
                    if(van.getName().startsWith("Wrapper"+version)){
                        File targetVanillify = new File(targetBuildFolder, "com/thizthizzydizzy/vanillify/version/"+van.getName());
                        targetVanillify.delete();
                        Files.copy(van.toPath(), targetVanillify.toPath());
                        System.out.println("Copied "+van.getAbsolutePath()+" to "+targetVanillify.getAbsolutePath());
                        toInject.put(van, "com/thizthizzydizzy/vanillify/version/"+van.getName());
                    }
                }
                File anvilgui = new File(buildFolder, "net/wesjd/anvilgui/version");
                for(File anv : anvilgui.listFiles()){
                    if(anv.getName().startsWith("Wrapper"+version)){
                        File targetAnvilGUI = new File(targetBuildFolder, "net/wesjd/anvilgui/version/"+anv.getName());
                        targetAnvilGUI.delete();
                        Files.copy(anv.toPath(), targetAnvilGUI.toPath());
                        System.out.println("Copied "+anv.getAbsolutePath()+" to "+targetAnvilGUI.getAbsolutePath());
                        toInject.put(anv, "net/wesjd/anvilgui/version/"+anv.getName());
                    }
                }
                anvilgui = new File(buildFolder, "net/wesjd/anvilgui/version/special");
                if(anvilgui.exists()){
                    for(File anv : anvilgui.listFiles()){
                        File targetAnvilGUI = new File(targetBuildFolder, "net/wesjd/anvilgui/version/special/"+anv.getName());
                        targetAnvilGUI.delete();
                        targetAnvilGUI.getParentFile().mkdirs();
                        Files.copy(anv.toPath(), targetAnvilGUI.toPath());
                        System.out.println("Copied "+anv.getAbsolutePath()+" to "+targetAnvilGUI.getAbsolutePath());
                        toInject.put(anv, "net/wesjd/anvilgui/version/special/"+anv.getName());
                    }
                }
            }
        }
        System.out.println("Injecting "+toInject.size()+" Vanillify files into "+specifiedJarfiles.size()+" specified jarfile"+(specifiedJarfiles.size()==1?"":"s")+"...");
        for(String jarPath : specifiedJarfiles){
            File jar = new File(jarPath);
            File tempjar = new File(jar.getAbsolutePath()+".temp");
            if(!jar.exists()){
                System.err.println("Jarfile not found: "+jar.getName()+"! ("+jarPath+")");
                continue;
            }
            tempjar.delete();
            Files.copy(jar.toPath(), tempjar.toPath());
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(jar));//modify dat file!
            ZipInputStream zis = new ZipInputStream(new FileInputStream(tempjar));
            ZipEntry ze;
            byte[] buffer = new byte[2048];
            ENTRY:
            while((ze = zis.getNextEntry())!=null){
                for(File f : toInject.keySet()){
                    String path = toInject.get(f);
                    if(ze.getName().equals(path)){
                        System.out.println("Skipping "+ze.getName());
                        continue ENTRY;
                    }
                }
                zos.putNextEntry(ze);
                int len;
                System.out.println("Copying "+ze.getName());
                while((len = zis.read(buffer))>0){
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
            }
            zis.close();
            for(File file : toInject.keySet()){
                if(!file.exists()){
                    System.err.println("Could not find injection target: "+file.getName()+"! ("+file.getAbsolutePath()+")");
                    continue;
                }
                zos.putNextEntry(new ZipEntry(toInject.get(file)));
                byte[] bytes = Files.readAllBytes(file.toPath());
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
                System.out.println("Injected "+file.getName()+" into "+jar.getName()+" at "+toInject.get(file));
            }
            zos.close();
            tempjar.delete();
        }
        System.out.println("Done!");
    }
}
