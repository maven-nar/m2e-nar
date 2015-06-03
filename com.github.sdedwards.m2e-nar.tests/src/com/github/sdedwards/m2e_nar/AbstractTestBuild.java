package com.github.sdedwards.m2e_nar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMavenConfiguration;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.internal.preferences.MavenPreferenceConstants;
import org.eclipse.m2e.core.internal.project.registry.MavenProjectManager;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.LocalProjectScanner;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
//import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;

@SuppressWarnings("restriction")
public class AbstractTestBuild {

	private static final String settingsFile = System.getProperty("m2e_nar.settings");

	protected static final String itPath = System.getProperty("m2e_nar.itPath", "it");

	private IWorkspace workspace;

	private IMavenConfiguration mavenConfiguration;

	private String oldUserSettingsFile = null;

	@SuppressWarnings("deprecation")
	@Before
	public void setUp() throws Exception {
		workspace = ResourcesPlugin.getWorkspace();

		// Turn off auto building
		IWorkspaceDescription description = workspace.getDescription();
		description.setAutoBuilding(false);
		workspace.setDescription(description);

		// Turn off index updating
		IEclipsePreferences store = new DefaultScope().getNode(IMavenConstants.PLUGIN_ID);
		store.putBoolean(MavenPreferenceConstants.P_UPDATE_INDEXES, false);

		mavenConfiguration = MavenPlugin.getMavenConfiguration();

		if (settingsFile != null) {
			oldUserSettingsFile = mavenConfiguration.getUserSettingsFile();
			File settings = new File(settingsFile).getCanonicalFile();
			if (settings.canRead()) {
				String userSettingsFile = settings.getAbsolutePath();
				System.out.println("Setting user settings file: " + userSettingsFile);
				mavenConfiguration.setUserSettingsFile(userSettingsFile);
			} else {
				fail("User settings file cannot be read: " + settings);
			}
		}

		cleanWorkspace();
		waitForJobs();

	}

	@After
	public void tearDown() throws CoreException {
		// workspace.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
		// waitForJobs();
		cleanWorkspace();
		waitForJobs();

		if (oldUserSettingsFile != null) {
			// Restore the user settings file location
			System.out.println("Restoring user settings file: " + oldUserSettingsFile);
			mavenConfiguration.setUserSettingsFile(oldUserSettingsFile);
		}
	}

	protected IWorkspace getWorkspace() {
		return workspace;
	}

