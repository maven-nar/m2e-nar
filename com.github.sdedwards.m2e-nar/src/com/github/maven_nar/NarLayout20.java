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
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Initial layout which expands a nar file into:
 *
 * <pre>
 * nar/includue
 * nar/bin
 * nar/lib
 * </pre>
 *
 * this layout was abandoned because there is no one-to-one relation between the nar file and its directory structure.
 * Therefore SNAPSHOTS could not be fully deleted when replaced.
 *
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class NarLayout20
    extends AbstractNarLayout
{
    private NarFileLayout fileLayout;

    public NarLayout20( Log log )
    {
        super( log );
        this.fileLayout = new NarFileLayout10();
    }

    /*
     * (non-Javadoc)
     * @see com.github.maven_nar.NarLayout#getNoArchDirectory(java.io.File)
     */
    public File getNoArchDirectory( File baseDir, NarArtifact artifact )
        throws MojoExecutionException, MojoFailureException
    {
        return baseDir;
    }

    public File getNoArchDirectory( File baseDir, MavenProject project )
            throws MojoExecutionException, MojoFailureException
    {
        return baseDir;
    }

    /*
     * (non-Javadoc)
     * @see com.github.maven_nar.NarLayout#getIncludeDirectory(java.io.File)
     */
    public final List<File> getIncludeDirectories( File baseDir, NarArtifact artifact )
    {
    	ArrayList<File> includes = new ArrayList<File>();
        includes.add( new File( baseDir, fileLayout.getIncludeDirectory() ) );
        return includes;
    }

    public final File getIncludeDirectory( File baseDir, MavenProject project )
    {
        return new File( baseDir, fileLayout.getIncludeDirectory() );
    }

    /*
     * (non-Javadoc)
     * @see com.github.maven_nar.NarLayout#getLibDir(java.io.File, com.github.maven_nar.AOL, String type)
     */
    public final File getLibDirectory( File baseDir, NarArtifact artifact, String aol, String type )
        throws MojoFailureException
    {
        if ( type.equals( Library.EXECUTABLE ) )
        {
            throw new MojoFailureException( "INTERNAL ERROR, Replace call to getLibDirectory with getBinDirectory" );
        }

        File dir = new File( baseDir, fileLayout.getLibDirectory( aol, type ) );
        return dir;
    }

    /*
     * (non-Javadoc)
     * @see com.github.maven_nar.NarLayout#getLibDir(java.io.File, com.github.maven_nar.AOL, String type)
     */
    public final File getLibDirectory( File baseDir, MavenProject project, String aol, String type )
        throws MojoFailureException
    {
        if ( type.equals( Library.EXECUTABLE ) )
        {
            throw new MojoFailureException( "INTERNAL ERROR, Replace call to getLibDirectory with getBinDirectory" );
        }

        File dir = new File( baseDir, fileLayout.getLibDirectory( aol, type ) );
        return dir;
    }

    /*
     * (non-Javadoc)
     * @see com.github.maven_nar.NarLayout#getBinDirectory(java.io.File, java.lang.String)
     */
    public final File getBinDirectory( File baseDir, NarArtifact artifact, String aol )
    {
        File dir = new File( baseDir, fileLayout.getBinDirectory( aol ) );
        return dir;
    }

    /*
     * (non-Javadoc)
     * @see com.github.maven_nar.NarLayout#getBinDirectory(java.io.File, java.lang.String)
     */
    public final File getBinDirectory( File baseDir, MavenProject project, String aol )
    {
        File dir = new File( baseDir, fileLayout.getBinDirectory( aol ) );
        return dir;
    }

    /*
     * (non-Javadoc)
     * @see com.github.maven_nar.NarLayout#attachNars(java.io.File, org.apache.maven.project.MavenProjectHelper,
     * org.apache.maven.project.MavenProject, com.github.maven_nar.NarInfo)
     */
    /*
    public final void prepareNarInfo( File baseDir, MavenProject project, NarInfo narInfo, AbstractNarMojo mojo )
        throws MojoExecutionException
    {
        if ( getIncludeDirectory( baseDir, project ).exists() )
        {
            narInfo.setNar( null, "noarch", project.getGroupId() + ":" + project.getArtifactId() + ":"
                + NarConstants.NAR_TYPE + ":" + "noarch" );
        }

        String[] binAOL = new File( baseDir, "bin" ).list();
        for ( int i = 0; ( binAOL != null ) && ( i < binAOL.length ); i++ )
        {// TODO: chose not to apply new file naming for outfile in case of backwards compatability,  may need to reconsider
            narInfo.setNar( null, Library.EXECUTABLE, project.getGroupId() + ":" + project.getArtifactId() + ":"
                + NarConstants.NAR_TYPE + ":" + "${aol}" + "-" + Library.EXECUTABLE );
            narInfo.setBinding( new AOL( binAOL[i] ), Library.EXECUTABLE );
            narInfo.setBinding( null, Library.EXECUTABLE );
        }

        File libDir = new File( baseDir, "lib" );
        String[] libAOL = libDir.list();
        for ( int i = 0; ( libAOL != null ) && ( i < libAOL.length ); i++ )
        {
            String bindingType = null;
            String[] libType = new File( libDir, libAOL[i] ).list();
            for ( int j = 0; ( libType != null ) && ( j < libType.length ); j++ )
            {
                narInfo.setNar( null, libType[j], project.getGroupId() + ":" + project.getArtifactId() + ":"
                    + NarConstants.NAR_TYPE + ":" + "${aol}" + "-" + libType[j] );

                // set if not set or override if SHARED
                if ( ( bindingType == null ) || libType[j].equals( Library.SHARED ) )
                {
                    bindingType = libType[j];
                }
            }

            AOL aol = new AOL( libAOL[i] );
            if ( narInfo.getBinding( aol, null ) == null )
            {
                narInfo.setBinding( aol, bindingType != null ? bindingType : Library.NONE );
            }
            if ( narInfo.getBinding( null, null ) == null )
            {
                narInfo.setBinding( null, bindingType != null ? bindingType : Library.NONE );
            }
        }
    }

    public File getNarUnpackDirectory(File baseUnpackDirectory, AttachedNarArtifact artifact )
    {
        return baseUnpackDirectory;
    }
    */
}
