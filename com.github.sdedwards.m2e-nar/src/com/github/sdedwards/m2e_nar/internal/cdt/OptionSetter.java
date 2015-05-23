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

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.github.sdedwards.m2e_nar.MavenNarPlugin;

public final class OptionSetter {
	private final IConfiguration config;
	private final String toolId;

	public OptionSetter(final IConfiguration config, final String toolId) {
		this.config = config;
		this.toolId = toolId;
	}

	public void clearOptions() throws CoreException {
		try {
			for (ITool tool : config.getToolsBySuperClassId(toolId)) {
				for (IOption option : tool.getOptions()) {
					switch (option.getValueType()) {
					case IOption.BOOLEAN:
						config.setOption(tool, option, false);
						break;
					case IOption.STRING:
						config.setOption(tool, option, (String) null);
						break;
					}
				}
			}
		} catch (BuildException e) {
			throw new CoreException(new Status(IStatus.ERROR, MavenNarPlugin.PLUGIN_ID, "Couldn't clear options", e));
		}
	}

	public void setOption(final String optionId, final String value) throws CoreException {
		try {
			for (final ITool tool : config.getToolsBySuperClassId(toolId)) {
				final IOption option = tool.getOptionBySuperClassId(optionId);
				config.setOption(tool, option, value);
			}
		} catch (BuildException e) {
			throw new CoreException(new Status(IStatus.ERROR, MavenNarPlugin.PLUGIN_ID, "Couldn't set " + optionId + " option", e));
		}
	}

	public void setOption(final String optionId, final String[] values) throws CoreException {
		try {
			for (final ITool tool : config.getToolsBySuperClassId(toolId)) {
				final IOption option = tool.getOptionBySuperClassId(optionId);
				config.setOption(tool, option, values);
			}
		} catch (BuildException e) {
			throw new CoreException(new Status(IStatus.ERROR, MavenNarPlugin.PLUGIN_ID, "Couldn't set " + optionId + " option", e));
		}
	}

	public void setOption(final String optionId, final boolean value) throws CoreException {
		try {
			for (final ITool tool : config.getToolsBySuperClassId(toolId)) {
				final IOption option = tool.getOptionBySuperClassId(optionId);
				config.setOption(tool, option, value);
			}
		} catch (BuildException e) {
			throw new CoreException(new Status(IStatus.ERROR, MavenNarPlugin.PLUGIN_ID, "Couldn't set " + optionId + " option", e));
		}
	}
}
