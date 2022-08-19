package jp.vmi.maven;

import java.io.IOException;
import java.util.Objects;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.shade.ShadeRequest;
import org.apache.maven.plugins.shade.Shader;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = Shader.class, hint = "custom")
public class IncludeMultipleResourcesShader implements Shader {

    @SuppressWarnings("unused")
    private final Logger logger;

    public IncludeMultipleResourcesShader() {
        this(LoggerFactory.getLogger(IncludeMultipleResourcesShader.class));
    }

    public IncludeMultipleResourcesShader(Logger logger) {
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    public void shade(ShadeRequest shadeRequest) throws IOException, MojoExecutionException {
        shadeRequest.getResourceTransformers().add(new IncludeMultipleResourcesTransformer());
    }
}
