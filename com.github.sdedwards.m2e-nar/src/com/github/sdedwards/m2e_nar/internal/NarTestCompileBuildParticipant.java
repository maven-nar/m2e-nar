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

import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NarTestCompileBuildParticipant extends NarBuildParticipant {

	private static final Logger logger = LoggerFactory.getLogger(CProjectConfigurator.class);

	public NarTestCompileBuildParticipant(MojoExecution execution, boolean runOnIncremental, boolean runOnConfiguration) {
		super(new MojoExecution(execution.getMojoDescriptor(), execution.getExecutionId(), execution.getSource()), runOnIncremental, runOnConfiguration);
		// Some versions of nar-maven-plugin don't have a nar-test-unpack goal
		// this means the test artifacts won't be available to us.
		// What we need to do is run the nar-testCompile goal without any tests
		// its configuration in order to just unpack.
		Xpp3Dom configuration = new Xpp3Dom(execution.getConfiguration());
		logger.info("Configuration before: " + configuration);
		for (int i = 0; i < configuration.getChildCount(); ++i) {
			if ("tests".equals(configuration.getChild(i).getName())) {
				configuration.removeChild(i);
				break;
			}
		}
		logger.info("Configuration after: " + configuration);
		getMojoExecution().setConfiguration(configuration);
	}

}
