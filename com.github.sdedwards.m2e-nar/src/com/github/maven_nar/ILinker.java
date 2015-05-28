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

public interface ILinker {

	public abstract String getName();

	public abstract boolean isIncremental();

	public abstract boolean isMap();

	public abstract List getOptions() throws MojoFailureException, MojoExecutionException;

	public abstract List getTestOptions();
	
	public abstract List getLibs() throws MojoFailureException, MojoExecutionException;

	public abstract List getSysLibs() throws MojoFailureException, MojoExecutionException;

}