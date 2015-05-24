package com.github.sdedwards.m2e_nar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

public class It0014Test extends AbstractTestBuild {
	@Test
	public void build() throws CoreException, InterruptedException {
		final String projectPath = itPath + "/it0014-multi-module";
		final String sharedBuildArtifact = "it0014-lib-shared/nar-shared/libit0014-lib-shared-1.0-SNAPSHOT.so";
		final String sharedTestArtifact = "it0014-lib-shared/nar-test-HelloWorldTest/HelloWorldTest";
		final String jniBuildArtifact = "it0014-jni-dep-lib-shared/nar-jni/libit0014-jni-dep-lib-shared-1.0-SNAPSHOT.so";
		
		List<IProject> createdProjects = importProject(projectPath);

		getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobs();

		for (IProject project : createdProjects) {
			buildAllConfigurations(project);
			assertFalse("Build errors", hasErrorMarkers(project));
		}

		boolean found = false;
		for (IProject project : createdProjects) {
			if ("it0014-multi-module".equals(project.getName())) {
				assertTrue(project.exists(Path.fromOSString(sharedBuildArtifact)));
				assertTrue(project.exists(Path.fromOSString(sharedTestArtifact)));
				assertTrue(project.exists(Path.fromOSString(jniBuildArtifact)));
				found = true;
				break;
			}
		}
		assertTrue(found);
	}

}
