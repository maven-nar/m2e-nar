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
package com.github.sdedwards.m2e_nar;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.internal.project.registry.MavenProjectManager;
import org.eclipse.m2e.jdt.MavenJdtPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.github.sdedwards.m2e_nar.internal.BuildPathManager;

@SuppressWarnings("restriction")
public class MavenNarPlugin extends AbstractUIPlugin {

	public static String PLUGIN_ID = "com.github.sdedwards.m2e_nar";

	private static MavenNarPlugin instance = null;

	private BuildPathManager buildpathManager = null;

	public void logError(String msg) {
		getLog().log(new Status(Status.ERROR, PLUGIN_ID, msg));
	}

	public void logError(String msg, Exception e) {
		getLog().log(new Status(Status.ERROR, PLUGIN_ID, msg, e));
	}

	public void log(String message) {
		getLog().log(new Status(Status.INFO, PLUGIN_ID, message));
	}

	public void log(String message, Exception e) {
		getLog().log(new Status(Status.INFO, PLUGIN_ID, message, e));
	}

	public MavenNarPlugin() {
		instance = this;

		if (Boolean.parseBoolean(Platform.getDebugOption(PLUGIN_ID
				+ "/debug/initialization"))) {
			System.err.println("### executing constructor " + PLUGIN_ID);
			new Throwable().printStackTrace();
		}
	}

	/**
	 * @noreference see class javadoc
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);

		if (Boolean.parseBoolean(Platform.getDebugOption(PLUGIN_ID
				+ "/debug/initialization"))) {
			System.err.println("### executing start() " + PLUGIN_ID);
			new Throwable().printStackTrace();
		}
		
		// Make sure the m2e jdt plugin is initialised first
		MavenJdtPlugin.getDefault();

		MavenProjectManager projectManager = MavenPluginActivator.getDefault()
				.getMavenProjectManager();

		this.buildpathManager = new BuildPathManager(projectManager);
		projectManager.addMavenProjectChangedListener(this.buildpathManager);

		/*
		 * this.launchConfigurationListener = new
		 * MavenLaunchConfigurationListener();
		 * DebugPlugin.getDefault().getLaunchManager
		 * ().addLaunchConfigurationListener(launchConfigurationListener);
		 * projectManager
		 * .addMavenProjectChangedListener(launchConfigurationListener);
		 * 
		 * this.mavenClassifierManager = new MavenClassifierManager();
		 */
	}

	/**
	 * @noreference see class javadoc
	 */
	public void stop(BundleContext context) throws Exception {
		MavenProjectManager projectManager = MavenPluginActivator.getDefault()
				.getMavenProjectManager();
		projectManager.removeMavenProjectChangedListener(buildpathManager);

		/*
		 * workspace.removeResourceChangeListener(this.buildpathManager);
		 * 
		 * DebugPlugin.getDefault().getLaunchManager().
		 * removeLaunchConfigurationListener(launchConfigurationListener);
		 * projectManager
		 * .removeMavenProjectChangedListener(launchConfigurationListener);
		 * 
		 * this.launchConfigurationListener = null; this.mavenClassifierManager
		 * = null;
		 */
		this.buildpathManager = null;
	}

	public static MavenNarPlugin getDefault() {
		return instance;
	}

	/*
	 * public IClasspathManager getBuildpathManager() { return buildpathManager;
	 * } /* /**
	 * 
	 * @return Returns the mavenClassifierManager.
	 */
	/*
	 * public IMavenClassifierManager getMavenClassifierManager() { return
	 * this.mavenClassifierManager; }
	 */
}
