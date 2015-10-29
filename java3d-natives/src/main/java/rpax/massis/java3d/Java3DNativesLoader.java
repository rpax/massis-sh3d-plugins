package rpax.massis.java3d;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.nativelib.DefaultJniExtractor;

/**
 * Hello world!
 *
 */
public class Java3DNativesLoader {

    private static boolean nativesLoaded = false;

    public static void main(String[] args)
    {
        loadJava3DNatives();
    }

    public static synchronized void loadJava3DNatives()
    {
        try
        {
            if (Java3DNativesLoader.nativesLoaded)
            {
                return;
            }


            String path = "rpax/massis/java3d/java3d-natives";
            path += "/";
            path += getOSName();
            path += "/";
            path += "lib";
            path += "/";
            path += getOSArch() + "/";
            String libname = "j3dcore-ogl";
            Path libraryDir = Files.createTempDirectory(
                    Java3DNativesLoader.class.getName());
            libraryDir.toFile().deleteOnExit();
            String mappedlibName = System.mapLibraryName(libname);


            File natLibFile = new DefaultJniExtractor(
                    Java3DNativesLoader.class,
                    "tmplib").extractJni(path, libname);
            Files.copy(natLibFile.toPath(), libraryDir.resolve(
                    mappedlibName));


            addLibraryPath(libraryDir.toFile().getAbsolutePath());
            Java3DNativesLoader.nativesLoaded = true;
        } catch (Exception ex)
        {
            Logger.getLogger(Java3DNativesLoader.class.getName()).log(
                    Level.SEVERE,
                    null, ex);
        }
    }

    private static String getOSArch()
    {
        return System.getProperty("os.arch").toLowerCase().trim();
    }

    private static String getOSName()
    {
        String os_name = System.getProperty("os.name").toLowerCase();
        if (os_name.startsWith("linux"))
        {
            return "linux";
        }
        if (os_name.startsWith("windows"))
        {
            return "windows";
        }
        if (os_name.startsWith("mac os x"))
        {
            return "macosx";
        }
        if (os_name.indexOf("sunos") >= 0)
        {
            return "solaris";
        }
        throw new UnsupportedOperationException(
                "OS name not recognized: " + System.getProperty("os.name"));

    }

    /**
     * Adds the specified path to the java library path
     * http://fahdshariff.blogspot.com.es/2011/08/changing-java-library-path-at-runtime.html
     *
     * @param pathToAdd the path to add
     * @throws Exception
     */
    private static void addLibraryPath(String pathToAdd) throws Exception
    {
        final Field usrPathsField = ClassLoader.class.getDeclaredField(
                "usr_paths");
        usrPathsField.setAccessible(true);

        //get array of paths
        final String[] paths = (String[]) usrPathsField.get(null);

        //check if the path to add is already present
        for (String path : paths)
        {
            if (path.equals(pathToAdd))
            {
                return;
            }
        }

        //add the new path
        final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length - 1] = pathToAdd;
        usrPathsField.set(null, newPaths);
    }
}
