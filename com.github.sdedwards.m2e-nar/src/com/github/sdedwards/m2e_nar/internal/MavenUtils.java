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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.embedder.ICallable;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenExecutionContext;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.maven_nar.AbstractCompileMojo;
import com.github.maven_nar.NarCompileMojo;
import com.github.maven_nar.NarProperties;
import com.github.maven_nar.NarTestCompileMojo;
import com.github.sdedwards.m2e_nar.MavenNarPlugin;
import com.github.sdedwards.m2e_nar.internal.model.NarExecution;

public final class MavenUtils {

	public static String NAR_COMPILE_GOAL = "nar-compile";
	public static String NAR_TESTCOMPILE_GOAL = "nar-testCompile";

	public static String NAR_TESTUNPACK_GOAL = "nar-testUnpack";
	public static String NAR_TEST_UNPACK_GOAL = "nar-test-unpack";

	private static final Logger logger = LoggerFactory.getLogger(MavenUtils.class);
	private static ClassRealm realm = null;

	private static <T> T loadMojo(final IMaven maven, final MavenProject project, final MojoExecution mojoExecution, final Class<T> asType,
			final IProgressMonitor monitor) throws CoreException {
		return maven.createExecutionContext().execute(project, new ICallable<T>() {
			public T call(IMavenExecutionContext context, IProgressMonitor monitor) throws CoreException {
				return maven.getConfiguredMojo(context.getSession(), mojoExecution, asType);
			}
		}, monitor);
	}

	private static void releaseMojo(final IMaven maven, final MavenProject project, final Object mojo, final MojoExecution mojoExecution,
			final IProgressMonitor monitor) throws CoreException {
		maven.createExecutionContext().execute(project, new ICallable<Boolean>() {
			public Boolean call(IMavenExecutionContext context, IProgressMonitor monitor) throws CoreException {
				maven.releaseMojo(mojo, mojoExecution);
				return true;
			}
		}, monitor);
	}

	private static synchronized ClassRealm getMyRealm(ClassWorld world) throws CoreException {
		try {
			if (realm == null) {
				realm = world.newRealm(MavenNarPlugin.PLUGIN_ID, CProjectConfigurator.class.getClassLoader());
			}
			return realm;
		} catch (DuplicateRealmException e) {
			throw new CoreException(new Status(IStatus.ERROR, MavenNarPlugin.PLUGIN_ID, "Problem when creating realm", e));
		}
	}

	private static <T extends AbstractMojo> T getConfiguredMojo(final IMaven maven, final MavenProject project, final MojoExecution mojoExecution,
			final Class<T> asType, final Log log, final IProgressMonitor monitor) throws CoreException {
		return maven.createExecutionContext().execute(project, new ICallable<T>() {
			public T call(IMavenExecutionContext context, IProgressMonitor monitor) throws CoreException {
				return getConfiguredMojo(context.getSession(), mojoExecution, asType, log);
			}

		}, monitor);
	}

