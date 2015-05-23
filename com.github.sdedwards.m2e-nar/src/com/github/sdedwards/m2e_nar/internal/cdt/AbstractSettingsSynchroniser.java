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
package com.github.sdedwards.m2e_nar.internal.cdt;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.ICLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.ICMacroEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.BuildConfigurationData;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sdedwards.m2e_nar.internal.BuildPathManager;
import com.github.sdedwards.m2e_nar.internal.model.NarBuildArtifact;
import com.github.sdedwards.m2e_nar.internal.model.NarLib;
import com.github.sdedwards.m2e_nar.internal.model.NarSysLib;

@SuppressWarnings("restriction")
public abstract class AbstractSettingsSynchroniser implements SettingsSynchroniser {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected static final String cppLanguageId = "org.eclipse.cdt.core.g++";
	protected static final String cLanguageId = "org.eclipse.cdt.core.gcc";
	// protected String cppSourceContentType = "org.eclipse.cdt.core.cxxSource";
	// protected String cSourceContentType = "org.eclipse.cdt.core.cSource";

	private final List<SettingsSynchroniser> toolSpecifics = new ArrayList<SettingsSynchroniser>();

	public AbstractSettingsSynchroniser() {
	}

	protected void addToolSpecificSynchroniser(SettingsSynchroniser toolSpecificSynchroniser) {
		toolSpecifics.add(toolSpecificSynchroniser);
	}

	@Override
	public void fullSync(final ICConfigurationDescription cfg, final NarBuildArtifact artifactSettings) throws CoreException {
		logger.info("Full sync to configuration " + cfg.getName());
		pathsOnlySync(cfg, artifactSettings);
		// General settings
		setArtifactName(cfg, artifactSettings);
		setSourceDirs(cfg, artifactSettings);
		setDefinedSymbols(cfg, artifactSettings);
		// Tool-specific settings
		for (SettingsSynchroniser toolSpecificSynchroniser : toolSpecifics) {
			toolSpecificSynchroniser.fullSync(cfg, artifactSettings);
		}
	}

	private void setArtifactName(ICConfigurationDescription cfg, NarBuildArtifact artifactSettings) {
		final String artifactName = artifactSettings.getArtifactName();
		if (artifactName != null) {
			BuildConfigurationData confData = (BuildConfigurationData) cfg.getConfigurationData();
			IConfiguration managedConf = confData.getConfiguration();
			managedConf.setArtifactName(artifactName);
		}
	}

	@Override
	public void pathsOnlySync(final ICConfigurationDescription cfg, final NarBuildArtifact artifactSettings) throws CoreException {
		logger.info("Paths sync to configuration " + cfg.getName());
		setProjectRefs(cfg, artifactSettings);
		setIncludes(cfg, artifactSettings);
		setLibraryPaths(cfg, artifactSettings);
		setLibraries(cfg, artifactSettings);
		// Tool-specific settings
		for (SettingsSynchroniser toolSpecificSynchroniser : toolSpecifics) {
			toolSpecificSynchroniser.pathsOnlySync(cfg, artifactSettings);
		}
	}

	public abstract String getToolchain();

	private void setProjectRefs(final ICConfigurationDescription conf, final NarBuildArtifact settings) throws CoreException {
		Map<String, String> refs = new HashMap<String, String>();
		for (String projectName : settings.getProjectReferences()) {
			// empty string means reference the active config
			refs.put(projectName, "");
		}
		conf.setReferenceInfo(refs);
	}

	private void setIncludes(final ICConfigurationDescription conf, final NarBuildArtifact settings) throws CoreException {
		final ICLanguageSetting[] languageSettings = conf.getRootFolderDescription().getLanguageSettings();

		final List<ICIncludePathEntry> cIncludePathEntries = new ArrayList<ICIncludePathEntry>();
		for (final String path : settings.getCSettings().getIncludePaths()) {
			ICIncludePathEntry includePath = createIncludePathEntry(path, ICSettingEntry.LOCAL);
			cIncludePathEntries.add(includePath);
		}
		for (final String path : settings.getCSettings().getSystemIncludePaths()) {
			ICIncludePathEntry includePath = createIncludePathEntry(path, 0);
			cIncludePathEntries.add(includePath);
		}
		final List<ICIncludePathEntry> cppIncludePathEntries = new ArrayList<ICIncludePathEntry>();
		for (final String path : settings.getCppSettings().getIncludePaths()) {
			ICIncludePathEntry includePath = createIncludePathEntry(path, ICSettingEntry.LOCAL);
			cppIncludePathEntries.add(includePath);
		}
		for (final String path : settings.getCppSettings().getSystemIncludePaths()) {
			ICIncludePathEntry includePath = createIncludePathEntry(path, 0);
			cppIncludePathEntries.add(includePath);
		}
		final List<ICIncludePathEntry> commonIncludePathEntries = new ArrayList<ICIncludePathEntry>();
		for (final String path : settings.getJavahIncludePaths()) {
			ICIncludePathEntry includePath = createIncludePathEntry(path, ICSettingEntry.LOCAL);
			commonIncludePathEntries.add(includePath);
		}
		for (final String path : settings.getJavaIncludePaths()) {
			ICIncludePathEntry includePath = createIncludePathEntry(path, 0);
			commonIncludePathEntries.add(includePath);
		}
		for (final File f : settings.getDependencyIncludePaths()) {
			ICIncludePathEntry includePath = createIncludePathEntry(f.getPath(), 0);
			commonIncludePathEntries.add(includePath);
		}
		for (ICLanguageSetting setting : languageSettings) {
			final List<ICLanguageSettingEntry> l = setting.getSettingEntriesList(ICSettingEntry.INCLUDE_PATH);
			l.clear();
			l.addAll(commonIncludePathEntries);
			if (cppLanguageId.equals(setting.getLanguageId())) {
				l.addAll(cppIncludePathEntries);
			} else if (cLanguageId.equals(setting.getLanguageId())) {
				l.addAll(cIncludePathEntries);
			}
			setting.setSettingEntries(ICSettingEntry.INCLUDE_PATH, l);
		}
	}