	private void cleanWorkspace() throws CoreException {
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (IProject project : workspace.getRoot().getProjects()) {
					project.delete(false, true, monitor);
				}
			}
		}, new NullProgressMonitor());
	}

	private List<MavenProjectInfo> getProjects(Collection<MavenProjectInfo> input) {
		List<MavenProjectInfo> toRet = new ArrayList<MavenProjectInfo>();
		for (MavenProjectInfo info : input) {
			toRet.add(info);
			toRet.addAll(getProjects(info.getProjects()));
		}
		return toRet;
	}

	protected List<IProject> importProject(final String path) {
		final ArrayList<IMavenProjectImportResult> importResults = new ArrayList<IMavenProjectImportResult>();

		Job job = new Job("Importing test project") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					final IProjectConfigurationManager configManager = MavenPlugin.getProjectConfigurationManager();
					final MavenModelManager mavenModelManager = MavenPlugin.getMavenModelManager();

					final ProjectImportConfiguration configuration = new ProjectImportConfiguration();

					final LocalProjectScanner scanner = new LocalProjectScanner(workspace.getRoot().getLocation().toFile(), path, true, mavenModelManager);
					scanner.run(monitor);

					final List<MavenProjectInfo> projects = getProjects(scanner.getProjects());

					workspace.run(new IWorkspaceRunnable() {
						public void run(IProgressMonitor monitor) throws CoreException {
							importResults.addAll(configManager.importProjects(projects, configuration, monitor));
						}
					}, MavenPlugin.getProjectConfigurationManager().getRule(), IWorkspace.AVOID_UPDATE, monitor);
				} catch (CoreException e) {
					return e.getStatus();
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}

		};
		job.schedule();
		waitForJobs();
		List<IProject> createdProjects = new ArrayList<IProject>();
		for (IMavenProjectImportResult r : importResults) {
			IProject p = r.getProject();
			if (p != null && p.exists()) {
				createdProjects.add(p);
			}
		}
		assertFalse("Could not create project " + path, createdProjects.isEmpty());
		return createdProjects;
	}

	protected IProject buildProject(final String projectPath) throws CoreException, InterruptedException {
		List<IProject> createdProjects = importProject(projectPath);
		final IProject project = createdProjects.get(0);
		validateCdtProject(project);

		workspace.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobs();
		
		buildAllConfigurations(project);

		assertFalse("Build errors", hasErrorMarkers(project));

		return project;
	}

	protected void buildAllConfigurations(final IProject project) {
		final ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(project, false);
		if (prjd != null) {
			final ICConfigurationDescription[] cfgDescriptions = prjd.getConfigurations();
			if (cfgDescriptions != null && cfgDescriptions.length > 0) {
				final IConfiguration[] cfgs = new IConfiguration[cfgDescriptions.length];
				for (int i=0; i < cfgDescriptions.length; ++i) {
					cfgs[i] = ManagedBuildManager.getConfigurationForDescription(cfgDescriptions[i]);
				}
				final Job job = new Job("Building all configurations") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							ManagedBuildManager.buildConfigurations(cfgs, monitor);
						} catch (CoreException e) {
							return e.getStatus();
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
				waitForJobs();
			}
		}		
	}
	
	private void validateCdtProject(IProject project) {
		final MavenProjectManager projectManager = MavenPluginActivator.getDefault().getMavenProjectManager();
		final IMavenProjectFacade facade = projectManager.getProject(project);
		assertNotNull(facade);
	}

	/**
	 * Process UI input but do not return for the specified time interval.
	 * 
	 * @param waitTimeMillis
	 *            the number of milliseconds
	 */
	private void delay(long waitTimeMillis) {
		// Display display = Display.getCurrent();
		//
		// // If this is the UI thread,
		// // then process input.
		//
		// if (display != null) {
		// long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
		// while (System.currentTimeMillis() < endTimeMillis) {
		// if (!display.readAndDispatch())
		// display.sleep();
		// }
		// display.update();
		// }
		// // Otherwise, perform a simple sleep.
		// else {
		try {
			Thread.sleep(waitTimeMillis);
		} catch (InterruptedException e) {
			// Ignored.
		}
		// }
	}

	/**
	 * Wait until all background tasks are complete.
	 */
	public void waitForJobs() {
		while (!Job.getJobManager().isIdle()) {
			delay(1000);
		}
	}

	public boolean hasErrorMarkers(final IProject project) throws CoreException {
		int errorCount = 0;
		int warnCount = 0;
		int infoCount = 0;
		for (final IMarker marker : project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)) {
			final Object severity = marker.getAttribute(IMarker.SEVERITY);
			final StringBuilder message = new StringBuilder();
			if (severity != null) {
				if ((Integer) severity == IMarker.SEVERITY_ERROR) {
					++errorCount;
					message.append("ERROR: ");
				} else if ((Integer) severity == IMarker.SEVERITY_WARNING) {
					++warnCount;
					message.append("WARNING: ");
				} else if ((Integer) severity == IMarker.SEVERITY_INFO) {
					++infoCount;
					message.append("INFO: ");
				}
			}
			System.out.println(message.toString() + marker.getAttribute(IMarker.MESSAGE) + " (" + marker.getResource().getName() + ":"
					+ marker.getAttribute(IMarker.LINE_NUMBER) + ")");
		}
		System.out
				.println(project.getName() + " has " + errorCount + " error marker(s), " + warnCount + " warning marker(s), " + infoCount + " info marker(s)");
		return errorCount != 0;
	}
}