	private static <T extends AbstractMojo> T getConfiguredMojo(MavenSession session, MojoExecution mojoExecution, Class<T> asType, Log log)
			throws CoreException {
		MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();

		PluginDescriptor pluginDescriptor = mojoDescriptor.getPluginDescriptor();

		ClassRealm pluginRealm = getMyRealm(pluginDescriptor.getClassRealm().getWorld());

		T mojo;
		try {
			mojo = asType.newInstance();
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, MavenNarPlugin.PLUGIN_ID, "Problem when creating mojo", e));
		}

		mojo.setLog(log);

		logger.debug("Configuring mojo " + mojoDescriptor.getId() + " from plugin realm " + pluginRealm);

		Xpp3Dom dom = mojoExecution.getConfiguration();

		PlexusConfiguration pomConfiguration;

		if (dom == null) {
			pomConfiguration = new XmlPlexusConfiguration("configuration");
		} else {
			pomConfiguration = new XmlPlexusConfiguration(dom);
		}

		ExpressionEvaluator expressionEvaluator = new PluginParameterExpressionEvaluator(session, mojoExecution);

		populatePluginFields(mojo, mojoDescriptor, pluginRealm, pomConfiguration, expressionEvaluator);

		return mojo;
	}

	private static void populatePluginFields(Object mojo, MojoDescriptor mojoDescriptor, ClassRealm pluginRealm, PlexusConfiguration configuration,
			ExpressionEvaluator expressionEvaluator) throws CoreException {
		ComponentConfigurator configurator = new BasicComponentConfigurator();

		try {

			configurator.configureComponent(mojo, configuration, expressionEvaluator, pluginRealm);

		} catch (ComponentConfigurationException e) {
			String message = "Unable to parse configuration of mojo " + mojoDescriptor.getId();
			if (e.getFailedConfiguration() != null) {
				message += " for parameter " + e.getFailedConfiguration().getName();
			}
			message += ": " + e.getMessage();

			throw new CoreException(new Status(IStatus.ERROR, MavenNarPlugin.PLUGIN_ID, message, e));
		} catch (NoClassDefFoundError e) {
			ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(os);
			ps.println("A required class was missing during configuration of mojo " + mojoDescriptor.getId() + ": " + e.getMessage());
			pluginRealm.display(ps);

			throw new CoreException(new Status(IStatus.ERROR, MavenNarPlugin.PLUGIN_ID, os.toString(), e));
		} catch (LinkageError e) {
			ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(os);
			ps.println("An API incompatibility was encountered during configuration of mojo " + mojoDescriptor.getId() + ": " + e.getClass().getName() + ": "
					+ e.getMessage());
			pluginRealm.display(ps);

			throw new CoreException(new Status(IStatus.ERROR, MavenNarPlugin.PLUGIN_ID, os.toString(), e));
		}
	}

	public static NarExecution readCompileSettings(final ConfiguratorContext context, final IMavenProjectFacade facade, final MojoExecution compileExecution,
			final IProgressMonitor monitor) throws CoreException {
		return readSettings(context, facade, compileExecution, NarCompileMojo.class, NarExecution.MAIN, monitor);
	}

	public static NarExecution readTestCompileSettings(final ConfiguratorContext context, final IMavenProjectFacade facade,
			final MojoExecution compileExecution, final IProgressMonitor monitor) throws CoreException {
		return readSettings(context, facade, compileExecution, NarTestCompileMojo.class, NarExecution.TEST, monitor);
	}

	public static <T extends AbstractCompileMojo> NarExecution readSettings(final ConfiguratorContext context, final IMavenProjectFacade facade,
			final MojoExecution compileExecution, final Class<T> mojoType, final String buildType, final IProgressMonitor monitor) throws CoreException {
		final IMaven maven = context.getMaven();
		final MavenProject mavenProject = facade.getMavenProject();
		NarExecution settings = null;
		if (compileExecution != null) {
			// Load plugin with Maven in order to check config
			// and to get at aol.properties resource inside the plugin
			AbstractMojo narMojo = loadMojo(maven, mavenProject, compileExecution, AbstractMojo.class, monitor);
			try {
				// ClassRealm pluginRealm =
				// compileExecution.getMojoDescriptor().getPluginDescriptor().getClassRealm();
				// NarClassloader classloader = new NarClassloader(pluginRealm);
				// INarExecutionBuilder builder =
				// classloader.createNarExecutionBuilder(mavenProject,
				// compileMojo);
				// settings = builder.build(NarExecution.MAIN);
				T compileMojo = getConfiguredMojo(maven, mavenProject, compileExecution, mojoType, narMojo.getLog(), monitor);
				compileMojo.setNarProperties(new NarProperties(mavenProject, narMojo.getClass()));
				// Need to call validate to set up defaults
				compileMojo.validate();
				// Resolve the NAR artifacts, possibly from workspace
				compileMojo.prepareNarArtifacts(context, facade, monitor);
				NarExecutionBuilder builder = new NarExecutionBuilder(compileMojo, compileExecution);
				settings = builder.build(buildType);
			} catch (MojoFailureException e) {
				throw new CoreException(new Status(IStatus.ERROR, MavenNarPlugin.PLUGIN_ID, "Couldn't configure mojo"));
			} catch (MojoExecutionException e) {
				throw new CoreException(new Status(IStatus.ERROR, MavenNarPlugin.PLUGIN_ID, "Couldn't configure mojo"));
			} finally {
				releaseMojo(maven, mavenProject, narMojo, compileExecution, monitor);
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, MavenNarPlugin.PLUGIN_ID, "Couldn't find default-nar-compile execution"));
		}
		return settings;
	}

	public static List<MojoExecution> getCompileExecutions(final ConfiguratorContext context, final IMavenProjectFacade facade, final IProgressMonitor monitor)
			throws CoreException {
		return getExecutions(NAR_COMPILE_GOAL, context, facade, monitor);
	}

	public static List<MojoExecution> getTestCompileExecutions(final ConfiguratorContext context, final IMavenProjectFacade facade,
			final IProgressMonitor monitor) throws CoreException {
		return getExecutions(NAR_TESTCOMPILE_GOAL, context, facade, monitor);
	}

	public static List<MojoExecution> getExecutions(final String goal, final ConfiguratorContext context, final IMavenProjectFacade facade,
			final IProgressMonitor monitor) throws CoreException {
		final List<MojoExecution> compileExecutions = new ArrayList<MojoExecution>();

		final Map<String, Set<MojoExecutionKey>> configuratorExecutions = AbstractProjectConfigurator.getConfiguratorExecutions(facade);

		final Set<MojoExecutionKey> executionKeys = configuratorExecutions.get(CProjectConfigurator.CONFIGURATOR_ID);
		if (executionKeys != null) {
			for (MojoExecutionKey key : executionKeys) {
				final MojoExecution mojoExecution = facade.getMojoExecution(key, monitor);
				if (goal.equals(mojoExecution.getGoal())) {
					compileExecutions.add(mojoExecution);
				}
			}
		}

		return compileExecutions;
	}

	public static List<NarExecution> buildCompileNarExecutions(final ConfiguratorContext context, final IMavenProjectFacade facade,
			final IProgressMonitor monitor) throws CoreException {
		List<NarExecution> narExecutions = new ArrayList<NarExecution>();
		List<MojoExecution> compileExecutions = MavenUtils.getCompileExecutions(context, facade, monitor);
		for (MojoExecution compileExecution : compileExecutions) {
			NarExecution narSettings = MavenUtils.readCompileSettings(context, facade, compileExecution, monitor);
			narExecutions.add(narSettings);
		}
		return narExecutions;
	}

	public static List<NarExecution> buildTestCompileNarExecutions(final ConfiguratorContext context, final IMavenProjectFacade facade,
			final IProgressMonitor monitor) throws CoreException {
		List<NarExecution> narExecutions = new ArrayList<NarExecution>();
		List<MojoExecution> testCompileExecutions = MavenUtils.getTestCompileExecutions(context, facade, monitor);
		for (MojoExecution testCompileExecution : testCompileExecutions) {
			NarExecution narSettings = MavenUtils.readTestCompileSettings(context, facade, testCompileExecution, monitor);
			narExecutions.add(narSettings);
		}
		return narExecutions;
	}

	public static boolean isTestUnpack(String goal) {
		return NAR_TESTUNPACK_GOAL.equals(goal) || NAR_TEST_UNPACK_GOAL.equals(goal);
	}

}
