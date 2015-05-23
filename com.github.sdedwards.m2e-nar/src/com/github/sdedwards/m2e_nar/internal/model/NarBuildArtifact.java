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
package com.github.sdedwards.m2e_nar.internal.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NarBuildArtifact {

	public static final String STATIC = "static";
	public static final String SHARED = "shared";
	public static final String EXECUTABLE = "executable";
	public static final String JNI = "jni";
	public static final String PLUGIN = "plugin";
	public static final String NONE = "none"; // no library produced

	private String configName;
	private String artifactName;
	private String type;
	private NarCompiler cppSettings;
	private NarCompiler cSettings;
	private NarLinker linkerSettings;

	private List<String> projectReferences = new ArrayList<String>();

	private List<String> javahIncludePaths = new ArrayList<String>();
	private List<String> javaIncludePaths = new ArrayList<String>();
	private List<File> dependencyIncludePaths = new ArrayList<File>();

	private List<NarLib> dependencyLibs = new ArrayList<NarLib>();
	private List<NarSysLib> dependencySysLibs = new ArrayList<NarSysLib>();
	private List<String> dependencyOptions = new ArrayList<String>();

	public String getConfigName() {
		return configName;
	}

	public String getArtifactName() {
		return artifactName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.m2e.cdt.internal.INarArtifactSettings#getType()
	 */
	public String getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.m2e.cdt.internal.INarArtifactSettings#getCppSettings()
	 */
	public NarCompiler getCppSettings() {
		return cppSettings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.m2e.cdt.internal.INarArtifactSettings#getcSettings()
	 */
	public NarCompiler getCSettings() {
		return cSettings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.m2e.cdt.internal.INarArtifactSettings#getLinkerSettings()
	 */
	public NarLinker getLinkerSettings() {
		return linkerSettings;
	}

	public List<String> getProjectReferences() {
		return projectReferences;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.m2e.cdt.internal.INarArtifactSettings#getDependencyIncludePaths
	 * ()
	 */
	public List<File> getDependencyIncludePaths() {
		return dependencyIncludePaths;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.m2e.cdt.internal.INarArtifactSettings#getJavahIncludePaths()
	 */
	public List<String> getJavahIncludePaths() {
		return javahIncludePaths;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.m2e.cdt.internal.INarArtifactSettings#getJavaIncludePaths()
	 */
	public List<String> getJavaIncludePaths() {
		return javaIncludePaths;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.m2e.cdt.internal.INarArtifactSettings#getDependencyLibs()
	 */
	public List<NarLib> getDependencyLibs() {
		return dependencyLibs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.m2e.cdt.internal.INarArtifactSettings#getDependencySysLibs()
	 */
	public List<NarSysLib> getDependencySysLibs() {
		return dependencySysLibs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.m2e.cdt.internal.INarArtifactSettings#getDependencyOptions()
	 */
	public List<String> getDependencyOptions() {
		return dependencyOptions;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setCppSettings(NarCompiler cppSettings) {
		this.cppSettings = cppSettings;
	}

	public void setCSettings(NarCompiler cSettings) {
		this.cSettings = cSettings;
	}

	public void setLinkerSettings(NarLinker linkerSettings) {
		this.linkerSettings = linkerSettings;
	}

	public static boolean isSharedLibrary(final String type) {
		return SHARED.equals(type) || JNI.equals(type) || PLUGIN.equals(type);
	}

	public boolean isSharedLibrary() {
		return isSharedLibrary(type);
	}
}
