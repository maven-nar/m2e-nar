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
package com.github.sdedwards.m2e_nar.internal;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.maven_nar.EclipseNarLayout;
import com.github.maven_nar.ICompiler;
import com.github.maven_nar.ILib;
import com.github.maven_nar.ILibrary;
import com.github.maven_nar.ILinker;
import com.github.maven_nar.INarCompileMojo;
import com.github.maven_nar.ISysLib;
import com.github.maven_nar.ITest;
import com.github.sdedwards.m2e_nar.MavenNarPlugin;
import com.github.sdedwards.m2e_nar.internal.cdt.CdtUtils;
import com.github.sdedwards.m2e_nar.internal.model.NarBuildArtifact;
import com.github.sdedwards.m2e_nar.internal.model.NarCompiler;
import com.github.sdedwards.m2e_nar.internal.model.NarExecution;
import com.github.sdedwards.m2e_nar.internal.model.NarLib;
import com.github.sdedwards.m2e_nar.internal.model.NarLinker;
import com.github.sdedwards.m2e_nar.internal.model.NarSysLib;

public class NarExecutionBuilder implements INarExecutionBuilder {
	private static final Logger logger = LoggerFactory.getLogger(NarExecutionBuilder.class);
	private final INarCompileMojo narCompileMojo;
	private final MojoExecution mojoExecution;
	
	public NarExecutionBuilder(final AbstractMojo compileMojo, final MojoExecution mojoExceution) {
		this.narCompileMojo = (INarCompileMojo) compileMojo;
		this.mojoExecution = mojoExceution;
	}
	
