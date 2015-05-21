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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.internal.JavaProjectConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sdedwards.m2e_nar.MavenNarPlugin;
import com.github.sdedwards.m2e_nar.internal.cdt.AbstractSettingsSynchroniser;
import com.github.sdedwards.m2e_nar.internal.cdt.CdtUtils;
import com.github.sdedwards.m2e_nar.internal.cdt.SynchroniserFactory;
import com.github.sdedwards.m2e_nar.internal.model.NarBuildArtifact;
import com.github.sdedwards.m2e_nar.internal.model.NarExecution;

@SuppressWarnings("restriction")
public class CProjectConfigurator extends AbstractProjectConfigurator {

	private static final Logger logger = LoggerFactory.getLogger(CProjectConfigurator.class);
	public static final String CONFIGURATOR_ID = "com.github.sdedwards.m2e_nar.cConfigurator";

	@Override
	public void configure(ProjectConfigurationRequest request,
			IProgressMonitor monitor) throws CoreException {

		final ConfiguratorContext context = new ConfiguratorContext(maven,
				projectManager);

		IProject project = request.getProject();

		monitor.setTaskName(Messages.CProjectConfigurator_task_name
				+ project.getName());

		logger.info("configure");

		ICProjectDescriptionManager mngr = CoreModel.getDefault()
				.getProjectDescriptionManager();

		// Set the first created configuration as active.
		boolean setActive = true;
		final IMavenProjectFacade facade = request.getMavenProjectFacade();
		List<NarExecution> narExecutions = MavenUtils.buildCompileNarExecutions(context,
				facade, monitor);
		narExecutions.addAll(MavenUtils.buildTestCompileNarExecutions(context,
				facade, monitor));
		for (NarExecution narSettings : narExecutions) {
			if (!narSettings.isSkip()) {
				final String os = narSettings.getOS();
				final String linkerName = narSettings.getLinkerName();
				final AbstractSettingsSynchroniser synchro = SynchroniserFactory
						.getSettingsSynchroniser(os, linkerName);
				final String toolchain = synchro.getToolchain();
				for (NarBuildArtifact artifactSettings : narSettings
						.getArtifactSettings()) {
					final String configName = artifactSettings.getConfigName();
					final String cdtArtefactType = CdtUtils
							.convertArtefactType(artifactSettings.getType());
					IToolChain tc = getToolChain(toolchain, cdtArtefactType);
					ICProjectDescription desc = getCdtProject(project, tc,
							cdtArtefactType, monitor);
					ICConfigurationDescription cfg = getCdtMavenConfig(project,
							desc, tc, cdtArtefactType, configName, setActive, monitor);
					setActive = false;
					synchro.fullSync(cfg, artifactSettings);
					mngr.setProjectDescription(project, desc);
				}
			}
		}

		JavaProjectConfigurator jConfig = new JavaProjectConfigurator();
		jConfig.configure(request, monitor);

		// ensure CDT builder is after the Maven one
		boolean changed = false;
		IProjectDescription description = project.getDescription();
		ICommand cdtBuilder = null;
		ICommand mavenBuilder = null;
		ArrayList<ICommand> newSpec = new ArrayList<ICommand>();
		for (ICommand command : description.getBuildSpec()) {
			if (ManagedCProjectNature.getBuilderID().equals(command.getBuilderName()) &&
					mavenBuilder == null) {
				cdtBuilder = command;
			}
			else {
				newSpec.add(command);
			}
			if (IMavenConstants.BUILDER_ID.equals(command.getBuilderName())) {
				mavenBuilder = command;
				if (cdtBuilder != null) {
					newSpec.add(cdtBuilder);
					changed = true;
				}
			}
		}
		if (changed) {
			description.setBuildSpec(newSpec.toArray(new ICommand[newSpec.size()]));
			project.setDescription(description, monitor);
		}
	}