	private void setDefinedSymbols(final ICConfigurationDescription conf, final NarBuildArtifact settings) throws CoreException {
		final ICLanguageSetting[] languageSettings = conf.getRootFolderDescription().getLanguageSettings();

		final List<ICMacroEntry> cMacroEntries = new ArrayList<ICMacroEntry>();
		for (final String define : settings.getCSettings().getDefines()) {
			ICMacroEntry macroEntry = createMacroEntry(define, 0);
			cMacroEntries.add(macroEntry);
		}
		final List<ICMacroEntry> cppMacroEntries = new ArrayList<ICMacroEntry>();
		for (final String define : settings.getCppSettings().getDefines()) {
			ICMacroEntry macroEntry = createMacroEntry(define, 0);
			cppMacroEntries.add(macroEntry);
		}
		for (ICLanguageSetting setting : languageSettings) {
			final List<ICLanguageSettingEntry> l = setting.getSettingEntriesList(ICSettingEntry.MACRO);
			l.clear();
			if (cppLanguageId.equals(setting.getLanguageId())) {
				l.addAll(cppMacroEntries);
			} else if (cLanguageId.equals(setting.getLanguageId())) {
				l.addAll(cMacroEntries);
			}
			setting.setSettingEntries(ICSettingEntry.MACRO, l);
		}

	}

	private void setSourceDirs(final ICConfigurationDescription conf, final NarBuildArtifact settings) throws CoreException {
		final Map<String, Set<String>> sourceDirs = new HashMap<String, Set<String>>();
		for (final File f : settings.getCppSettings().getSourceDirectories()) {
			sourceDirs.put(f.getPath(), settings.getCppSettings().getExcludes());
		}
		for (final File f : settings.getCSettings().getSourceDirectories()) {
			sourceDirs.put(f.getPath(), settings.getCSettings().getExcludes());
		}
		final ICSourceEntry[] sourceEntries = new ICSourceEntry[sourceDirs.size()];
		int i = 0;
		for (final Map.Entry<String, Set<String>> sourceDir : sourceDirs.entrySet()) {
			sourceEntries[i] = createSourcePathEntry(sourceDir.getKey(), sourceDir.getValue(), 0);
			++i;
		}
		conf.setSourceEntries(sourceEntries);
	}

	private void setLibraryPaths(final ICConfigurationDescription conf, final NarBuildArtifact settings) throws CoreException {
		final ICLanguageSetting[] languageSettings = conf.getRootFolderDescription().getLanguageSettings();

		final List<ICLibraryPathEntry> libraryPathEntries = new ArrayList<ICLibraryPathEntry>();
		for (final NarLib lib : settings.getDependencyLibs()) {
			ICLibraryPathEntry libraryPath = createLibraryPathEntry(lib.getDirectory().getPath(), 0);
			libraryPathEntries.add(libraryPath);
		}
		for (final NarLib lib : settings.getLinkerSettings().getLibs()) {
			ICLibraryPathEntry libraryPath = createLibraryPathEntry(lib.getDirectory().getPath(), 0);
			libraryPathEntries.add(libraryPath);
		}
		for (final ICLanguageSetting setting : languageSettings) {
			final List<ICLanguageSettingEntry> l = setting.getSettingEntriesList(ICSettingEntry.LIBRARY_PATH);
			l.clear();
			l.addAll(libraryPathEntries);
			setting.setSettingEntries(ICSettingEntry.LIBRARY_PATH, l);
		}
	}

