/*
 * #%L
 * Native ARchive plugin for Maven
 * %%
 * Copyright (C) 2002 - 2014 NAR Maven Plugin developers.
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
 * 
 * 2014/09/18 Modified by Stephen Edwards:
 *  Add layout field
 */
package com.github.maven_nar;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;

/**
 * @author Mark Donszelmann
 */
public class NarArtifact extends DefaultArtifact {

	private NarInfo narInfo;
	private NarLayout narLayout;

	public NarArtifact(Artifact dependency, NarInfo narInfo, NarLayout narLayout) {
		super(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersionRange(), dependency.getScope(), dependency.getType(), dependency
				.getClassifier(), dependency.getArtifactHandler(), dependency.isOptional());
		this.setFile(dependency.getFile());
		this.narInfo = narInfo;
		this.narLayout = narLayout;
	}

	public final NarInfo getNarInfo() {
		return narInfo;
	}

	public final NarLayout getNarLayout() {
		return narLayout;
	}

	public String getBaseFilename() {
		return getArtifactId() + "-" + getBaseVersion() + "-" + getClassifier();
	}

	public final File getTargetDirectory() {
		return getNarInfo().getTargetDirectory();
	}
}
