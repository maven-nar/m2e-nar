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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;

import com.github.sdedwards.m2e_nar.internal.ConfiguratorContext;
import com.github.sdedwards.m2e_nar.internal.MavenUtils;
import com.github.sdedwards.m2e_nar.internal.model.NarExecution;

/**
 * @author Mark Donszelmann
 */
public abstract class AbstractDependencyMojo extends AbstractNarMojo {

	/**
	 * @parameter default-value="${localRepository}"
	 * @required
	 * @readonly
	 */
	private ArtifactRepository localRepository;

	/**
	 * Artifact resolver, needed to download the attached nar files.
	 * 
	 * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
	 * @required
	 * @readonly
	 */
	protected ArtifactResolver artifactResolver;

	/**
	 * Remote repositories which will be searched for nar attachments.
	 * 
	 * @parameter default-value="${project.remoteArtifactRepositories}"
	 * @required
	 * @readonly
	 */
	protected List<ArtifactRepository> remoteArtifactRepositories;

	/**
	 * The plugin remote repositories declared in the pom.
	 * 
	 * @parameter default-value="${project.pluginArtifactRepositories}"
	 * @since 2.2
	 */
	// private List remotePluginRepositories;

	protected List<NarArtifact> narDependencies = null;

	protected final ArtifactRepository getLocalRepository() {
		return localRepository;
	}

	protected final List<ArtifactRepository> getRemoteRepositories() {
		return remoteArtifactRepositories;
	}

	protected abstract List<Artifact> getArtifacts();

	/**
	 * Returns dependencies which are dependent on NAR files (i.e. contain
	 * NarInfo)
	 */
	public final List<NarArtifact> getNarArtifacts() {
		return narDependencies;
	}

	private final NarInfo getNarInfo(Artifact dependency) throws MojoExecutionException {

		File file = dependency.getFile();
		if (!file.exists()) {
			getLog().debug("Dependency nar file does not exist: " + file);
			return null;
		}
		// If it's a directory then it must be available as an Eclipse project
		if (file.isDirectory()) {
			getLog().debug("Dependency nar file is a directory: " + file);
			return null;
		}

		try {
			NarInfo info = new NarInfo(dependency.getGroupId(), dependency.getArtifactId(), dependency.getBaseVersion(), getLog());
			if (!info.exists(file)) {
				getLog().debug("Dependency nar file does not contain this artifact: " + file);
				return null;
			}
			info.read(file);
			return info;
		} catch (IOException e) {
			throw new MojoExecutionException("Error while reading " + file, e);
		}
	}

	private final NarInfo getNarInfo(Artifact dependency, EclipseNarLayout layout) throws MojoExecutionException, MojoFailureException {
		NarInfo narInfo = new NarInfo(dependency.getGroupId(), dependency.getArtifactId(), dependency.getBaseVersion(), getLog());
		AOL aol = getAOL();
		for (String type : layout.getConfigurations()) {

			if ((narInfo.getOutput(aol, null) == null)) {
				narInfo.setOutput(aol, layout.getArtifactName(type));
			}

			// We prefer shared to jni/executable/static/none,
			if (type.equals(ILibrary.SHARED)) // overwrite whatever we had
			{
				narInfo.setBinding(aol, type);
				narInfo.setBinding(null, type);
			} else {
				// if the binding is already set, then don't write it for
				// jni/executable/static/none.
				if ((narInfo.getBinding(aol, null) == null)) {
					narInfo.setBinding(aol, type);
				}
				if ((narInfo.getBinding(null, null) == null)) {
					narInfo.setBinding(null, type);
				}
			}

		}

		// setting this first stops the per type config because getOutput check
		// for aol defaults to this generic one...
		if (narInfo.getOutput(null, null) == null) {
			narInfo.setOutput(null, dependency.getArtifactId());
		}
		return narInfo;
	}

	public final List<AttachedNarArtifact> getAllAttachedNarArtifacts(List<NarArtifact> narArtifacts/*
																									 * ,
																									 * Library
																									 * library
																									 */) throws MojoExecutionException, MojoFailureException {
		List<AttachedNarArtifact> artifactList = new ArrayList<AttachedNarArtifact>();
		for (Iterator<NarArtifact> i = narArtifacts.iterator(); i.hasNext();) {
			NarArtifact dependency = i.next();

			// Skip dependencies that are already available
			// eg sibling sub-module dependencies in compile only builds
			if (dependency.getNarInfo() == null) {
				continue;
			}

			String binding = getBinding(/* library, */dependency);

			// TODO: dependency.getFile(); find out what the stored pom says
			// about this - what nars should exist, what layout are they
			// using...
			artifactList.addAll(getAttachedNarArtifacts(dependency, /* library. */
					getAOL(), binding));
			artifactList.addAll(getAttachedNarArtifacts(dependency, null, NarConstants.NAR_NO_ARCH));
		}
		return artifactList;
	}

	protected String getBinding(/* Library library, */NarArtifact dependency) throws MojoFailureException, MojoExecutionException {
		// how does this project specify the dependency is used
		// - library.getLinker().getLibs();
		// - if it is specified but the artifact is not available should fail.
		// otherwise how does the artifact specify it should be used by default
		//
		// - what is the preference for this type of library to use (shared -
		// shared, static - static...)

		// library.getType()
		String binding = dependency.getNarInfo().getBinding(
		/* library. */getAOL(), /* type != null ? type : */
		ILibrary.STATIC);
		return binding;
	}

	public File getArtifactDirectory(NarArtifact dependency, File unpackDirectory) {
		File targetDirectory = dependency.getNarInfo().getTargetDirectory();
		if (targetDirectory != null) {
			return targetDirectory;
		} else {
			return unpackDirectory;
		}
	}

