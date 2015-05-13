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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

import org.apache.maven.plugin.logging.Log;

/**
 * Linker tag
 * 
 * @author Mark Donszelmann
 */
public class Linker implements ILinker
{

    /**
     * The Linker Some choices are: "msvc", "g++", "CC", "icpc", ... Default is Architecture-OS-Linker specific: FIXME:
     * table missing
     * 
     * @parameter default-value=""
     */
    private String name;

    /**
     * Path location of the linker tool
     *
     * @parameter default-value=""
     */
    private String toolPath;

    /**
     * Enables or disables incremental linking.
     * 
     * @parameter default-value="false"
     * @required
     */
    private boolean incremental = false;

    /**
     * Enables or disables the production of a map file.
     * 
     * @parameter default-value="false"
     * @required
     */
    private boolean map = false;

    /**
     * Options for the linker Defaults to Architecture-OS-Linker specific values. FIXME table missing
     * 
     * @parameter default-value=""
     */
    private List options;

    /**
     * Options for the linker as a whitespace separated list. Defaults to Architecture-OS-Linker specific values. Will
     * work in combination with &lt;options&gt;.
     * 
     * @parameter default-value=""
     */
    private String optionSet;

    /**
     * Clears default options
     * 
     * @parameter default-value="false"
     * @required
     */
    private boolean clearDefaultOptions;

    /**
     * Adds libraries to the linker.
     * 
     * @parameter default-value=""
     */
    private List/* <Lib> */libs;

    /**
     * Adds libraries to the linker. Will work in combination with &lt;libs&gt;. The format is comma separated,
     * colon-delimited values (name:type:dir), like "myLib:shared:/home/me/libs/, otherLib:static:/some/path".
     * 
     * @parameter default-value=""
     */
    private String libSet;

    /**
     * Adds system libraries to the linker.
     * 
     * @parameter default-value=""
     */
    private List/* <SysLib> */sysLibs;

    /**
     * Adds system libraries to the linker. Will work in combination with &lt;sysLibs&gt;. The format is comma
     * separated, colon-delimited values (name:type), like "dl:shared, pthread:shared".
     * 
     * @parameter default-value=""
     */
    private String sysLibSet;

    /**
     * <p>
     * Specifies the link ordering of libraries that come from nar dependencies. The format is a comma separated list of
     * dependency names, given as groupId:artifactId.
     * </p>
     * <p>
     * Example: &lt;narDependencyLibOrder&gt;someGroup:myProduct, other.group:productB&lt;narDependencyLibOrder&gt;
     * </p>
     * 
     * @parameter default-value=""
     */
    private String narDependencyLibOrder;

    private final Log log;

    private AbstractCompileMojo mojo;

    public Linker()
    {
        // default constructor for use as TAG
        this( null );
    }

    public Linker( final Log log )
    {
        this.log = log;
    }

    /**
     * For use with specific named linker.
     * 
     * @param name
     */
    public Linker( String name, final Log log )
    {
        this.name = name;
        this.log = log;
    }

    public final void setAbstractCompileMojo( AbstractCompileMojo mojo )
    {
        this.mojo = mojo;
    }

    /* (non-Javadoc)
	 * @see com.github.maven_nar.ILinker#getName()
	 */
    public final String getName()
    {
        return name;
    }

    public final String getName( NarProperties properties, String prefix )
        throws MojoFailureException, MojoExecutionException
    {
        if ( ( name == null ) && ( properties != null ) && ( prefix != null ) )
        {
            name = properties.getProperty( prefix + "linker" );
        }
        if ( name == null )
        {
            throw new MojoExecutionException( "NAR: One of two things may be wrong here:\n\n"
                + "1. <Name> tag is missing inside the <Linker> tag of your NAR configuration\n\n"
                + "2. no linker is defined in the aol.properties file for '" + prefix + "linker'\n" );
        }
        return name;
    }

    public final String getVersion() 
        throws MojoFailureException, MojoExecutionException
    {
        if ( name == null )
        {
            throw new MojoFailureException( "Cannot deduce linker version if name is null" );
        }

        String version = null;

        TextStream out = new StringTextStream();
        TextStream err = new StringTextStream();
        TextStream dbg = new StringTextStream();

        if ( name.equals( "g++" ) || name.equals( "gcc" ) )
        {
            NarUtil.runCommand( "gcc", new String[] { "--version" }, null, null, out, err, dbg, log );
            Pattern p = Pattern.compile( "\\d+\\.\\d+\\.\\d+" );
            Matcher m = p.matcher( out.toString() );
            if ( m.find() )
            {
                version = m.group( 0 );
            }
        }
        else if ( name.equals( "msvc" ) )
        {
            NarUtil.runCommand( "link", new String[] { "/?" }, null, null, out, err, dbg, log, true );
            Pattern p = Pattern.compile( "\\d+\\.\\d+\\.\\d+(\\.\\d+)?" );
            Matcher m = p.matcher( out.toString() );
            if ( m.find() )
            {
                version = m.group( 0 );
            }
        }
        else if ( name.equals( "icc" ) || name.equals( "icpc" ) )
        {
            NarUtil.runCommand( "icc", new String[] { "--version" }, null, null, out, err, dbg, log );
            Pattern p = Pattern.compile( "\\d+\\.\\d+" );
            Matcher m = p.matcher( out.toString() );
            if ( m.find() )
            {
                version = m.group( 0 );
            }
        }
        else if ( name.equals( "icl" ) )
        {
            NarUtil.runCommand( "icl", new String[] { "/QV" }, null, null, out, err, dbg, log );
            Pattern p = Pattern.compile( "\\d+\\.\\d+" );
            Matcher m = p.matcher( err.toString() );
            if ( m.find() )
            {
                version = m.group( 0 );
            }
        }
        else if ( name.equals( "CC" ) )
        {
        	NarUtil.runCommand( "CC", new String[] { "-V" }, null, null, out, err, dbg, log );
        	Pattern p = Pattern.compile( "\\d+\\.d+" );
        	Matcher m = p.matcher( err.toString() );
        	if ( m.find() )
        	{ 
        		version = m.group( 0 ); 
        	}
        }
        else
        {
            throw new MojoFailureException( "Cannot find version number for linker '" + name + "'" );
        }
        
        if (version == null) {
        	throw new MojoFailureException( "Cannot deduce version number from: " + out.toString() );
        }
        return version;
    }

