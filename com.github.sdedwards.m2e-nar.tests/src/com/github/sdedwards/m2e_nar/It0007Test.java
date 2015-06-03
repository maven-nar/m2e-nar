package com.github.sdedwards.m2e_nar;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

public class It0007Test extends AbstractTestBuild {

	@Test
	public void build() throws CoreException, InterruptedException {
		final String projectPath = itPath + "/it0007-lib-shared";
		final String buildArtifact = "nar-shared/libit0007-lib-shared-1.0-SNAPSHOT.so";
		final String testArtifact = "nar-test-HelloWorldTest/HelloWorldTest";

		IProject project = buildProject(projectPath);
		assertTrue(project.exists(Path.fromOSString(buildArtifact)));
		assertTrue(project.exists(Path.fromOSString(testArtifact)));
	}
}