	private List<AttachedNarArtifact> getAttachedNarArtifacts(NarArtifact dependency, AOL aol, String type) throws MojoExecutionException, MojoFailureException {
		getLog().debug("GetNarDependencies for " + dependency + ", aol: " + aol + ", type: " + type);
		List<AttachedNarArtifact> artifactList = new ArrayList<AttachedNarArtifact>();
		NarInfo narInfo = dependency.getNarInfo();
		String[] nars = narInfo.getAttachedNars(aol, type);
		// FIXME Move this to NarInfo....
		if (nars != null) {
			for (int j = 0; j < nars.length; j++) {
				getLog().debug("    Checking: " + nars[j]);
				if (nars[j].equals("")) {
					continue;
				}
				String[] nar = nars[j].split(":", 5);
				if (nar.length >= 4) {
					try {
						String groupId = nar[0].trim();
						String artifactId = nar[1].trim();
						String ext = nar[2].trim();
						String classifier = nar[3].trim();
						// translate for instance g++ to gcc...
						AOL aolString = narInfo.getAOL(aol);
						if (aolString != null) {
							classifier = NarUtil.replace("${aol}", aolString.toString(), classifier);
						}
						String version = nar.length >= 5 ? nar[4].trim() : dependency.getVersion();
						artifactList.add(new AttachedNarArtifact(groupId, artifactId, version, dependency.getScope(), ext, classifier, dependency.isOptional(),
								dependency.getFile()));
					} catch (InvalidVersionSpecificationException e) {
						throw new MojoExecutionException("Error while reading nar file for dependency " + dependency, e);
					}
				} else {
					getLog().warn("nars property in " + dependency.getArtifactId() + " contains invalid field: '" + nars[j]
					// + "' for type: " + type
					);
				}
			}
		}
		return artifactList;
	}

	@SuppressWarnings("deprecation")
	public final void downloadAttachedNars(List<AttachedNarArtifact> dependencies) throws MojoExecutionException, MojoFailureException {
		getLog().debug("Download for NarDependencies {");
		for (Iterator<AttachedNarArtifact> i = dependencies.iterator(); i.hasNext();) {
			getLog().debug("  - " + (i.next()));
		}
		getLog().debug("}");

		for (Iterator<AttachedNarArtifact> i = dependencies.iterator(); i.hasNext();) {
			Artifact dependency = (Artifact) i.next();
			try {
				getLog().debug("Resolving " + dependency);
				artifactResolver.resolve(dependency, remoteArtifactRepositories, getLocalRepository());
			} catch (ArtifactNotFoundException e) {
				String message = "nar not found " + dependency.getId();
				throw new MojoExecutionException(message, e);
			} catch (ArtifactResolutionException e) {
				String message = "nar cannot resolve " + dependency.getId();
				throw new MojoExecutionException(message, e);
			}
		}
	}

	public void prepareNarArtifacts(final ConfiguratorContext context, IMavenProjectFacade facade, IProgressMonitor monitor) throws MojoExecutionException,
			CoreException, MojoFailureException {
		narDependencies = new LinkedList<NarArtifact>();
		for (Iterator<Artifact> i = getArtifacts().iterator(); i.hasNext();) {
			Artifact dependency = i.next();
			getLog().debug("Examining artifact for NarInfo: " + dependency);

			NarLayout layout;
			EclipseNarLayout eclipseLayout = resolveEclipseProject(dependency, context, facade, monitor);
			NarInfo narInfo;
			if (eclipseLayout != null) {
				layout = eclipseLayout;
				narInfo = getNarInfo(dependency, eclipseLayout);
			} else {
				layout = getLayout();
				narInfo = getNarInfo(dependency);
			}
			if (narInfo != null) {
				getLog().debug("    - added as NarDependency");
				narDependencies.add(new NarArtifact(dependency, narInfo, layout));
			}
		}
		getLog().debug("Dependencies contained " + narDependencies.size() + " NAR artifacts.");
	}

	private EclipseNarLayout resolveEclipseProject(final Artifact artifact, final ConfiguratorContext context, IMavenProjectFacade facade,
			IProgressMonitor monitor) throws CoreException {
		final IMavenProjectRegistry projectManager = context.getProjectManager();
		if (!Artifact.SCOPE_COMPILE.equals(artifact.getScope()) && !Artifact.SCOPE_TEST.equals(artifact.getScope())) {
			return null;
		}
		IMavenProjectFacade dependency = projectManager.getMavenProject(artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion());
		if (dependency == null || dependency.getFullPath(artifact.getFile()) == null) {
			return null;
		}
		if (dependency.getProject().equals(facade.getProject())) {
			return null;
		}
		getLog().debug("Found dependency project " + dependency.getProject().getName());
		EclipseNarLayout layout = new EclipseNarLayout(getLog());
		layout.setProject(dependency);
		List<NarExecution> narExecutions = MavenUtils.buildCompileNarExecutions(context, dependency, monitor);
		getLog().debug("Found " + narExecutions.size() + " compile executions");
		com.github.sdedwards.m2e_nar.internal.model.NarBuildArtifact artifactSettings = null;
		for (NarExecution narSettings : narExecutions) {
			for (com.github.sdedwards.m2e_nar.internal.model.NarBuildArtifact settings : narSettings.getArtifactSettings()) {
				layout.addArtifactName(settings.getType(), settings.getArtifactName());
				artifactSettings = settings;
			}
		}
		if (artifactSettings != null) {
			for (String includePath : artifactSettings.getCSettings().getIncludePaths()) {
				layout.addIncludeDirectory(new File(includePath));
			}
			for (String includePath : artifactSettings.getCppSettings().getIncludePaths()) {
				layout.addIncludeDirectory(new File(includePath));
			}
			return layout;
		}
		return null;
	}
}
