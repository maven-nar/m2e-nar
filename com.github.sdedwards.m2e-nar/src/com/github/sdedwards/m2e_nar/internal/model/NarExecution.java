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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecution;

public class NarExecution {

	public static final String MAIN = "main";
	public static final String TEST = "test";

	private final MojoExecution mojoExecution;
    private boolean skip;
    private String os;
    private String linkerName;
	private List<NarBuildArtifact> artifactSettings = new ArrayList<NarBuildArtifact>();

	public NarExecution(final MojoExecution mojoExecution) {
		this.mojoExecution = mojoExecution;
	}
	
	public MojoExecution getMojoExecution() {
		return mojoExecution;
	}
	
	public boolean isSkip() {
		return skip;
	}
	
	public String getOS() {
		return os;
	}
	
	public String getLinkerName() {
		return linkerName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.m2e.cdt.internal.INarSettings#getArtifactSettings()
	 */
	public List<NarBuildArtifact> getArtifactSettings() {
		return artifactSettings;
	}

	public void setOS(String os) {
		this.os = os;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	public void setLinkerName(String linkerName) {
		this.linkerName = linkerName;
	}
}
