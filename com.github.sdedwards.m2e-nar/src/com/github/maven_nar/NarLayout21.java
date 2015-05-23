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
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Layout which expands a nar file into:
 * 
 * <pre>
 * nar/noarch/include
 * nar/aol/<aol>-<type>/bin
 * nar/aol/<aol>-<type>/lib
 * </pre>
 * 
 * This loayout has a one-to-one relation with the aol-type version of the nar.
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class NarLayout21 extends AbstractNarLayout {
	private NarFileLayout fileLayout;

	public NarLayout21(Log log) {
		super(log);
		this.fileLayout = new NarFileLayout10();
	}

	public File getNoArchDirectory(File baseDir, NarArtifact artifact) {
		return new File(baseDir, artifact.getArtifactId() + "-" + artifact.getBaseVersion() + "-" + NarConstants.NAR_NO_ARCH);
	}

	public File getNoArchDirectory(File baseDir, MavenProject project) {
		return new File(baseDir, project.getArtifactId() + "-" + project.getVersion() + "-" + NarConstants.NAR_NO_ARCH);
	}

	private File getAolDirectory(File baseDir, NarArtifact artifact, String aol, String type) {
		return new File(baseDir, artifact.getArtifactId() + "-" + artifact.getBaseVersion() + "-" + aol + "-" + type);
	}

	private File getAolDirectory(File baseDir, MavenProject project, String aol, String type) {
		return new File(baseDir, project.getArtifactId() + "-" + project.getVersion() + "-" + aol + "-" + type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.github.maven_nar.NarLayout#getIncludeDirectory(java.io.File)
	 */
	public final List<File> getIncludeDirectories(File baseDir, NarArtifact artifact) {
		ArrayList<File> includes = new ArrayList<File>();
		includes.add(new File(getNoArchDirectory(baseDir, artifact), fileLayout.getIncludeDirectory()));
		return includes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.github.maven_nar.NarLayout#getIncludeDirectory(java.io.File)
	 */
	public final File getIncludeDirectory(File baseDir, MavenProject project) {
		return new File(getNoArchDirectory(baseDir, project), fileLayout.getIncludeDirectory());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.github.maven_nar.NarLayout#getLibDir(java.io.File,
	 * com.github.maven_nar.AOL, java.lang.String)
	 */
	public final File getLibDirectory(File baseDir, NarArtifact artifact, String aol, String type) throws MojoExecutionException {
		if (type.equals(Library.EXECUTABLE)) {
			throw new MojoExecutionException("NAR: for type EXECUTABLE call getBinDirectory instead of getLibDirectory");
		}

		File dir = getAolDirectory(baseDir, artifact, aol, type);
		dir = new File(dir, fileLayout.getLibDirectory(aol, type));
		return dir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.github.maven_nar.NarLayout#getLibDir(java.io.File,
	 * com.github.maven_nar.AOL, java.lang.String)
	 */
	public final File getLibDirectory(File baseDir, MavenProject project, String aol, String type) throws MojoExecutionException {
		if (type.equals(Library.EXECUTABLE)) {
			throw new MojoExecutionException("NAR: for type EXECUTABLE call getBinDirectory instead of getLibDirectory");
		}

		File dir = getAolDirectory(baseDir, project, aol, type);
		dir = new File(dir, fileLayout.getLibDirectory(aol, type));
		return dir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.github.maven_nar.NarLayout#getLibDir(java.io.File,
	 * com.github.maven_nar.AOL, java.lang.String)
	 */
	public final File getBinDirectory(File baseDir, NarArtifact artifact, String aol) {
		File dir = getAolDirectory(baseDir, artifact, aol, Library.EXECUTABLE);
		dir = new File(dir, fileLayout.getBinDirectory(aol));
		return dir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.github.maven_nar.NarLayout#getLibDir(java.io.File,
	 * com.github.maven_nar.AOL, java.lang.String)
	 */
	public final File getBinDirectory(File baseDir, MavenProject project, String aol) {
		File dir = getAolDirectory(baseDir, project, aol, Library.EXECUTABLE);
		dir = new File(dir, fileLayout.getBinDirectory(aol));
		return dir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.github.maven_nar.NarLayout#attachNars(java.io.File,
	 * org.apache.maven.project.MavenProjectHelper,
	 * org.apache.maven.project.MavenProject, com.github.maven_nar.NarInfo)
	 */
	/*
	 * public final void prepareNarInfo( File baseDir, MavenProject project,
	 * NarInfo narInfo, AbstractNarMojo mojo ) throws MojoExecutionException {
	 * if ( getNoArchDirectory( baseDir, project ).exists() ) { narInfo.setNar(
	 * null, NarConstants.NAR_NO_ARCH, project.getGroupId() + ":" +
	 * project.getArtifactId() + ":" + NarConstants.NAR_TYPE + ":" +
	 * NarConstants.NAR_NO_ARCH ); }
	 * 
	 * String artifactIdVersion = project.getArtifactId() + "-" +
	 * project.getVersion(); // list all directories in basedir, scan them for
	 * classifiers String[] subDirs = baseDir.list(); ArrayList<String>
	 * classifiers = new ArrayList<String>(); for ( int i = 0; ( subDirs != null
	 * ) && ( i < subDirs.length ); i++ ) { // skip entries not belonging to
	 * this project if ( !subDirs[i].startsWith( artifactIdVersion ) ) continue;
	 * 
	 * String classifier = subDirs[i].substring( artifactIdVersion.length() + 1
	 * );
	 * 
	 * // skip noarch here if ( classifier.equals( NarConstants.NAR_NO_ARCH ) )
	 * continue;
	 * 
	 * classifiers.add(classifier); }
	 * 
	 * if( !classifiers.isEmpty() ){
	 * 
	 * for(String classifier : classifiers ){ int lastDash =
	 * classifier.lastIndexOf( '-' ); String type = classifier.substring(
	 * lastDash + 1 ); AOL aol = new AOL( classifier.substring( 0, lastDash ) );
	 * 
	 * if ( ( narInfo.getOutput( aol, null ) == null ) ) { narInfo.setOutput(
	 * aol, mojo.getOutput(! aol.getOS().contains( OS.WINDOWS ) && !
	 * type.equals( Library.EXECUTABLE ) ) ); }
	 * 
	 * // We prefer shared to jni/executable/static/none, if ( type.equals(
	 * Library.SHARED ) ) // overwrite whatever we had { narInfo.setBinding(
	 * aol, type ); narInfo.setBinding( null, type ); } else { // if the binding
	 * is already set, then don't write it for jni/executable/static/none. if (
	 * ( narInfo.getBinding( aol, null ) == null ) ) { narInfo.setBinding( aol,
	 * type ); } if ( ( narInfo.getBinding( null, null ) == null ) ) {
	 * narInfo.setBinding( null, type ); } }
	 * 
	 * narInfo.setNar( null, type, project.getGroupId() + ":" +
	 * project.getArtifactId() + ":" + NarConstants.NAR_TYPE + ":" + "${aol}" +
	 * "-" + type );
	 * 
	 * }
	 * 
	 * // setting this first stops the per type config because getOutput check
	 * for aol defaults to this generic one... if ( mojo!= null && (
	 * narInfo.getOutput( null, null ) == null ) ) { narInfo.setOutput( null,
	 * mojo.getOutput(true) ); } } }
	 * 
	 * public File getNarUnpackDirectory(File baseUnpackDirectory,
	 * AttachedNarArtifact artifact) { File dir = new File( baseUnpackDirectory,
	 * artifact
	 * .getArtifactId()+"-"+artifact.getBaseVersion()+"-"+artifact.getClassifier
	 * () ); return dir; }
	 */
}
