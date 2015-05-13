/*
 * #%L
 * Maven Integration for Eclipse CDT
 * %%
 * Copyright (C) 2014 Stephen Edwards
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
 */
package com.github.maven_nar;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

import com.github.sdedwards.m2e_nar.internal.cdt.CdtUtils;

public class EclipseNarLayout extends AbstractNarLayout {
	private List<File> includeDirectories = new ArrayList<File>();
	private Map<String, String> artifactNames = new LinkedHashMap<String, String>();
	private IMavenProjectFacade project;

	public EclipseNarLayout(final Log log) {
		super(log);
	}

	public void setProject(final IMavenProjectFacade project) {
		this.project = project;
	}

	public IMavenProjectFacade getProject() {
		return project;
	}

	public void addIncludeDirectory(File includeDirectory) {
		if (!includeDirectory.isAbsolute()) {
			includeDirectory = new File(project.getProject().getLocation()
					.toFile(), includeDirectory.getPath());
		}
		this.includeDirectories.add(includeDirectory);
	}

	public void addArtifactName(final String configuration, final String artifactName) {
		this.artifactNames.put(configuration, artifactName);
	}

	public File getNoArchDirectory(File baseDir, NarArtifact artifact) {
		return null;
	}

    public File getNoArchDirectory( File baseDir, MavenProject project )
    {
        return null;
    }

    private File getTargetDirectory(String type) {
		return new File(project.getProject().getLocation().toFile(), CdtUtils.DEFAULT_CONFIG_NAME_PREFIX + type);
	}

	public final List<File> getIncludeDirectories(File baseDir,
			NarArtifact artifact) {
		return includeDirectories;
	}

    /*
     * (non-Javadoc)
     * @see com.github.maven_nar.NarLayout#getIncludeDirectory(java.io.File)
     */
    public final File getIncludeDirectory( File baseDir, MavenProject project )
    {
    	return null;
    }

	public final File getLibDirectory(File baseDir, NarArtifact artifact, String aol, String type)
			throws MojoExecutionException {
		return getTargetDirectory(type);
	}

    /*
     * (non-Javadoc)
     * @see com.github.maven_nar.NarLayout#getLibDir(java.io.File, com.github.maven_nar.AOL,
     * java.lang.String)
     */
    public final File getLibDirectory( File baseDir, MavenProject project, String aol, String type )
        throws MojoExecutionException
    {
    	return getTargetDirectory(type);
    }
    
	public final File getBinDirectory(File baseDir, NarArtifact artifact, String aol) {
		return getTargetDirectory(ILibrary.EXECUTABLE);
	}

    /*
     * (non-Javadoc)
     * @see com.github.maven_nar.NarLayout#getLibDir(java.io.File, com.github.maven_nar.AOL,
     * java.lang.String)
     */
    public final File getBinDirectory( File baseDir, MavenProject project, String aol )
    {
    	return getTargetDirectory(ILibrary.EXECUTABLE);
    }
    
    /*
	public final void prepareNarInfo(File baseDir, MavenProject project,
			NarInfo narInfo, AbstractNarMojo mojo)
			throws MojoExecutionException {
	}

	public File getNarUnpackDirectory(File baseUnpackDirectory, File narFile) {
		return null;
	}
	*/

	public List<String> getConfigurations() {
		return new ArrayList<String>(artifactNames.keySet());
	}
	
	public String getArtifactName(final String configuration) {
		return artifactNames.get(configuration);
	}
}
