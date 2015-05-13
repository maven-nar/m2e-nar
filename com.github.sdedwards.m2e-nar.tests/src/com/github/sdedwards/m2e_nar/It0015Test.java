package com.github.sdedwards.m2e_nar;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

public class It0015Test extends AbstractTestBuild {

	@Test
	public void build() throws CoreException, InterruptedException {
		final String projectPath = itPath + "/it0015-cpp-executable";
		final String buildArtifact = "nar-executable/it0015-cpp-executable";

		IProject project = buildProject(projectPath);
		assertTrue(project.exists(Path.fromOSString(buildArtifact)));
	}
}
