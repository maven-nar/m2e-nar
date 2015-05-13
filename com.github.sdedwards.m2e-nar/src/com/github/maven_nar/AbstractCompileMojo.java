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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.github.sdedwards.m2e_nar.internal.cdt.CdtUtils;

/**
 * @author Mark Donszelmann
 */
public abstract class AbstractCompileMojo
    extends AbstractDependencyMojo implements INarCompileMojo
{

    /**
     * C++ Compiler
     * 
     * @parameter default-value=""
     */
    private Cpp cpp;

    /**
     * C Compiler
     * 
     * @parameter default-value=""
     */
    private C c;

    /**
     * Fortran Compiler
     * 
     * @parameter default-value=""
     */
    private Fortran fortran;

    /**
     * Resource Compiler
     * 
     * @parameter default-value=""
     */
    private Resource resource;

    /**
     * IDL Compiler
     * 
     * @parameter default-value=""
     */
    private IDL idl;

    /**
     * Message Compiler
     * 
     * @parameter default-value=""
     */
    private Message message;

    /**
     * By default NAR compile will attempt to compile using all known compilers against files in the directories specified by convention.
     * This allows configuration to a reduced set, you will have to specify each compiler to use in the configuration.
     * 
     * @parameter default-value="false"
     */
    protected boolean onlySpecifiedCompilers;

    /**
     * Maximum number of Cores/CPU's to use. 0 means unlimited.
     * 
     * @parameter default-value=""
     */
    private int maxCores = 0;


    /**
     * Fail on compilation/linking error.
     * 
     * @parameter default-value="true"
     * @required
     */
    private boolean failOnError;

    /**
     * Sets the type of runtime library, possible values "dynamic", "static".
     * 
     * @parameter default-value="dynamic"
     * @required
     */
    private String runtime;

    /**
     * Set use of libtool. If set to true, the "libtool " will be prepended to the command line for compatible
     * processors.
     * 
     * @parameter default-value="false"
     * @required
     */
    private boolean libtool;

    /**
     * List of tests to create
     * 
     * @parameter default-value=""
     */
    private List tests;

    /**
     * Java info for includes and linking
     * 
     * @parameter default-value=""
     */
    private Java java;

    /**
     * Flag to cpptasks to indicate whether linker options should be decorated or not
     *
     * @parameter default-value=""
     */
    protected boolean decorateLinkerOptions;

    private NarInfo narInfo;

    private List/* <String> */dependencyLibOrder;

    public void setCpp(Cpp cpp) {
        this.cpp = cpp;
        cpp.setAbstractCompileMojo( this );
    }

    public void setC(C c) {
        this.c = c;
        c.setAbstractCompileMojo( this );
    }

    public void setFortran(Fortran fortran) {
        this.fortran = fortran;
        fortran.setAbstractCompileMojo( this );
    }

    public void setResource(Resource resource) {
        this.resource = resource;
        resource.setAbstractCompileMojo( this );
    }

    public void setIdl(IDL idl) {
        this.idl = idl;
        idl.setAbstractCompileMojo( this );
    }
    
    public void setMessage(Message message) {
        this.message = message;
        message.setAbstractCompileMojo( this );
    }
    
    public final C getC()
    {
        if ( c == null && !onlySpecifiedCompilers )
        {
            setC( new C() );
        }
        return c;
    }

    public final Cpp getCpp()
    {
        if ( cpp == null && !onlySpecifiedCompilers )
        {
            setCpp( new Cpp() );
        }
        return cpp;
    }

    public final Fortran getFortran()
    {
        if ( fortran == null && !onlySpecifiedCompilers )
        {
            setFortran( new Fortran() );
        }
        return fortran;
    }

    protected final Resource getResource( )
    {
		if ( resource == null && !onlySpecifiedCompilers )
        {
			setResource( new Resource() );
        }
        return resource;
    }
    
    protected final IDL getIdl( )
    {
		if ( idl == null && !onlySpecifiedCompilers )
        {
            setIdl( new IDL() );
        }
        return idl;
    }
    
    protected final Message getMessage( )
    {
		if ( message == null && !onlySpecifiedCompilers )
        {
			setMessage( new Message() );
        }
        return message;
    }

    protected final int getMaxCores( AOL aol )
        throws MojoExecutionException
    {
        return getNarInfo().getProperty( aol, "maxCores", maxCores );
    }

    protected final boolean useLibtool( AOL aol )
        throws MojoExecutionException
    {
        return getNarInfo().getProperty( aol, "libtool", libtool );
    }

    protected final boolean failOnError( AOL aol )
        throws MojoExecutionException
    {
        return getNarInfo().getProperty( aol, "failOnError", failOnError );
    }

    protected final String getRuntime( AOL aol )
        throws MojoExecutionException
    {
        return getNarInfo().getProperty( aol, "runtime", runtime );
    }

    protected final String getOutput( AOL aol, String type )
        throws MojoExecutionException
    {
        return getNarInfo().getOutput( aol, getOutput( !ILibrary.EXECUTABLE.equals( type ) ) );
    }

    public final List getTests()
    {
        if ( tests == null )
        {
            tests = Collections.EMPTY_LIST;
        }
        return tests;
    }

    protected final Java getJava()
    {
        if ( java == null )
        {
            java = new Java();
        }
        java.setAbstractCompileMojo( this );
        return java;
    }

    public final void setDependencyLibOrder( List/* <String> */order )
    {
        dependencyLibOrder = order;
    }

    protected final List/* <String> */getDependencyLibOrder()
    {
        return dependencyLibOrder;
    }

    protected final NarInfo getNarInfo()
        throws MojoExecutionException
    {
        if ( narInfo == null )
        {
            String groupId = getMavenProject().getGroupId();
            String artifactId = getMavenProject().getArtifactId();

            File propertiesDir = new File( getMavenProject().getBasedir(), "src/main/resources/META-INF/nar/" + groupId + "/" + artifactId );
            File propertiesFile = new File( propertiesDir, NarInfo.NAR_PROPERTIES );

            narInfo = new NarInfo(
                groupId, artifactId,
                getMavenProject().getVersion(),
                getLog(),
                propertiesFile );
        }
        return narInfo;
    }
    
    public Linker getLinker() {
    	Linker linker = super.getLinker();
    	linker.setAbstractCompileMojo(this);
    	return linker;
    }

	protected List/* <NarArtifact> */ getDependenciesToLink(String type) throws MojoExecutionException, MojoFailureException {
        List dependencies = new LinkedList();
		// FIXME: what about PLUGIN and STATIC, depending on STATIC, should we
        // not add all libraries, see NARPLUGIN-96
        if ( type.equals( ILibrary.SHARED ) || type.equals( ILibrary.JNI ) || type.equals( ILibrary.EXECUTABLE ) )
        {
	        List depLibOrder = getDependencyLibOrder();
	        List depLibs = getNarArtifacts();
	
	        // reorder the libraries that come from the nar dependencies
	        // to comply with the order specified by the user
	        if ( ( depLibOrder != null ) && !depLibOrder.isEmpty() )
	        {
	            List tmp = new LinkedList();
	
	            for ( Iterator i = depLibOrder.iterator(); i.hasNext(); )
	            {
	                String depToOrderName = (String) i.next();
	
	                for ( Iterator j = depLibs.iterator(); j.hasNext(); )
	                {
	                    NarArtifact dep = (NarArtifact) j.next();
	                    String depName = dep.getGroupId() + ":" + dep.getArtifactId();
	
	                    if (depName.equals(depToOrderName)) 
	                    {
	                        tmp.add(dep);
	                        j.remove();
	                    }
	                }
	            }
	
	            tmp.addAll(depLibs);
	            depLibs = tmp;
	        }
            for ( Iterator i = depLibs.iterator(); i.hasNext(); )
            {
                NarArtifact dependency = (NarArtifact) i.next();

                // FIXME no handling of "local"

                // FIXME, no way to override this at this stage
                String binding = dependency.getNarInfo().getBinding( getAOL(), ILibrary.NONE );
                getLog().debug("Using Binding: " + binding);

                if ( !binding.equals( ILibrary.JNI ) && !binding.equals( ILibrary.NONE ) && !binding.equals( ILibrary.EXECUTABLE) )
                {
                	dependencies.add(dependency);
                }
            }
        }
        return dependencies;
	}
	
	protected File getLibraryPath(NarArtifact dependency) throws MojoFailureException, MojoExecutionException {
        String binding = dependency.getNarInfo().getBinding( getAOL(), ILibrary.NONE );
        AOL aol = getAOL();
        aol = dependency.getNarInfo().getAOL(getAOL());
        getLog().debug("Using Library AOL: " + aol.toString());
    
        File unpackDirectory = getUnpackDirectory(dependency);
        NarLayout layout = dependency.getNarLayout();

        File dir =
        		layout.getLibDirectory( unpackDirectory, dependency, aol.toString(), binding );
        return dir;
	}
    
    /* (non-Javadoc)
	 * @see com.github.maven_nar.INarCompileMojo#getJavahIncludePaths()
	 */
    public List/* <String> */ getJavahIncludePaths() {
    	List includePaths = new ArrayList();
    	boolean isJNI = false;
    	for (Library library : libraries) {
    		if (Library.JNI.equals(library.getType())) {
    			isJNI = true;
    		}
    	}
        if (isJNI)
        {
            // add javah include path
            File jniDirectory = getJavah().getJniDirectory();
            includePaths.add(jniDirectory.getPath());
        }
        return includePaths;
    }
    
    /* (non-Javadoc)
	 * @see com.github.maven_nar.INarCompileMojo#getJavaIncludePaths()
	 */
    public List/* <String> */ getJavaIncludePaths() throws MojoExecutionException, MojoFailureException {
    	for (Library library : libraries) {
    		if (Library.JNI.equals(library.getType())) {
    			getJava().setInclude(true);
    		}
    	}
        return getJava().getIncludePaths();
    }
    
    /* (non-Javadoc)
	 * @see com.github.maven_nar.INarCompileMojo#getDependencyIncludePaths()
	 */
    public List/* <File> */ getDependencyIncludePaths() throws MojoExecutionException, MojoFailureException {
    	List includePaths = new ArrayList();
        // add dependency include paths
        for ( Iterator i = getNarArtifacts().iterator(); i.hasNext(); )
        {
            // FIXME, handle multiple includes from one NAR
            NarArtifact narDependency = (NarArtifact) i.next();
            String binding = narDependency.getNarInfo().getBinding(getAOL(), ILibrary.STATIC);
            getLog().debug( "Looking for " + narDependency + " found binding " + binding);
            if ( !binding.equals(ILibrary.JNI ) )
            {
                File unpackDirectory = getUnpackDirectory(narDependency);
                NarLayout layout = narDependency.getNarLayout();
                List<File> includes =
                		layout.getIncludeDirectories( unpackDirectory, narDependency );
                includePaths.addAll(includes);
            }
        }
        return includePaths;
    }
    
    /* (non-Javadoc)
	 * @see com.github.maven_nar.INarCompileMojo#getDependencyLibs(java.lang.String)
	 */
    public List/* <Lib> */ getDependencyLibs(final String type, final ITest test) throws MojoExecutionException, MojoFailureException {
    	List libraries = new ArrayList();
    	if (test != null) {
    		// Add the library of this package if it exists
    		final String linkType = test.getLink();
    		getLog().debug("Test: " + test.getName() + ", link: " + linkType);
    		boolean found = false;
    		for (Library lib : getLibraries()) {
    			if (linkType.equals(lib.getType())) {
    				found = true;
    				break;
    			}
    		}
    		if (found) {
        		getLog().debug("Adding " + linkType + " library for test " + test.getName());
	    		final File dir = new File(getMavenProject().getBasedir(), CdtUtils.DEFAULT_CONFIG_NAME_PREFIX + test.getLink());
	    		final Lib library = new Lib();
	    		library.setName(getMavenProject().getArtifactId());
	    		library.setDirectory(dir);
	    		library.setType(linkType);
	    		libraries.add(library);
    		}
    	}
        for ( Iterator i = getDependenciesToLink(type).iterator(); i.hasNext(); )
        {
            NarArtifact dependency = (NarArtifact) i.next();

            // FIXME, no way to override
            String binding = dependency.getNarInfo().getBinding( getAOL(), ILibrary.NONE );
            String libs = dependency.getNarInfo().getLibs(getAOL());
            if ( ( libs != null ) && !libs.equals( "" ) )
            {
            	File dir = getLibraryPath(dependency);
            	String[] libArray = new NarUtil.StringArrayBuilder(libs).getValue();
            	for (int j = 0; j < libArray.length; ++j) {
            		Lib library = new Lib();
            		library.setName(libArray[j]);
            		library.setDirectory(dir);
            		library.setType(binding);
            		libraries.add(library);
            	}
            }
        }
        return libraries;
    }

    /* (non-Javadoc)
	 * @see com.github.maven_nar.INarCompileMojo#getDependencySysLibs(java.lang.String)
	 */
    public List/* <SysLib> */ getDependencySysLibs(final String type) throws MojoExecutionException, MojoFailureException {
    	List libraries = new ArrayList();
        for ( Iterator i = getDependenciesToLink(type).iterator(); i.hasNext(); )
        {
            NarArtifact dependency = (NarArtifact) i.next();

            String sysLibs = dependency.getNarInfo().getSysLibs( getAOL() );
            if ( ( sysLibs != null ) && !sysLibs.equals( "" ) )
            {
            	String[] sysLibArray = new NarUtil.StringArrayBuilder(sysLibs).getValue();
            	for (int j = 0; j < sysLibArray.length; ++j) {
            		SysLib library = new SysLib();
            		library.setName(sysLibArray[j]);
            		library.setType(null);
            		libraries.add(library);
            	}
            }
        }
        return libraries;
    }

    /* (non-Javadoc)
	 * @see com.github.maven_nar.INarCompileMojo#getDependencyOptions(java.lang.String)
	 */
    public List/* <String> */ getDependencyOptions(final String type) throws MojoExecutionException, MojoFailureException {
    	List optionList = new ArrayList();
        for ( Iterator i = getDependenciesToLink(type).iterator(); i.hasNext(); )
        {
            NarArtifact dependency = (NarArtifact) i.next();

            String options = dependency.getNarInfo().getOptions( getAOL() );
            if ( ( options != null ) && !options.equals( "" ) )
            {
            	optionList.add(options);
            }
        }
        return optionList;
    }
    
    public String getOutput(final String type) throws MojoFailureException, MojoExecutionException  {
    	return getOutput(getAOL(), type);
    }
}
