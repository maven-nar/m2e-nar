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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.ArtifactIdFilter;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.apache.maven.shared.artifact.filter.collection.GroupIdFilter;
import org.apache.maven.shared.artifact.filter.collection.ScopeFilter;
import org.codehaus.plexus.util.StringUtils;
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

  @Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
  private ArtifactRepository localRepository;

  /**
   * Artifact resolver, needed to download the attached nar files.
   */
  @Component(role = org.apache.maven.artifact.resolver.ArtifactResolver.class)
  protected ArtifactResolver artifactResolver;

  /**
   * Remote repositories which will be searched for nar attachments.
   */
  @Parameter(defaultValue = "${project.remoteArtifactRepositories}", required = true, readonly = true)
  protected List remoteArtifactRepositories;

  /**
   * Comma separated list of Artifact names to exclude.
   * 
   * @since 2.0
   */
  @Parameter(property = "excludeArtifactIds", defaultValue = "")
  protected String excludeArtifactIds;

  /**
   * Comma separated list of Artifact names to include.
   * 
   * @since 2.0
   */
  @Parameter(property = "includeArtifactIds", defaultValue = "")
  protected String includeArtifactIds;

  /**
   * Comma separated list of GroupId Names to exclude.
   * 
   * @since 2.0
   */
  @Parameter(property = "excludeGroupIds", defaultValue = "")
  protected String excludeGroupIds;

  /**
   * Comma separated list of GroupIds to include.
   * 
   * @since 2.0
   */
  @Parameter(property = "includeGroupIds", defaultValue = "")
  protected String includeGroupIds;

	protected List<NarArtifact> narDependencies = null;

	protected final ArtifactRepository getLocalRepository() {
		return localRepository;
	}

	protected final List<ArtifactRepository> getRemoteRepositories() {
		return remoteArtifactRepositories;
	}

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

  public final List<AttachedNarArtifact> getAllAttachedNarArtifacts(final List<NarArtifact> narArtifacts,
      List<? extends Executable> libraries) throws MojoExecutionException, MojoFailureException {
    final List<AttachedNarArtifact> artifactList = new ArrayList<AttachedNarArtifact>();
    for (NarArtifact dependency : narArtifacts) {
      // Skip dependencies that are already available
      // eg sibling sub-module dependencies in compile only builds
      if (dependency.getNarInfo() == null) {
        continue;
      }
      if ("NAR".equalsIgnoreCase(getMavenProject().getPackaging())) {
        final String bindings[] = getBindings(libraries, dependency);

        // TODO: dependency.getFile(); find out what the stored pom says
        // about this - what nars should exist, what layout are they
        // using...
        for (final String binding : bindings) {
          artifactList.addAll(getAttachedNarArtifacts(dependency, /* library. */
              getAOL(), binding));
        }
      } else {
        artifactList.addAll(getAttachedNarArtifacts(dependency, getAOL(), Library.EXECUTABLE));
        artifactList.addAll(getAttachedNarArtifacts(dependency, getAOL(), Library.SHARED));
        artifactList.addAll(getAttachedNarArtifacts(dependency, getAOL(), Library.JNI));
        artifactList.addAll(getAttachedNarArtifacts(dependency, getAOL(), Library.STATIC));
      }
      artifactList.addAll(getAttachedNarArtifacts(dependency, null, NarConstants.NAR_NO_ARCH));
    }
    return artifactList;
  }

  protected String[] getBindings(List<? extends Executable> libraries, NarArtifact dependency)
      throws MojoFailureException, MojoExecutionException {

    Set<String> bindings = new HashSet<String>();
    if (libraries != null){
      for (Object library : libraries) {
        Executable exec = (Executable) library;
        // how does this project specify the dependency is used
        String binding = exec.getBinding(dependency);
        if( null != binding )
          bindings.add(binding);
      }
    }

    // - if it is specified but the atrifact is not available should fail.
    // otherwise
    // how does the artifact specify it should be used by default
    // -
    // whats the preference for this type of library to use (shared - shared,
    // static - static...)

    // library.getType()
    if (bindings.isEmpty())
      bindings.add(dependency.getNarInfo().getBinding(getAOL(), Library.STATIC));

    return bindings.toArray(new String[1]);
  }

  protected String getBinding(Executable exec, NarArtifact dependency)
      throws MojoFailureException, MojoExecutionException {

    // how does this project specify the dependency is used
    String binding = exec.getBinding(dependency);

    // - if it is specified but the atrifact is not available should fail.
    // otherwise
    // how does the artifact specify it should be used by default
    // -
    // whats the preference for this type of library to use (shared - shared,
    // static - static...)

    // library.getType()
    if (binding == null)
      binding = dependency.getNarInfo().getBinding(getAOL(), Library.STATIC);

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

  /**
   * Returns the artifacts which must be taken in account for the Mojo.
   * 
   * @return Artifacts
   */
  protected abstract ScopeFilter getArtifactScopeFilter();

  /**
   * Returns the attached NAR Artifacts (AOL and noarch artifacts) from the NAR
   * dependencies artifacts of the project.
   * The artifacts which will be processed are those returned by the method
   * getArtifacts() which must be implemented
   * in each class which extends AbstractDependencyMojo.
   * 
   * @return Attached NAR Artifacts
   * @throws MojoFailureException
   * @throws MojoExecutionException
   * 
   * @see getArtifacts
   */
  protected List<AttachedNarArtifact> getAttachedNarArtifacts(List<? extends Executable> libraries)
      throws MojoFailureException, MojoExecutionException {
    getLog().info("Getting Nar dependencies");
    final List<NarArtifact> narArtifacts = getNarArtifacts();
    final List<AttachedNarArtifact> attachedNarArtifacts = getAllAttachedNarArtifacts(narArtifacts, libraries);
    return attachedNarArtifacts;
  }

  private List<AttachedNarArtifact> getAttachedNarArtifacts(final NarArtifact dependency, final AOL aol,
      final String type) throws MojoExecutionException, MojoFailureException {
    getLog().debug("GetNarDependencies for " + dependency + ", aol: " + aol + ", type: " + type);
    final List<AttachedNarArtifact> artifactList = new ArrayList<AttachedNarArtifact>();
    final NarInfo narInfo = dependency.getNarInfo();
    final String[] nars = narInfo.getAttachedNars(aol, type);
    // FIXME Move this to NarInfo....
    if (nars != null) {
      for (final String nar2 : nars) {
        getLog().debug("    Checking: " + nar2);
        if (nar2.equals("")) {
          continue;
        }
        final String[] nar = nar2.split(":", 5);
        if (nar.length >= 4) {
          try {
            final String groupId = nar[0].trim();
            final String artifactId = nar[1].trim();
            final String ext = nar[2].trim();
            String classifier = nar[3].trim();
            // translate for instance g++ to gcc...
            final AOL aolString = narInfo.getAOL(aol);
            if (aolString != null) {
              classifier = NarUtil.replace("${aol}", aolString.toString(), classifier);
            }
            final String version = nar.length >= 5 ? nar[4].trim() : dependency.getVersion();
            artifactList.add(new AttachedNarArtifact(groupId, artifactId, version, dependency.getScope(), ext,
                classifier, dependency.isOptional(), dependency.getFile()));
          } catch (final InvalidVersionSpecificationException e) {
            throw new MojoExecutionException("Error while reading nar file for dependency " + dependency, e);
          }
        } else {
          getLog().warn("nars property in " + dependency.getArtifactId() + " contains invalid field: '" + nar2);
        }
      }
    }
    return artifactList;
  }
  
  @SuppressWarnings("deprecation")
  public final void downloadAttachedNars(final List<AttachedNarArtifact> dependencies)
      throws MojoExecutionException, MojoFailureException {
    getLog().debug("Download for NarDependencies {");
    for (final AttachedNarArtifact attachedNarArtifact : dependencies) {
      getLog().debug("  - " + attachedNarArtifact);
    }
    getLog().debug("}");

    for (final AttachedNarArtifact attachedNarArtifact : dependencies) {
      try {
        getLog().debug("Resolving " + attachedNarArtifact);
        this.artifactResolver.resolve(attachedNarArtifact, this.remoteArtifactRepositories, getLocalRepository());
      } catch (final ArtifactNotFoundException e) {
        final String message = "nar not found " + attachedNarArtifact.getId();
        throw new MojoExecutionException(message, e);
      } catch (final ArtifactResolutionException e) {
        final String message = "nar cannot resolve " + attachedNarArtifact.getId();
        throw new MojoExecutionException(message, e);
      }
    }
  }
	
	@SuppressWarnings("unchecked")
	public void prepareNarArtifacts(final ConfiguratorContext context, IMavenProjectFacade facade, IProgressMonitor monitor) throws MojoExecutionException,
			CoreException, MojoFailureException {
		narDependencies = new LinkedList<NarArtifact>();
	    FilterArtifacts filter = new FilterArtifacts();

	    filter.addFilter(new GroupIdFilter(cleanToBeTokenizedString(this.includeGroupIds),
	        cleanToBeTokenizedString(this.excludeGroupIds)));

	    filter.addFilter(new ArtifactIdFilter(cleanToBeTokenizedString(this.includeArtifactIds),
	        cleanToBeTokenizedString(this.excludeArtifactIds)));

	    filter.addFilter(getArtifactScopeFilter());

	    Set<Artifact> artifacts = getMavenProject().getArtifacts();

	    // perform filtering
	    try {
	      artifacts = filter.filter(artifacts);
	    } catch (ArtifactFilterException e) {
	      throw new MojoExecutionException(e.getMessage(), e);
	    }

	    for (final Artifact dependency : artifacts) {
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

  //
  // clean up configuration string before it can be tokenized
  //
  private static String cleanToBeTokenizedString(String str) {
    String ret = "";
    if (!StringUtils.isEmpty(str)) {
      // remove initial and ending spaces, plus all spaces next to commas
      ret = str.trim().replaceAll("[\\s]*,[\\s]*", ",");
    }

    return ret;
  }
}
