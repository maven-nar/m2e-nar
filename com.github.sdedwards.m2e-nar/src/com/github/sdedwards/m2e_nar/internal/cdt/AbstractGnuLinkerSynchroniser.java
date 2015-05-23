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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.BuildConfigurationData;
import org.eclipse.core.runtime.CoreException;

import com.github.sdedwards.m2e_nar.internal.model.NarBuildArtifact;
import com.github.sdedwards.m2e_nar.internal.model.NarLib;
import com.github.sdedwards.m2e_nar.internal.model.NarLinker;

@SuppressWarnings("restriction")
public abstract class AbstractGnuLinkerSynchroniser implements SettingsSynchroniser {

	private static final String defaultFlags = "";
	private static final String rpathFlag = "-Wl,-rpath";
	private static final String incrementalFlag = "-i";
	private static final String mapFlag = "-M";

	public AbstractGnuLinkerSynchroniser() {
	}

	public abstract String getToolId();

	public abstract String getFlagsOptionId();

	public abstract String getSharedOptionId();

	public String getFlags(final NarLinker linkerSettings, final List<NarLib> libs) {
		final StringBuilder flags = new StringBuilder();

		flags.append(defaultFlags);
		if (linkerSettings.isIncremental()) {
			flags.append(" ");
			flags.append(incrementalFlag);
		}
		if (linkerSettings.isMap()) {
			flags.append(" ");
			flags.append(mapFlag);
		}
		for (String option : linkerSettings.getOptions()) {
			flags.append(" ");
			flags.append(option);
		}
		boolean first = true;
		for (NarLib lib : libs) {
			if (!NarBuildArtifact.isSharedLibrary(lib.getType())) {
				continue;
			}
			if (first) {
				first = false;
				flags.append(" " + rpathFlag + ",");
			} else {
				flags.append(":");
			}
			flags.append(lib.getDirectory());
		}
		return flags.toString();
	}

	@Override
	public void fullSync(ICConfigurationDescription cfg, NarBuildArtifact artifactSettings) throws CoreException {
		final BuildConfigurationData confData = (BuildConfigurationData) cfg.getConfigurationData();
		final IConfiguration managedConf = confData.getConfiguration();
		for (final ITool tool : managedConf.getToolsBySuperClassId(getToolId())) {
			tool.setToolCommand(artifactSettings.getLinkerSettings().getName());
		}
	}

	@Override
	public void pathsOnlySync(ICConfigurationDescription cfg, NarBuildArtifact artifactSettings) throws CoreException {
		final BuildConfigurationData confData = (BuildConfigurationData) cfg.getConfigurationData();
		final IConfiguration managedConf = confData.getConfiguration();
		final OptionSetter optionSetter = new OptionSetter(managedConf, getToolId());
		setOptions(optionSetter, artifactSettings);
	}

	protected void setOptions(final OptionSetter optionSetter, final NarBuildArtifact settings) throws CoreException {

		// Clear all other options
		optionSetter.clearOptions();

		List<NarLib> libs = new ArrayList<NarLib>();
		if (NarBuildArtifact.EXECUTABLE.equals(settings.getType())) {
			libs.addAll(settings.getLinkerSettings().getLibs());
			libs.addAll(settings.getDependencyLibs());
		}
		String flags = getFlags(settings.getLinkerSettings(), libs);
		optionSetter.setOption(getFlagsOptionId(), flags.trim());

		if (settings.isSharedLibrary()) {
			// Add the shared flag
			optionSetter.setOption(getSharedOptionId(), true);
		}
	}
}
