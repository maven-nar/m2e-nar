package com.github.sdedwards.m2e_nar;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

public class It0012Test extends AbstractTestBuild {

	@Test
	public void build() throws CoreException, InterruptedException {
		final String projectPath = itPath + "/it0012-jni-dep-lib-static";
		final String buildArtifact = "nar-jni/libit0012-jni-dep-lib-static-1.0-SNAPSHOT.so";

		IProject project = buildProject(projectPath);
		assertTrue(project.exists(Path.fromOSString(buildArtifact)));
	}
	
}
