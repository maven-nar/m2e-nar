/*
 * #%L
 * Native ARchive plugin for Maven
 * %%
 * Copyright (C) 2002 - 2014 NAR Maven Plugin developers.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 * 
 * 2014/09/18 Modified by Stephen Edwards:
 *  Make a public API for extracting NAR config
 */
package com.github.maven_nar;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * @author Mark Donszelmann
 */
public abstract class AbstractNarMojo
    extends AbstractMojo
    implements NarConstants
{

  /**
   * Skip running of NAR plugins (any) altogether.
   */
  @Parameter(property = "nar.skip", defaultValue = "false")
  protected boolean skip;

  /**
   * Skip the tests. Listens to Maven's general 'maven.skip.test'.
   */
  @Parameter(property = "maven.test.skip")
  boolean skipTests;

  /**
   * Ignore errors and failures.
   */
  @Parameter(property = "nar.ignore", defaultValue = "false")
  private boolean ignore;

  /**
   * The Architecture for the nar, Some choices are: "x86", "i386", "amd64",
   * "ppc", "sparc", ... Defaults to a derived
   * value from ${os.arch}
   */
  @Parameter(property = "nar.arch")
  private String architecture;

  /**
   * The Operating System for the nar. Some choices are: "Windows", "Linux",
   * "MacOSX", "SunOS","AIX" ... Defaults to a
   * derived value from ${os.name} FIXME table missing
   */
  @Parameter(property = "nar.os")
  private String os;

  /**
   * Architecture-OS-Linker name. Defaults to: arch-os-linker.
   */
  @Parameter(defaultValue = "")
  private String aol;

    /**
     * Additional classifier suffix. Defaults to: ""
     *
     * @parameter default-value=""
     */
    private String aolSuffix;

  /**
   * Linker
   */
  @Parameter
  private Linker linker;

  // these could be obtained from an injected project model.

  @Parameter(property = "project.build.directory", readonly = true)
  private File outputDirectory;

  @Parameter(property = "project.build.outputDirectory", readonly = true)
  protected File classesDirectory;

  /**
   * Name of the output
   * - for jni default-value="${project.artifactId}-${project.version}"
   * - for libs default-value="${project.artifactId}-${project.version}"
   * - for exe default-value="${project.artifactId}"
   * -- for tests default-value="${test.name}"
   * 
   */
  @Parameter
  private String output;

  @Parameter(property = "project.basedir", readonly = true)
  private File baseDir;

  /**
   * Target directory for Nar file construction. Defaults to
   * "${project.build.directory}/nar" for "nar-compile" goal
   */
  @Parameter
  private File targetDirectory;

  /**
   * Target directory for Nar test construction. Defaults to
   * "${project.build.directory}/test-nar" for "nar-testCompile" goal
   */
  @Parameter
  private File testTargetDirectory;

  /**
   * Target directory for Nar file unpacking. Defaults to "${targetDirectory}"
   */
  @Parameter
  private File unpackDirectory;

  /**
   * Target directory for Nar test unpacking. Defaults to
   * "${testTargetDirectory}"
   */
  @Parameter
  private File testUnpackDirectory;

    /**
     * NARVersionInfo for Windows binaries
     *
     */
    //@Parameter
    //private NARVersionInfo versionInfo;

  /**
   * List of classifiers which you want download/unpack/assemble
   * Example ppc-MacOSX-g++, x86-Windows-msvc, i386-Linux-g++.
   * Not setting means all.
   */
  @Parameter
  protected List<String> classifiers;

  /**
   * List of libraries to create
   */
  @Parameter
  protected List<Library> libraries;

  /**
   * Name of the libraries included
   */
  @Parameter
  private String libsName;

  /**
   * Layout to be used for building and unpacking artifacts
   */
  @Parameter(property = "nar.layout", defaultValue = "com.github.maven_nar.NarLayout21", required = true)
  private String layout;
    
    private NarLayout narLayout;

    /**
     * @parameter property="project"
     * @readonly
     * @required
     */
    private MavenProject mavenProject;

    private AOL aolId;

	private NarInfo narInfo;

	/**
	 * Javah info
	 * 
	 * @parameter default-value=""
	 */
	private Javah javah;

	/**
	 * The home of the Java system. Defaults to a derived value from ${java.home} which is OS specific.
	 * 
	 * @parameter default-value=""
	 * @readonly
	 */
	private File javaHome;

    /**
     * Force the default binding, e.g. shared or static
     *
     * @parameter
     */
    private String defaultBinding;

    private NarProperties narProperties = null;

    public final void validate()
        throws MojoFailureException, MojoExecutionException
    {
    	if (narProperties == null)
    	{
    		narProperties = new NarProperties(mavenProject, getClass());
    	}
        linker = NarUtil.getLinker( linker, getLog() );

        architecture = NarUtil.getArchitecture( architecture );
        os = NarUtil.getOS( os );
        aolId = NarUtil.getAOL(narProperties, architecture, os, linker, aol, aolSuffix, getLog() );
        
        Model model = mavenProject.getModel();
        Properties properties = model.getProperties();
        properties.setProperty("nar.arch", getArchitecture());
        properties.setProperty("nar.os", getOS());
        properties.setProperty("nar.linker", getLinker().getName());
        properties.setProperty("nar.aol", aolId.toString());
        properties.setProperty("nar.aol.key", aolId.getKey());
        model.setProperties(properties);

        if ( targetDirectory == null )
        {
            targetDirectory = new File( mavenProject.getBuild().getDirectory(), "nar" );
        }
        if ( testTargetDirectory == null )
        {
            testTargetDirectory = new File( mavenProject.getBuild().getDirectory(), "test-nar" );
        }

        if ( unpackDirectory == null )
        {
            unpackDirectory = targetDirectory;
        }
        if ( testUnpackDirectory == null )
        {
            testUnpackDirectory = testTargetDirectory;
        }
    }

    protected final String getOutput( boolean versioned )
    	    throws MojoExecutionException
	{
	    if( output != null && !output.trim().isEmpty()){
	    	return output; 
	    } else {
	    	if( versioned )
	    		return getMavenProject().getArtifactId() + "-" + getMavenProject().getVersion();
	    	else 
	    		return getMavenProject().getArtifactId();
	    }
	}
    
    protected final String getArchitecture()
    {
        return architecture;
    }

    public final String getOS()
    {
        return os;
    }

    protected final AOL getAOL()
        throws MojoFailureException, MojoExecutionException
    {
        return aolId;
    }

    protected final String getAOLSuffix()
    {
        return aolSuffix;
    }

    public Linker getLinker()
    {
        return linker;
    }
    
    protected final File getBasedir()
    {
    	return baseDir;
    }

    protected final File getOutputDirectory()
    {
        return outputDirectory;
    }

    protected final File getTargetDirectory()
    {
        return targetDirectory;
    }
    protected final File getTestTargetDirectory()
    {
        return testTargetDirectory;
    }

    protected File getUnpackDirectory()
    {
        return unpackDirectory;
    }

    protected final File getTestUnpackDirectory()
    {
        return testUnpackDirectory;
    }

    protected final NarLayout getLayout()
        throws MojoExecutionException
    {
        if ( narLayout == null )
        {
            narLayout =
                AbstractNarLayout.getLayout( layout, getLog() );
        }
        return narLayout;
    }

    protected final MavenProject getMavenProject()
    {
        return mavenProject;
    }

    public final void execute()
        throws MojoExecutionException, MojoFailureException
    {
    	// This Mojo should never actually be executed
    	// it exists just for reading the config in the pom
        getLog().info( getClass().getName() + " skipped" );
    }

	protected NarInfo getNarInfo() throws MojoExecutionException {
	    if ( narInfo == null )
	    {
	    	String groupId = getMavenProject().getGroupId();
	    	String artifactId = getMavenProject().getArtifactId();
            String path = "META-INF/nar/" + groupId + "/" + artifactId + "/" + NarInfo.NAR_PROPERTIES;
            File propertiesFile = null;
            if (classesDirectory != null) {
                propertiesFile = new File( classesDirectory, path );
            }
            // should not need to try and read from source.
            if( propertiesFile == null || !propertiesFile.exists() ){
                propertiesFile = new File( getMavenProject().getBasedir(), "src/main/resources/" + path);
            }
	
	        narInfo = new NarInfo( 
	            groupId, artifactId,
	            getMavenProject().getVersion(), 
	            getLog(),
	            propertiesFile );
	    }
	    return narInfo;
	}

	public final List<Library> getLibraries() {
	    if ( libraries == null )
	    {
	        libraries = Collections.EMPTY_LIST;
	    }
	    return libraries;
	}

	protected final Javah getJavah() {
	    if ( javah == null )
	    {
	        javah = new Javah();
	    }
	    javah.setAbstractCompileMojo( this );
	    return javah;
	}

	protected final File getJavaHome(AOL aol)
			throws MojoExecutionException {
			    // FIXME should be easier by specifying default...
			    return getNarInfo().getProperty( aol, "javaHome", NarUtil.getJavaHome( javaHome, getOS() ) );
			}
	
	public final boolean isSkip() {
		return skip;
	}

	public NarProperties getNarProperties() {
		return narProperties;
	}

	public void setNarProperties(NarProperties narProperties) {
		this.narProperties = narProperties;
	}
	
	protected File getUnpackDirectory(final NarArtifact dependency) {
		File unpackDirectory = getUnpackDirectory();
		if (Artifact.SCOPE_TEST.equals(dependency.getScope())) {
			if (getTestUnpackDirectory() != null) {
				unpackDirectory = getTestUnpackDirectory();
			}
		}
		return unpackDirectory;
	}
}