	private void setLibraries(final ICConfigurationDescription conf, final NarBuildArtifact settings) throws CoreException {
		final ICLanguageSetting[] languageSettings = conf.getRootFolderDescription().getLanguageSettings();

		final List<ICLibraryFileEntry> libraryEntries = new ArrayList<ICLibraryFileEntry>();
		for (final NarLib lib : settings.getDependencyLibs()) {
			final ICLibraryFileEntry library = CDataUtil.createCLibraryFileEntry(lib.getName(), 0);
			libraryEntries.add(library);
		}
		for (final NarSysLib syslib : settings.getDependencySysLibs()) {
			final ICLibraryFileEntry library = CDataUtil.createCLibraryFileEntry(syslib.getName(), 0);
			libraryEntries.add(library);
		}
		for (final NarLib lib : settings.getLinkerSettings().getLibs()) {
			final ICLibraryFileEntry library = CDataUtil.createCLibraryFileEntry(lib.getName(), 0);
			libraryEntries.add(library);
		}
		for (final NarSysLib syslib : settings.getLinkerSettings().getSysLibs()) {
			final ICLibraryFileEntry library = CDataUtil.createCLibraryFileEntry(syslib.getName(), 0);
			libraryEntries.add(library);
		}
		/*
		 * for (String path : settings.getLibraries()) { ICLibraryFileEntry
		 * library = CDataUtil.createCLibraryFileEntry(lib, 0);
		 * libraryPathEntries.add(library); }
		 */
		for (final ICLanguageSetting setting : languageSettings) {
			final List<ICLanguageSettingEntry> l = setting.getSettingEntriesList(ICSettingEntry.LIBRARY_FILE);
			l.clear();
			l.addAll(libraryEntries);
			setting.setSettingEntries(ICSettingEntry.LIBRARY_FILE, l);
		}
	}

	private ICIncludePathEntry createIncludePathEntry(final String path, final int flags) {
		final IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		final File file = new File(path);
		logger.debug("Include path " + path);
		if (!file.isAbsolute()) {
			return CDataUtil.createCIncludePathEntry(path, ICSettingEntry.VALUE_WORKSPACE_PATH | flags);
		} else {
			IContainer container = workspace.getContainerForLocation(Path.fromOSString(path));
			if (container == null) {
				return CDataUtil.createCIncludePathEntry(path, flags);
			} else {
				return CDataUtil.createCIncludePathEntry(container.getFullPath().toOSString(), ICSettingEntry.VALUE_WORKSPACE_PATH | flags);
			}
		}
	}

	private ICMacroEntry createMacroEntry(final String define, final int flags) {
		final String[] split = define.split("=", 2);
		return CDataUtil.createCMacroEntry(split[0], (split.length > 1 ? split[1] : null), flags);
	}

	private ICSourceEntry createSourcePathEntry(final String path, final Set<String> excludes, final int flags) {
		IPath[] exclusionPatterns = null;
		if (excludes.size() > 0) {
			exclusionPatterns = new IPath[excludes.size()];
			int i = 0;
			for (String exclude : excludes) {
				exclusionPatterns[i] = Path.fromOSString(exclude);
				++i;
			}
			logger.debug("Excludes for source path " + path + ": " + Arrays.deepToString(exclusionPatterns));
		}
		final IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		final File file = new File(path);
		if (!file.isAbsolute()) {
			return (ICSourceEntry) CDataUtil.createEntry(ICLanguageSettingEntry.SOURCE_PATH, path, null, exclusionPatterns, ICSettingEntry.VALUE_WORKSPACE_PATH
					| flags);
		} else {
			IContainer container = workspace.getContainerForLocation(Path.fromOSString(path));
			if (container == null) {
				return (ICSourceEntry) CDataUtil.createEntry(ICLanguageSettingEntry.SOURCE_PATH, path, null, exclusionPatterns, flags);
			} else {
				return (ICSourceEntry) CDataUtil.createEntry(ICLanguageSettingEntry.SOURCE_PATH, container.getFullPath().toOSString(), null, exclusionPatterns,
						ICSettingEntry.VALUE_WORKSPACE_PATH | flags);
			}
		}
	}

	private ICLibraryPathEntry createLibraryPathEntry(final String path, final int flags) {
		final IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		final File file = new File(path);
		if (!file.isAbsolute()) {
			return CDataUtil.createCLibraryPathEntry(path, ICSettingEntry.VALUE_WORKSPACE_PATH | flags);
		} else {
			IContainer container = workspace.getContainerForLocation(Path.fromOSString(path));
			if (container == null) {
				return CDataUtil.createCLibraryPathEntry(path, flags);
			} else {
				return CDataUtil.createCLibraryPathEntry(container.getFullPath().toOSString(), ICSettingEntry.VALUE_WORKSPACE_PATH | flags);
			}
		}
	}

}