	@Override
	public AbstractBuildParticipant getBuildParticipant(
			IMavenProjectFacade projectFacade, MojoExecution execution,
			IPluginExecutionMetadata executionMetadata) {
		final String goal = execution.getGoal();
		if ("nar-validate".equals(goal)) {
			return new MojoExecutionBuildParticipant(execution, false, true);
		} else if ("nar-download".equals(goal)) {
			return new MojoExecutionBuildParticipant(execution, false, true);
		} else if ("nar-unpack".equals(goal)) {
			return new NarBuildParticipant(execution, false, true);
		} else if ("nar-gnu-configure".equals(goal)) {
			// TODO
			return new MojoExecutionBuildParticipant(execution, false, true);
		} else if ("nar-system-generate".equals(goal)) {
			return new NarBuildParticipant(execution, false, true);
		} else if ("nar-resources".equals(goal)) {
			return new NarBuildParticipant(execution, true, true);
		} else if ("nar-gnu-resources".equals(goal)) {
			// TODO
			return new MojoExecutionBuildParticipant(execution, false, true);
		} else if ("nar-vcproj".equals(goal)) {
			// TODO
			return new MojoExecutionBuildParticipant(execution, false, true);
		} else if ("nar-javah".equals(goal)) {
			return new NarBuildParticipant(execution, true, false);
		} else if ("nar-gnu-make".equals(goal)) {
			return null;
		} else if ("nar-compile".equals(goal)) {
			return null;
		} else if ("nar-gnu-process".equals(goal)) {
			return null;
		} else if ("nar-testDownload".equals(goal)) {
			return new MojoExecutionBuildParticipant(execution, false, true);
		} else if (MavenUtils.isTestUnpack(goal)) {
			return new NarBuildParticipant(execution, false, true);
		} else if ("nar-testCompile".equals(goal)) {
			// Note that this does not actually compile the tests, only unpacks
			// test dependencies for compatibility with older versions of
			// nar-maven-plugin
			return new NarTestCompileBuildParticipant(execution, false, true);
		} else if ("nar-test".equals(goal)) {
			return null;
		}
		return super.getBuildParticipant(projectFacade, execution,
				executionMetadata);
	}

	@Override
	public void unconfigure(ProjectConfigurationRequest request,
			IProgressMonitor monitor) throws CoreException {
		super.unconfigure(request, monitor);
		// removeMavenClasspathContainer(request.getProject());
	}

	protected void addCppNature(IProject project, IProgressMonitor monitor)
			throws CoreException {
		CProjectNature.addCNature(project, monitor);
	}

	/**
	 * Checks whether toolchain can be used on this system
	 * 
	 * @param tc
	 * @return
	 */
	protected boolean isValid(IToolChain tc) {
		/*
		 * // Check for langiuage compatibility first in any case if
		 * (!isLanguageCompatible(tc, w)) return false;
		 */

		// Filter off unsupported and system toolchains
		if (tc == null || !tc.isSupported() || tc.isAbstract()
				|| tc.isSystemObject())
			return false;

		// Check for platform compatibility
		return ManagedBuildManager.isPlatformOk(tc);
	}

	protected boolean isValid(IConfiguration cfg) {
		return (!cfg.isSystemObject() && cfg.isSupported());
	}

	protected List<IConfiguration> getCfgs(IToolChain tc, String artefactType) {
		List<IConfiguration> out = new ArrayList<IConfiguration>();
		IConfiguration[] cfgs = ManagedBuildManager.getExtensionConfigurations(
				tc, ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID,
				artefactType);
		if (cfgs != null) {
			for (IConfiguration cfg : cfgs) {
				if (isValid(cfg)) {
					out.add(cfg);
				}
			}
		}
		return out;
	}

	/**
	 * Reorders selected configurations in "physical" order. Although toolchains
	 * are displayed in alphabetical order in Wizard, it's required to create
	 * corresponding configurations in the same order as they are listed in xml
	 * file, inside of single project type.
	 * 
	 * @param its
	 *            - items in initial order.
	 * @return
	 * @return - items in "physical" order.
	 */
	/*
	 * public static List<IConfiguration> reorder(List<IConfiguration> cfgs) {
	 * List<IConfiguration> ls = new ArrayList<IConfiguration>(cfgs.size());
	 * IConfiguration[] its = cfgs.toArray(new IConfiguration[cfgs.size()]);
	 * boolean found = true; while (found) { found = false; for (int i=0;
	 * i<its.length; i++) { if (its[i] == null) continue; found = true;
	 * IProjectType pt = its[i].getProjectType(); if (pt == null) {
	 * ls.add(its[i]); its[i] = null; continue; } IConfiguration[] cfs =
	 * pt.getConfigurations(); for (int j=0; j<cfs.length; j++) { for (int k=0;
	 * k<its.length; k++) { if (its[k] == null) continue; if
	 * (cfs[j].equals(its[k].getTcCfg())) { ls.add(its[k]); its[k] = null; } } }
	 * } } return ls.toArray(new CfgHolder[ls.size()]); }
	 */

