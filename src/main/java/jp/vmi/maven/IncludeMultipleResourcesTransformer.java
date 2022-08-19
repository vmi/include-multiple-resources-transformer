package jp.vmi.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.resource.ResourceTransformer;
import org.codehaus.plexus.util.DirectoryWalkListener;
import org.codehaus.plexus.util.DirectoryWalker;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncludeMultipleResourcesTransformer implements ResourceTransformer {

    private final Logger logger = LoggerFactory.getLogger(IncludeMultipleResourcesTransformer.class);

    File resourceDirectory;
    List<String> includes;
    List<String> excludes;
    String resourceBase;

    private List<Path> paths = null;

    @Override
    public boolean canTransformResource(String resource) {
        return false;
    }

    @Override
    @Deprecated
    public void processResource(String resource, InputStream is, List<Relocator> relocators) throws IOException {
        // no operation.
    }

    private void scanResources() {
        if (paths != null)
            return;
        if (resourceDirectory == null)
            throw new IllegalStateException("resourceDirectory is empty");
        DirectoryWalker s = new DirectoryWalker();
        s.setBaseDir(resourceDirectory);
        if (includes != null)
            s.setIncludes(includes);
        if (excludes != null)
            s.setExcludes(excludes);
        s.addSCMExcludes();
        s.addDirectoryWalkListener(new DirectoryWalkListener() {

            @Override
            public void directoryWalkStep(int percentage, File file) {
                paths.add(file.toPath());
            }

            @Override
            public void directoryWalkStarting(File basedir) {
                paths = new ArrayList<>();
            }

            @Override
            public void directoryWalkFinished() {
                // no operation.
            }

            @Override
            public void debug(String message) {
                logger.debug(message);
            }
        });
        s.scan();
    }

    @Override
    public boolean hasTransformedResource() {
        scanResources();
        return !paths.isEmpty();
    }

    @Override
    public void modifyOutputStream(JarOutputStream jos) throws IOException {
        Path rDir = resourceDirectory.toPath();
        String rBase;
        if (resourceBase == null)
            rBase = "";
        else
            rBase = resourceBase.replaceFirst("/*$", "/").replaceFirst("^/+", "");
        logger.info("Include multiple resources:");
        for (Path path : paths) {
            String rName = rBase + rDir.relativize(path).toString();
            logger.info("Adding {}", rName);
            jos.putNextEntry(new JarEntry(rName));
            try (InputStream in = Files.newInputStream(path)) {
                IOUtil.copy(in, jos);
            }
        }
        logger.info("Done.");
    }
}
