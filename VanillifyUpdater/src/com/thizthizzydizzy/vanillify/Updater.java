package com.thizthizzydizzy.vanillify;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
public class Updater{
    private static final File workRoot = new File("./work");
    private static final File vanillifyRoot = new File("..");
    private static String templateVersion = "1_20_R1";
    public static void main(String[] argz) throws IOException{
        ArrayList<String> args = new ArrayList<>(Arrays.asList(argz));
        System.out.println("Cleaning up...");
        deleteFolder(workRoot);
        workRoot.mkdirs();

        runProcess("git", "clone", "https://github.com/WesJD/AnvilGUI.git");

        System.out.println("Checking for outdated AnvilGUI versions");
        boolean rebuildProjectReferences = false;
        ArrayList<String> projectNames = new ArrayList<>();
        for(var file : new File(workRoot, "AnvilGUI").listFiles()){
            if(file.isDirectory()&&file.getName().contains("_")){
                String rVersionString = file.getName();
                if(file.getName().split("_").length==4){
                    String[] parts = file.getName().split("_");
                    rVersionString = parts[0]+"_"+parts[1]+"_"+parts[3];//1_19_1_R1 => 1_19_R1
                }
                File anvilGUISrc = new File(file, "src/main/java/net/wesjd/anvilgui");
                if(!anvilGUISrc.exists()){
                    System.out.println("Skipping unrecognized anvilgui structure: "+file.getName()+"!");
                    continue;
                }
//                File specialSrc = new File(anvilGUISrc, "version/special");
//                if(specialSrc.exists()){
//                    System.out.println("Skipping special anvilgui structure: "+file.getName()+"!");
//                    continue;
//                }
                File projectRoot = new File(vanillifyRoot, "VS"+rVersionString);
                File projectVanillifySrc = new File(projectRoot, "src/com/thizthizzydizzy/vanillify");
                File projectAnvilGUISrc = new File(projectRoot, "src/net/wesjd/anvilgui");
                if(!projectRoot.exists()){
                    String mcVersion = null;
                    for(String s : Files.readAllLines(new File(workRoot, "AnvilGUI/api/src/main/java/net/wesjd/anvilgui/version/VersionMatcher.java").toPath())){
                        if(s.trim().contains(rVersionString)&&s.split("\"").length>3)
                            mcVersion = s.split("\"")[1];
                    }
                    if(mcVersion==null){
                        System.out.println("Skipping "+rVersionString+"; no project exists, and unknown MC version!");
                        continue;
                    }
                    System.out.println("Preparing to create project: "+projectRoot.getName());

                    if(args.contains("--skip-BuildTools")){
                        System.out.println("Skipping BuildTools, assuming correct version of bukkit has already been built.");
                    }else{
                        System.out.println("Downloading latest BuildTools.jar");
                        File buildTools = new File(workRoot, "BuildTools.jar");
                        buildTools.delete();
                        downloadFile("https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar", buildTools);
                        System.out.println("Building craftbukkit-"+mcVersion+"...");
                        runProcess("java", "-jar", buildTools.getAbsolutePath(), "--rev", mcVersion, "--compile", "craftbukkit");
                    }
                    ArrayList<String> libraryNames = new ArrayList<>();
                    System.out.println("Extracting bukkit libraries");
                    for(File f : workRoot.listFiles()){
                        if(f.isFile()&&f.getName().startsWith("craftbukkit")&&f.getName().endsWith(".jar")){
                            File librariesTargetDir = new File(vanillifyRoot.getAbsoluteFile().getParentFile().getParentFile().getParentFile(), "Libraries/Bukkit/"+rVersionString);
                            librariesTargetDir.mkdirs();
                            var toF = new File(librariesTargetDir, f.getName());
                            if(!toF.exists())
                                Files.copy(f.toPath(), toF.toPath());//copy the whole jar file over

                            ZipFile zf = new ZipFile(f);
                            var entries = zf.entries();
                            while(entries.hasMoreElements()){
                                ZipEntry entry = entries.nextElement();
                                if(!entry.isDirectory()){
                                    String filename = entry.getName();
                                    if(filename.startsWith("META-INF/")&&filename.endsWith(".jar")){
                                        while(filename.contains("/"))
                                            filename = filename.substring(1);
                                        libraryNames.add(filename);
                                        File toZf = new File(librariesTargetDir, filename);
                                        if(!toZf.exists())
                                            Files.copy(zf.getInputStream(entry), toZf.toPath());
                                    }
                                }
                            }
                        }
                    }

                    rebuildProjectReferences = true;

                    System.out.println("Copying project template: "+templateVersion);
                    updateFolder(new File(vanillifyRoot, "VS"+templateVersion), projectRoot);

                    System.out.println("Cleaning template sources");
                    deleteFolder(new File(projectRoot, "src"));

                    System.out.println("Converting template to "+projectRoot.getName());
                    String[] projectFiles = new String[]{
                        "nbproject/project.properties",
                        "build.xml",
                        "nbproject/project.xml",
                        "nbproject/build-impl.xml"
                    };
                    for(String pf : projectFiles){
                        String str = Files.readString(new File(projectRoot, pf).toPath());
                        str = str.replace(templateVersion, rVersionString);
                        Files.writeString(new File(projectRoot, pf).toPath(), str);
                    }

                    System.out.println("Adding libraries...");
                    String projectProperties = Files.readString(new File(projectRoot, "nbproject/project.properties").toPath());
                    projectProperties = projectProperties.replace(templateVersion, rVersionString);
                    projectProperties = projectProperties.replaceAll("file\\.reference\\..+?=.+?\\.jar\\n", ""); // remove template libraries
                    projectProperties = projectProperties.replaceAll("javac\\.classpath=\\\\\\n.*?}(:\\\\\\n.*?})*", "");// remove template classpath
                    // add libraries
                    for(var lib : libraryNames){
                        projectProperties += "file.reference."+lib+"=../../Libraries/Bukkit/"+rVersionString+"/"+lib+"\n";
                    }
                    // add classpath
                    for(int i = 0; i<libraryNames.size(); i++){
                        String lib = libraryNames.get(i);
                        if(i==0)projectProperties += "javac.classpath=\\\n";
                        projectProperties += "    ${file.reference."+lib+"}";
                        if(i<libraryNames.size()-1)projectProperties += ":\\";
                        projectProperties += "\n";
                    }
                    Files.writeString(new File(projectRoot, "nbproject/project.properties").toPath(), projectProperties);

                    System.out.println("Loading vanillify sources for "+projectRoot.getName());

                    File templateVanillifySrc = new File(vanillifyRoot, "VS"+templateVersion+"/src/com/thizthizzydizzy/vanillify");
                    new File(projectVanillifySrc, "version").mkdirs();
                    Files.copy(new File(templateVanillifySrc, "version/Wrapper"+templateVersion+".java").toPath(), new File(projectVanillifySrc, "version/Wrapper"+rVersionString+".java").toPath());
                    var wrapper = Files.readString(new File(projectVanillifySrc, "version/Wrapper"+rVersionString+".java").toPath());
                    wrapper = wrapper.replace(templateVersion, rVersionString);
                    Files.writeString(new File(projectVanillifySrc, "version/Wrapper"+rVersionString+".java").toPath(), wrapper);

                    System.out.println("Loading AnvilGUI sources for "+projectRoot.getName());
                }
                projectNames.add(projectRoot.getName());
                updateFolder(anvilGUISrc, projectAnvilGUISrc);
                updateFolder(new File(workRoot, "AnvilGUI/abstraction/src/main/java/net/wesjd/anvilgui/version/VersionWrapper.java"), new File(projectAnvilGUISrc, "version/VersionWrapper.java"));
                updateFolder(new File(vanillifyRoot, "Vanillify/src/com/thizthizzydizzy/vanillify/version/VersionWrapper.java"), new File(projectVanillifySrc, "version/VersionWrapper.java"));
            }
        }
        System.out.println("Updating VersionMatcher");
        var anvilGUIVersionMatcher = new File(vanillifyRoot, "Vanillify/src/net/wesjd/anvilgui/version/VersionMatcher.java");
        updateFolder(new File(workRoot, "AnvilGUI/api/src/main/java/net/wesjd/anvilgui/version/VersionMatcher.java"), anvilGUIVersionMatcher);
        var versionMatcher = Files.readString(anvilGUIVersionMatcher.toPath());
        versionMatcher = versionMatcher.replace("net.wesjd.anvilgui", "com.thizthizzydizzy.vanillify");
        versionMatcher = versionMatcher.replace("AnvilGUI", "Vanillify");
        Files.writeString(new File(vanillifyRoot, "Vanillify/src/com/thizthizzydizzy/vanillify/version/VersionMatcher.java").toPath(), versionMatcher);
        
        if(rebuildProjectReferences){
            System.out.println("Rebuilding project references...");
            String buildImpl = Files.readString(new File(vanillifyRoot, "Vanillify/nbproject/build-impl.xml").toPath());
            buildImpl = buildImpl.replaceAll("<antcall target=\\\"-maybe-call-dep\\\">\\n.+\\{built-clean\\.properties\\}(.+\\n)+?.+</antcall>", "%%ANTCALL_CLEAN%%");
            buildImpl = buildImpl.replaceAll("<antcall target=\\\"-maybe-call-dep\\\">\\n.+\\{built-jar\\.properties\\}(.+\\n)+?.+</antcall>", "%%ANTCALL_JAR%%");
            String antcallClean = "";
            String antcallJar = "";
            for(var project : projectNames){
                antcallClean = antcallClean
                    +"        <antcall target=\"-maybe-call-dep\">\n"
                    +"            <param name=\"call.built.properties\" value=\"${built-clean.properties}\"/>\n"
                    +"            <param location=\"${project."+project+"}\" name=\"call.subproject\"/>\n"
                    +"            <param location=\"${project."+project+"}/build.xml\" name=\"call.script\"/>\n"
                    +"            <param name=\"call.target\" value=\"clean\"/>\n"
                    +"            <param name=\"transfer.built-clean.properties\" value=\"${built-clean.properties}\"/>\n"
                    +"            <param name=\"transfer.not.archive.disabled\" value=\"true\"/>\n"
                    +"            <param name=\"transfer.do.jlink\" value=\"false\"/>\n"
                    +"        </antcall>\n";
                antcallJar = antcallJar
                    +"        <antcall target=\"-maybe-call-dep\">\n"
                    +"            <param name=\"call.built.properties\" value=\"${built-jar.properties}\"/>\n"
                    +"            <param location=\"${project."+project+"}\" name=\"call.subproject\"/>\n"
                    +"            <param location=\"${project."+project+"}/build.xml\" name=\"call.script\"/>\n"
                    +"            <param name=\"call.target\" value=\"jar\"/>\n"
                    +"            <param name=\"transfer.built-jar.properties\" value=\"${built-jar.properties}\"/>\n"
                    +"            <param name=\"transfer.not.archive.disabled\" value=\"true\"/>\n"
                    +"            <param name=\"transfer.do.jlink\" value=\"false\"/>\n"
                    +"        </antcall>\n";
            }
            buildImpl = buildImpl.replaceFirst("%%ANTCALL_CLEAN%%", "%%ANTCALL_CLEAN1%%");
            buildImpl = buildImpl.replace("%%ANTCALL_CLEAN1%%", antcallClean);
            buildImpl = buildImpl.replace("%%ANTCALL_CLEAN%%", "");
            buildImpl = buildImpl.replaceFirst("%%ANTCALL_JAR%%", "%%ANTCALL_JAR1%%");
            buildImpl = buildImpl.replace("%%ANTCALL_JAR1%%", antcallJar);
            buildImpl = buildImpl.replace("%%ANTCALL_JAR%%", "");
            buildImpl = buildImpl.replaceAll("\\n\\s+\\n", "\n");
            Files.writeString(new File(vanillifyRoot, "Vanillify/nbproject/build-impl.xml").toPath(), buildImpl);

            String projectProperties = Files.readString(new File(vanillifyRoot, "Vanillify/nbproject/project.properties").toPath());
            projectProperties = projectProperties.replaceAll("reference\\.VS.+?=.+?\\.jar\\n", ""); // clear project references
            projectProperties = projectProperties.replaceAll("project\\.VS.+?=.+?\\n", ""); // clear projects
            projectProperties = projectProperties.replaceAll("javac\\.classpath=\\\\\\n.*?}(:\\\\\\n.*?})*", "");// clear classpath
            // add projects
            for(var project : projectNames){
                projectProperties += "project."+project+"=../"+project+"\n";
                projectProperties += "reference."+project+".jar=${project."+project+"}/dist/"+project+".jar\n";
            }
            // add classpath
            for(int i = 0; i<projectNames.size(); i++){
                String project = projectNames.get(i);
                if(i==0)projectProperties += "javac.classpath=\\\n"
                        +"    ${file.reference.craftbukkit-1.16.5.jar}:\\";
                projectProperties += "    ${reference."+project+"}";
                if(i<projectNames.size()-1)projectProperties += ":\\";
                projectProperties += "\n";
            }
            Files.writeString(new File(vanillifyRoot, "Vanillify/nbproject/project.properties").toPath(), projectProperties);

            String projectXML = Files.readString(new File(vanillifyRoot, "Vanillify/nbproject/project.xml").toPath());
            projectXML = projectXML.replaceAll("<reference>.*</reference>", "%%REFERENCES%%");
            String projectReferences = "";
            for(var project : projectNames){
                projectReferences = projectReferences
                    +"            <reference>\n"
                    +"                <foreign-project>"+project+"</foreign-project>\n"
                    +"                <artifact-type>jar</artifact-type>\n"
                    +"                <script>build.xml</script>\n"
                    +"                <target>jar</target>\n"
                    +"                <clean-target>clean</clean-target>\n"
                    +"                <id>jar</id>\n"
                    +"            </reference>\n";
            }
            projectXML = projectXML.replace("%%REFERENCES%%", projectReferences.trim());
            Files.writeString(new File(vanillifyRoot, "Vanillify/nbproject/project.xml").toPath(), projectXML);
        }
        System.out.println("Done!");
    }
    private static void deleteFolder(File file){
        if(file.isDirectory()){
            for(File f : file.listFiles())deleteFolder(f);
        }
        if(file.exists())file.delete();
    }
    private static void updateFolder(File from, File to) throws IOException{
        if(from.isDirectory()){
            for(File f : from.listFiles()){
                updateFolder(f, new File(to, f.getName()));
            }
        }
        if(from.isFile()){
            if(!to.exists()||(to.exists()&&!Files.readString(from.toPath()).equals(Files.readString(to.toPath())))){
                System.out.println((to.exists()?"Updating":"Copying")+" "+from.getPath()+" > "+to.getPath());
                if(to.exists())to.delete();
                to.getParentFile().mkdirs();
                Files.copy(from.toPath(), to.toPath());
            }
        }
    }
    private static void runProcess(String... command) throws IOException{
        System.out.println(String.join(" ", command));
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workRoot);
        Process p = pb.start();
        new Thread(() -> {
            try(BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))){
                String line;
                while((line = in.readLine())!=null){
                    System.out.println(line);
                }
            }catch(IOException ex){
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(ex.hashCode());
            }
        }, "Process output stream").start();
        new Thread(() -> {
            try(BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()))){
                String line;
                while((line = in.readLine())!=null){
                    System.out.println(line);
                }
            }catch(IOException ex){
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(ex.hashCode());
            }
        }, "Process error stream").start();
        while(p.isAlive()){
            try{
                Thread.sleep(100);
            }catch(InterruptedException ex){
                p.destroyForcibly();
                break;
            }
        }
    }
    public static File downloadFile(String link, File destinationFile){
        if(destinationFile.exists()||link==null){
            return destinationFile;
        }
        if(destinationFile.getParentFile()!=null)
            destinationFile.getParentFile().mkdirs();
        try{
            URL url = new URL(link);
            int fileSize;
            URLConnection connection = url.openConnection();
            connection.setDefaultUseCaches(false);
            if((connection instanceof HttpURLConnection)){
                ((HttpURLConnection)connection).setRequestMethod("HEAD");
                int code = ((HttpURLConnection)connection).getResponseCode();
                if(code/100==3){
                    return null;
                }
            }
            fileSize = connection.getContentLength();
            byte[] buffer = new byte[65535];
            URLConnection urlconnection = url.openConnection();
            if((urlconnection instanceof HttpURLConnection)){
                urlconnection.setRequestProperty("Cache-Control", "no-cache");
                urlconnection.connect();
            }
            String targetFile = destinationFile.getName();
            FileOutputStream fos;
            int downloadedFileSize;
            try(InputStream inputstream = getRemoteInputStream(targetFile, urlconnection)){
                fos = new FileOutputStream(destinationFile);
                downloadedFileSize = 0;
                int read;
                while((read = inputstream.read(buffer))!=-1){
                    fos.write(buffer, 0, read);
                    downloadedFileSize += read;
                }
            }
            fos.close();
            if(((urlconnection instanceof HttpURLConnection))
                &&((downloadedFileSize!=fileSize)&&(fileSize>0))){
                throw new Exception("failed to download "+targetFile);
            }
            return destinationFile;
        }catch(Exception ex){
            return null;
        }
    }
    public static InputStream getRemoteInputStream(String currentFile, final URLConnection urlconnection) throws Exception{
        final InputStream[] is = new InputStream[1];
        for(int j = 0; (j<3)&&(is[0]==null); j++){
            Thread t = new Thread(){
                public void run(){
                    try{
                        is[0] = urlconnection.getInputStream();
                    }catch(IOException localIOException){
                    }
                }
            };
            t.setName("FileDownloadStreamThread");
            t.start();
            int iterationCount = 0;
            while((is[0]==null)&&(iterationCount++<5)){
                try{
                    t.join(1000L);
                }catch(InterruptedException localInterruptedException){
                }
            }
            if(is[0]!=null){
                continue;
            }
            try{
                t.interrupt();
                t.join();
            }catch(InterruptedException localInterruptedException1){
            }
        }
        if(is[0]==null){
            throw new Exception("Unable to download "+currentFile);
        }
        return is[0];
    }
    public static InputStream getRemoteInputStream(String link){
        try{
            URL url = new URL(link);
            URLConnection connection = url.openConnection();
            connection.setDefaultUseCaches(false);
            if((connection instanceof HttpURLConnection)){
                ((HttpURLConnection)connection).setRequestMethod("HEAD");
                int code = ((HttpURLConnection)connection).getResponseCode();
                if(code/100==3){
                    return null;
                }
            }
            URLConnection urlconnection = url.openConnection();
            if((urlconnection instanceof HttpURLConnection)){
                urlconnection.setRequestProperty("Cache-Control", "no-cache");
                urlconnection.connect();
            }
            return getRemoteInputStream(null, urlconnection);
        }catch(Exception ex){
            return null;
        }
    }
}
