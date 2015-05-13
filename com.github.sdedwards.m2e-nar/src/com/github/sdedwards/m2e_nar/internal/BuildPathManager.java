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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sdedwards.m2e_nar.MavenNarPlugin;
import com.github.sdedwards.m2e_nar.internal.cdt.AbstractSettingsSynchroniser;
import com.github.sdedwards.m2e_nar.internal.cdt.SettingsSynchroniser;
import com.github.sdedwards.m2e_nar.internal.cdt.SynchroniserFactory;
import com.github.sdedwards.m2e_nar.internal.model.NarBuildArtifact;
import com.github.sdedwards.m2e_nar.internal.model.NarExecution;

public class BuildPathManager implements IMavenProjectChangedListener {

	private static final Logger logger = LoggerFactory.getLogger(BuildPathManager.class);
	private final IMavenProjectRegistry projectManager;

	public BuildPathManager(IMavenProjectRegistry projectManager) {
		this.projectManager = projectManager;
	}

	@Override
	public void mavenProjectChanged(MavenProjectChangedEvent[] events,
			IProgressMonitor monitor) {
		monitor.setTaskName(Messages.BuildPathManager_setting_paths);
		final Set<IProject> projects = new HashSet<IProject>();
		for (final MavenProjectChangedEvent event : events) {
			final IProject project = event.getSource().getProject();
			if (project.isAccessible() && projects.add(project)) {
				try {
					updateBuildPaths(project, monitor);
				}
				catch (final CoreException e) {
					MavenNarPlugin.getDefault().logError("Problem when updating build paths", e);
				}
			}
		}
	}

	private void updateBuildPaths(IProject project, IProgressMonitor monitor) throws CoreException {
		final IMavenProjectFacade facade = projectManager.getProject(project);
		if (facade != null) {
			final ICProjectDescriptionManager mngr = CoreModel.getDefault()
					.getProjectDescriptionManager();
			final ICProjectDescription des = mngr.getProjectDescription(project,
					true);
			if (des != null) {
				boolean changed = false;
				logger.debug("updateBuildPaths: project=" + project.getName());
				final ConfiguratorContext context = new ConfiguratorContext(MavenPlugin.getMaven(), projectManager);
				List<NarExecution> narExecutions = MavenUtils.buildCompileNarExecutions(context,
							facade, monitor);
				narExecutions.addAll(MavenUtils.buildTestCompileNarExecutions(context,
							facade, monitor));
				for (NarExecution narSettings : narExecutions) {
					if (!narSettings.isSkip()) {
						final String os = narSettings.getOS();
						final String linkerName = narSettings.getLinkerName();
						final AbstractSettingsSynchroniser synchro = SynchroniserFactory.getSettingsSynchroniser(
								os, linkerName);
						changed = updateCdtBuildPaths(des, synchro, narSettings);
					}
				}
				if (changed) {
					mngr.setProjectDescription(project, des);
				}
			}
		}
	}

	private boolean updateCdtBuildPaths(final ICProjectDescription des, final SettingsSynchroniser synchro,
			final NarExecution narSettings) throws CoreException {
		boolean changed = false;
		for (NarBuildArtifact artifactSettings : narSettings
				.getArtifactSettings()) {
			final String configName = artifactSettings.getConfigName();
			final ICConfigurationDescription cfg = des.getConfigurationByName(configName);
			if (cfg != null) {
				logger.debug("updateBuildPaths: updating config " + configName);
				synchro.pathsOnlySync(cfg, artifactSettings);
				changed = true;
			}
			else {
				logger.debug("updateBuildPaths: could not find config " + configName);
			}
		}
		return changed;
	}

}