	public NarExecution build(final String buildType) throws CoreException {
		try {
			NarExecution settings = new NarExecution(mojoExecution);		
			settings.setSkip(narCompileMojo.isSkip());
			settings.setOS(narCompileMojo.getOS());
			settings.setLinkerName(narCompileMojo.getLinker().getName());
			List<NarBuildArtifact> artifactSettings = settings.getArtifactSettings();
			if (NarExecution.MAIN.equals(buildType)) {
				List<?> libraries = narCompileMojo.getLibraries();
				for (Iterator<?> iter = libraries.iterator(); iter.hasNext();) {
					ILibrary library = (ILibrary) iter.next();
					NarBuildArtifact buildArtifact = buildArtifactSettings(library.getType(), buildType, library.linkCPP(), null);
					buildArtifact.setArtifactName(narCompileMojo.getOutput(library.getType()));
					artifactSettings.add(buildArtifact);
				}
			}
			else if (NarExecution.TEST.equals(buildType)) {
				List<?> tests = narCompileMojo.getTests();
				for (Iterator<?> iter = tests.iterator(); iter.hasNext();) {
					ITest test = (ITest) iter.next();
					NarBuildArtifact buildArtifact = buildArtifactSettings(NarBuildArtifact.EXECUTABLE, buildType, true, test);
					buildArtifact.setArtifactName(test.getName());
					artifactSettings.add(buildArtifact);
				}
			}
			return settings;
		}
		catch (MojoFailureException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					MavenNarPlugin.PLUGIN_ID,
					"Mojo failure",
					e));			
		}
		catch (MojoExecutionException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					MavenNarPlugin.PLUGIN_ID,
					"Mojo execution failed",
					e));
		}
	}
	
	@SuppressWarnings("unchecked")
	private NarBuildArtifact buildArtifactSettings(final String type, final String buildType, final boolean linkCPP, final ITest test) throws MojoExecutionException, MojoFailureException {
		NarBuildArtifact settings = new NarBuildArtifact();
		
		settings.setType(type);
		if (test == null) {
			settings.setConfigName(CdtUtils.getConfigName(
					mojoExecution, settings));
		} else {
			settings.setConfigName(CdtUtils.getTestConfigName(
					mojoExecution, settings));
		}
		
		List<String> projectRefs = settings.getProjectReferences();
		List<com.github.maven_nar.NarArtifact> narArtifacts = narCompileMojo.getNarArtifacts();
		for (com.github.maven_nar.NarArtifact artifact : narArtifacts) {
			if (artifact.getNarLayout() instanceof EclipseNarLayout) {
				EclipseNarLayout layout = (EclipseNarLayout)artifact.getNarLayout();
				projectRefs.add(layout.getProject().getProject().getName());
			}
		}
		
		settings.setLinkerSettings(buildLinkerSettings(narCompileMojo.getLinker(), linkCPP));
		settings.setCppSettings(buildCompilerSettings(narCompileMojo.getCpp(), buildType, test));
		settings.setCSettings(buildCompilerSettings(narCompileMojo.getC(), buildType, test));
		
		List<String> javahIncludePaths = settings.getJavahIncludePaths();
		javahIncludePaths.addAll(narCompileMojo.getJavahIncludePaths());
		List<String> javaIncludePaths = settings.getJavaIncludePaths();
		javaIncludePaths.addAll(narCompileMojo.getJavaIncludePaths());
		List<File> dependencyIncludePaths = settings.getDependencyIncludePaths();
		logger.debug("include paths size " + narCompileMojo.getDependencyIncludePaths().size());
		dependencyIncludePaths.addAll(narCompileMojo.getDependencyIncludePaths());
		List<NarLib> dependencyLibs = settings.getDependencyLibs();
		logger.debug("library size " + narCompileMojo.getDependencyLibs(type, test).size());
		for (Iterator<?> it = narCompileMojo.getDependencyLibs(type, test).iterator(); it.hasNext(); ) {
			dependencyLibs.add(buildLibSettings((ILib) it.next()));
		}
		List<NarSysLib> dependencySysLibs = settings.getDependencySysLibs();
		for (Iterator<?> it = narCompileMojo.getDependencySysLibs(type).iterator(); it.hasNext(); ) {
			dependencySysLibs.add(buildSysLibSettings((ISysLib) it.next()));
		}
		List<String> dependencyOptions = settings.getDependencyOptions();
		dependencyOptions.addAll(narCompileMojo.getDependencyOptions(type));
		
		return settings;
	}

	@SuppressWarnings("unchecked")
	public NarLinker buildLinkerSettings(final ILinker linker, final boolean linkCpp) throws MojoFailureException, MojoExecutionException {
		NarLinker settings = new NarLinker();
		settings.setName(linker.getName());
		List<NarLib> libs = settings.getLibs();
		for (Iterator<?> it = linker.getLibs().iterator(); it.hasNext(); ) {
			libs.add(buildLibSettings((ILib) it.next()));
		}
		List<NarSysLib> sysLibs = settings.getSysLibs();
		for (Iterator<?> it = linker.getSysLibs().iterator(); it.hasNext(); ) {
			sysLibs.add(buildSysLibSettings((ISysLib) it.next()));
		}			
		settings.setIncremental(linker.isIncremental());
		settings.setMap(linker.isMap());
		List<String> options = settings.getOptions();
		options.addAll(linker.getOptions());
		settings.setLinkCpp(linkCpp);
		return settings;
	}

	@SuppressWarnings("unchecked")
	private NarCompiler buildCompilerSettings(final ICompiler compiler, final String buildType, final ITest test) throws MojoFailureException, MojoExecutionException {
		NarCompiler settings = new NarCompiler();
		settings.setName(compiler.getName());
		List<String> includePaths = settings.getIncludePaths();
		includePaths.addAll(compiler.getIncludePaths(buildType));
		
		List<String> systemIncludes = compiler.getSystemIncludePaths();
		if (systemIncludes != null) {
			List<String> systemIncludePaths = settings.getSystemIncludePaths();
			systemIncludePaths.addAll(systemIncludes);
		}
		List<File> sourceDirectories = settings.getSourceDirectories();
		sourceDirectories.addAll(compiler.getSourceDirectories(buildType));
		
		settings.setDebug(compiler.isDebug());
		settings.setRtti(compiler.isRtti());
		settings.setOptimize(NarCompiler.OptimizationLevel.valueOf(compiler.getOptimize().toUpperCase()));
		settings.setMultiThreaded(compiler.isMultiThreaded());
		settings.setExceptions(compiler.isExceptions());
		List<String> defines = settings.getDefines();
		defines.addAll(compiler.getDefines());
		List<String> undefines = settings.getUndefines();
		undefines.addAll(compiler.getUndefines());
		List<String> options = settings.getOptions();
		options.addAll(compiler.getOptions());
		Set<String> includes = settings.getIncludes();
		for (Object include : compiler.getIncludes(buildType)) {
			String includeStr = (String)include;
			if (includeStr.trim().length() > 0) {
				includes.add(includeStr);
			}
		}
		Set<String> excludes = settings.getExcludes();
		for (Object exclude : compiler.getExcludes(buildType, test)) {
			String excludeStr = (String)exclude;
			if (excludeStr.trim().length() > 0) {
				excludes.add(excludeStr);
			}
		}
		return settings;
	}
	
	public NarLib buildLibSettings(final ILib lib) {
		logger.debug("NAR library: " + lib.getName());
		NarLib settings = new NarLib();
		settings.setName(lib.getName());
		settings.setType(lib.getType());
		settings.setDirectory(lib.getDirectory());
		return settings;
	}
	
	public NarSysLib buildSysLibSettings(final ISysLib syslib) {
		logger.debug("NAR sys library: " + syslib.getName());
		NarSysLib settings = new NarSysLib();
		settings.setName(syslib.getName());
		settings.setType(syslib.getType());
		return settings;
	}
}