	private IToolChain getToolChain(final String toolChain,
			final String artefactType) throws CoreException {
		// Find the tool chains supported on our system for the selected
		// artefact type
		IToolChain[] tcs = ManagedBuildManager.getExtensionsToolChains(
				ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID,
				artefactType, true);
		// Find the tool chain
		IToolChain tc = null;
		for (IToolChain tc2 : tcs) {
			if (isValid(tc2) && toolChain.equals(tc2.getUniqueRealName())) {
				tc = tc2;
				break;
			}
		}
		if (tc == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					MavenNarPlugin.PLUGIN_ID,
					"Could not find valid tool chain \"" + toolChain + "\""));
		}
		return tc;
	}

	private ICProjectDescription getCdtProject(IProject project, IToolChain tc,
			String artefactType, IProgressMonitor monitor) throws CoreException {
		try {
			ICProjectDescriptionManager mngr = CoreModel.getDefault()
					.getProjectDescriptionManager();
			if (!project.hasNature(CCProjectNature.CC_NATURE_ID)) {
				MavenNarPlugin.getDefault().log(
						"Configuring project with " + tc.getUniqueRealName()
								+ " tool chain");
				// Add the C++ Nature
				CCorePlugin.getDefault().convertProjectToNewCC(project,
						ManagedBuildManager.CFG_DATA_PROVIDER_ID, monitor);
				ICProjectDescription des = mngr.createProjectDescription(
						project, false, false);
				IManagedBuildInfo info = ManagedBuildManager
						.createBuildInfo(project);
				List<IConfiguration> cfgs = getCfgs(tc, artefactType);

				if (cfgs.isEmpty()) {
					throw new CoreException(new Status(IStatus.ERROR,
							MavenNarPlugin.PLUGIN_ID,
							"Cannot find any configurations"));
				}
				IConfiguration cf = cfgs.get(0);
				IManagedProject mProj = ManagedBuildManager
						.createManagedProject(project, cf.getProjectType());
				info.setManagedProject(mProj);
				return des;
			} else {
				ICProjectDescription des = mngr.getProjectDescription(project,
						true);
				return des;
			}
		} catch (BuildException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					MavenNarPlugin.PLUGIN_ID,
					"Cannot create CDT managed project", e));
		}
	}

	private ICConfigurationDescription getCdtMavenConfig(IProject project,
			ICProjectDescription des, IToolChain tc, String artefactType,
			String name, boolean setActive, IProgressMonitor monitor) throws CoreException {
		IManagedProject mProj = ManagedBuildManager.getBuildInfo(project)
				.getManagedProject();
		ICConfigurationDescription mavenCfg = des.getConfigurationByName(name);
		if (mavenCfg == null) {
			List<IConfiguration> cfgs = getCfgs(tc, artefactType);

			if (cfgs.isEmpty()) {
				throw new CoreException(new Status(IStatus.ERROR,
						MavenNarPlugin.PLUGIN_ID,
						"Cannot find any configurations"));
			}
			monitor.worked(10);
			monitor.worked(10);
			// cfgs = CfgHolder.unique(cfgs);
			// cfgs = CfgHolder.reorder(cfgs);

			IConfiguration cfgRelease = null;
			IConfiguration cfgFirst = null;

			int work = 50 / cfgs.size();

			for (IConfiguration cfg : cfgs) {
				IBuildProperty b = cfg.getBuildProperties().getProperty(
						ManagedBuildManager.BUILD_TYPE_PROPERTY_ID);
				if (cfgRelease == null
						&& b != null
						&& b.getValue() != null
						&& ManagedBuildManager.BUILD_TYPE_PROPERTY_RELEASE
								.equals(b.getValue().getId())) {
					cfgRelease = cfg;
				}
				if (cfgFirst == null) {
					cfgFirst = cfg;
				}
				monitor.worked(work);
			}
			if (cfgFirst != null) {
				if (cfgRelease != null) {
					cfgFirst = cfgRelease;
				}
				MavenNarPlugin.getDefault().log(
						"Creating configuration " + name);
				IConfiguration newCfg = createConfiguration(cfgFirst, mProj,
						des);
				newCfg.setName(name);
				newCfg.setDescription("m2e generated configuration");
				mavenCfg = ManagedBuildManager
						.getDescriptionForConfiguration(newCfg);
			}
		}
		if (mavenCfg != null) {
			if (setActive) {
				des.setActiveConfiguration(mavenCfg);
			}
			return mavenCfg;
		} else {
			throw new CoreException(new Status(IStatus.ERROR,
					MavenNarPlugin.PLUGIN_ID, "Cannot find any configurations"));
		}
		// mngr.setProjectDescription(project, des);
	}

	private IConfiguration createConfiguration(IConfiguration cfg,
			IManagedProject proj, ICProjectDescription des)
			throws WriteAccessException, CoreException {
		String id = ManagedBuildManager.calculateChildId(cfg.getId(), null);
		// CProjectDescriptionManager.getInstance();
		Configuration config = new Configuration((ManagedProject) proj,
				(Configuration) cfg, id, false, true);
		CConfigurationData data = config.getConfigurationData();
		ICConfigurationDescription cfgDes = des.createConfiguration(
				ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
		config.setConfigurationDescription(cfgDes);
		config.exportArtifactInfo();

		// Force internal builder
		IBuilder internalBuilder = ManagedBuildManager.getInternalBuilder();
		config.changeBuilder(internalBuilder, internalBuilder.getId(),
				internalBuilder.getName());

		// IBuilder bld = config.getEditableBuilder();
		// if (bld != null) { bld.setManagedBuildOn(true); }

		config.setName(cfg.getName());
		config.setArtifactName(proj.getDefaultArtifactName());

		return config;
	}
}