    protected final String getPrefix()
            throws MojoFailureException, MojoExecutionException
        {
            return mojo.getAOL().getKey() + ".linker.";
        }

    /* (non-Javadoc)
	 * @see com.github.maven_nar.ILinker#isIncremental()
	 */
    public boolean isIncremental() {
		return incremental;
	}

	/* (non-Javadoc)
	 * @see com.github.maven_nar.ILinker#isMap()
	 */
	public boolean isMap() {
		return map;
	}

	/* (non-Javadoc)
	 * @see com.github.maven_nar.ILinker#getOptions()
	 */
	public List getOptions() throws MojoFailureException, MojoExecutionException {
		List optionList = new ArrayList();
        if ( options != null )
        {
            optionList.addAll(options);
        }

        if ( optionSet != null )
        {

            String[] opts = optionSet.split( "\\s" );

            for ( int i = 0; i < opts.length; i++ )
            {

                optionList.add( opts[i] );
            }
        }

        if ( !clearDefaultOptions )
        {
            String option = mojo.getNarProperties().getProperty( getPrefix() + "options" );
            if ( option != null )
            {
                String[] opt = option.split( " " );
                for ( int i = 0; i < opt.length; i++ )
                {
                    optionList.add( opt[i] );
                }
            }
        }
        return optionList;
	}
	
    private List buildLibList( String libraryList )
    {
    	List libList = new ArrayList();
        if ( libraryList == null )
        {
            return libList;
        }

        String[] lib = libraryList.split( "," );

        for ( int i = 0; i < lib.length; i++ )
        {

            String[] libInfo = lib[i].trim().split( ":", 3 );

            String[] libNames = new NarUtil.StringArrayBuilder( libInfo[0] ).getValue();
            for ( int j = 0; j < libNames.length; ++j) {
                Lib library = new Lib();
                library.setName( libNames[j] );
                library.setType(null);
                if ( libInfo.length > 1 )
                {
                    library.setType( libInfo[1] );
                    if ( libInfo.length > 2 )
                    {
                        library.setDirectory( new File( libInfo[2] ) );
                    }
                }
                libList.add(library);
            }
        }
        return libList;
    }

    private List buildSysLibList( String libraryList )
    {
    	List libList = new ArrayList();
        if ( libraryList == null )
        {
            return libList;
        }

        String[] lib = libraryList.split( "," );

        for ( int i = 0; i < lib.length; i++ )
        {

            String[] libInfo = lib[i].trim().split( ":", 3 );

            String[] libNames = new NarUtil.StringArrayBuilder( libInfo[0] ).getValue();
            for ( int j = 0; j < libNames.length; ++j) {
                SysLib library = new SysLib();
                library.setName( libNames[j] );
                library.setType(null);
                if ( libInfo.length > 1 )
                {
                    library.setType( libInfo[1] );
                }
                libList.add(library);
            }
        }
        return libList;
    }
    
    /* (non-Javadoc)
	 * @see com.github.maven_nar.ILinker#getLibs()
	 */
    public List getLibs() throws MojoFailureException, MojoExecutionException {
        List fullLibList = new ArrayList();
        if ( ( libs != null ) || ( libSet != null ) )
        {

            if ( libs != null )
            {
            	fullLibList.addAll(libs);
            }

            if ( libSet != null )
            {
                fullLibList.addAll( buildLibList( libSet ) );
            }
        }
        else
        {
            String libsList = mojo.getNarProperties().getProperty( getPrefix() + "libs" );
            fullLibList.addAll( buildLibList( libsList ) );
        }
        return fullLibList;
    }
    
    /* (non-Javadoc)
	 * @see com.github.maven_nar.ILinker#getSysLibs()
	 */
    public List getSysLibs() throws MojoFailureException, MojoExecutionException {
        List fullSysLibList = new ArrayList();
        if ( ( sysLibs != null ) || ( sysLibSet != null ) )
        {

            if ( sysLibs != null )
            {
            	fullSysLibList.addAll(sysLibs);
            }

            if ( sysLibSet != null )
            {
                fullSysLibList.addAll( buildSysLibList( sysLibSet ) );
            }
        }
        else
        {
            String sysLibsList = mojo.getNarProperties().getProperty( getPrefix() + "sysLibs" );
            fullSysLibList.addAll( buildSysLibList( sysLibsList ) );
        }
        return fullSysLibList;
    }
}
