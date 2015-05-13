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
package com.github.maven_nar;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public interface INarCompileMojo {

	public abstract void validate() throws MojoFailureException, MojoExecutionException;
	
	public abstract String getOS();

	public abstract boolean isSkip();
	
	public abstract String getOutput(String type) throws MojoFailureException, MojoExecutionException;
	
	public abstract List<NarArtifact> getNarArtifacts();
	
	public abstract ILinker getLinker();
	
	public abstract List/* <ILibrary> */getLibraries();
	
	public abstract List/* <ITest> */getTests();

	public abstract ICompiler getC();
	
	public abstract ICompiler getCpp();
	
	public abstract List/* <String> */getJavahIncludePaths();

	public abstract List/* <String> */getJavaIncludePaths()
			throws MojoExecutionException, MojoFailureException;

	public abstract List/* <File> */getDependencyIncludePaths()
			throws MojoExecutionException, MojoFailureException;

	public abstract List/* <ILib> */getDependencyLibs(String type, ITest test)
			throws MojoExecutionException, MojoFailureException;

	public abstract List/* <ISysLib> */getDependencySysLibs(String type)
			throws MojoExecutionException, MojoFailureException;

	public abstract List/* <String> */getDependencyOptions(String type)
			throws MojoExecutionException, MojoFailureException;

}
