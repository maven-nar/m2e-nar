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

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.BuildConfigurationData;
import org.eclipse.core.runtime.CoreException;

import com.github.sdedwards.m2e_nar.internal.model.NarBuildArtifact;
import com.github.sdedwards.m2e_nar.internal.model.NarCompiler;
import com.github.sdedwards.m2e_nar.internal.model.NarCompiler.OptimizationLevel;

@SuppressWarnings("restriction")
public abstract class AbstractGnuCompilerSynchroniser implements SettingsSynchroniser {

	private static final String defaultFlags = "-c";
	private static final boolean isPICMeaningful = System.getProperty("os.name").indexOf("Windows") < 0;
	private static final String noExceptions = "-fno-exceptions";

	protected enum GnuOptimizationLevel {
		NONE, OPTIMIZE, MORE, MOST, SIZE
	}

	protected enum GnuDebugLevel {
		NONE, MINIMAL, DEFAULT, MAX
	}

	public AbstractGnuCompilerSynchroniser() {
	}

	public abstract NarCompiler getCompilerSettings(NarBuildArtifact settings);

	public abstract String getToolId();

	public abstract String getUndefOptionId();

	public abstract String getOptLevelOptionId();

	public abstract String getOptLevel(GnuOptimizationLevel optLevel);

	public abstract String getDebugLevelOptionId();

	public abstract String getDebugLevel(GnuDebugLevel debugLevel);

	public abstract String getOtherFlagsOptionId();

	public abstract String getFPICOptionId();

	public String getFlags(final NarCompiler compilerSettings) {
		final StringBuilder flags = new StringBuilder();

		flags.append(defaultFlags);
		for (String option : compilerSettings.getOptions()) {
			flags.append(" ");
			flags.append(option);
		}

		// Add no exceptions flag if required
		if (!compilerSettings.isExceptions()) {
			flags.append(" ");
			flags.append(noExceptions);
		}

		return flags.toString();
	}

	@Override
	public void fullSync(ICConfigurationDescription cfg, NarBuildArtifact artifactSettings) throws CoreException {
		BuildConfigurationData confData = (BuildConfigurationData) cfg.getConfigurationData();
		IConfiguration managedConf = confData.getConfiguration();
		for (final ITool tool : managedConf.getToolsBySuperClassId(getToolId())) {
			tool.setToolCommand(getCompilerSettings(artifactSettings).getName());
		}
		final OptionSetter optionSetter = new OptionSetter(managedConf, getToolId());

		setUndefinedSymbols(optionSetter, artifactSettings);
		setOptimization(optionSetter, artifactSettings);
		setDebug(optionSetter, artifactSettings);
		setOptions(optionSetter, artifactSettings);
	}

	@Override
	public void pathsOnlySync(ICConfigurationDescription cfg, NarBuildArtifact artifactSettings) throws CoreException {
	}

	protected void setUndefinedSymbols(final OptionSetter optionSetter, final NarBuildArtifact settings) throws CoreException {
		List<String> undefines = getCompilerSettings(settings).getUndefines();
		String[] undefineArray = undefines.toArray(new String[undefines.size()]);

		optionSetter.setOption(getUndefOptionId(), undefineArray);
	}

	protected void setOptimization(final OptionSetter optionSetter, final NarBuildArtifact settings) throws CoreException {
		OptimizationLevel optLevel = getCompilerSettings(settings).getOptimize();
		GnuOptimizationLevel levelGcc = GnuOptimizationLevel.NONE;
		switch (optLevel) {
		case NONE:
			levelGcc = GnuOptimizationLevel.NONE;
			break;
		case SIZE:
			levelGcc = GnuOptimizationLevel.SIZE;
			break;
		case MINIMAL:
			levelGcc = GnuOptimizationLevel.OPTIMIZE;
			break;
		case SPEED:
			levelGcc = GnuOptimizationLevel.OPTIMIZE;
			break;
		case FULL:
			levelGcc = GnuOptimizationLevel.MORE;
			break;
		case AGGRESSIVE:
			levelGcc = GnuOptimizationLevel.MOST;
			break;
		case EXTREME:
			levelGcc = GnuOptimizationLevel.MOST;
			break;
		case UNSAFE:
			levelGcc = GnuOptimizationLevel.MOST;
			break;
		}
		optionSetter.setOption(getOptLevelOptionId(), getOptLevel(levelGcc));
	}

	protected void setDebug(final OptionSetter optionSetter, final NarBuildArtifact settings) throws CoreException {
		boolean debug = getCompilerSettings(settings).isDebug();
		GnuDebugLevel debugLevel = (debug ? GnuDebugLevel.DEFAULT : GnuDebugLevel.NONE);

		optionSetter.setOption(getDebugLevelOptionId(), getDebugLevel(debugLevel));
	}

	protected void setOptions(final OptionSetter optionSetter, final NarBuildArtifact settings) throws CoreException {
		// Clear all other options
		optionSetter.clearOptions();

		// Get the flags
		String flags = getFlags(getCompilerSettings(settings));
		optionSetter.setOption(getOtherFlagsOptionId(), flags.trim());

		// Set fPIC option if required
		if (isPICMeaningful && settings.isSharedLibrary()) {
			optionSetter.setOption(getFPICOptionId(), true);
		}
	}
}
