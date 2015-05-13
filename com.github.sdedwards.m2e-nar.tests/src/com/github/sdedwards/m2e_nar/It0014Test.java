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
		List<IProject> createdProjects = importProject(projectPath);
		
		getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForJobs();
		
		for (IProject project : createdProjects) {
			assertFalse("Build errors", hasErrorMarkers(project));			
		}
		
		//final String buildArtifact = "nar-jni/libit0012-jni-dep-lib-static.so";

		//IProject project = buildProject(projectPath);
		//assertTrue(project.exists(Path.fromOSString(buildArtifact)));
	}
	
}
