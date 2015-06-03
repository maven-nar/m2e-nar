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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NarCompiler {

	public enum OptimizationLevel {
		NONE, SIZE, MINIMAL, SPEED, FULL, AGGRESSIVE, EXTREME, UNSAFE
	}

	private String name;

	private final List<String> includePaths = new ArrayList<String>();
	private final List<String> systemIncludePaths = new ArrayList<String>();
	private final List<File> sourceDirectories = new ArrayList<File>();

	private boolean ignoreOptionElements = false;
	private boolean debug;
	private boolean rtti;
	private OptimizationLevel optimize;
	private boolean multiThreaded;
	private boolean exceptions;
	private List<String> defines = new ArrayList<String>();
	private List<String> undefines = new ArrayList<String>();
	private List<String> options = new ArrayList<String>();
	private Set<String> includes = new HashSet<String>();
	private Set<String> excludes = new HashSet<String>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.m2e.cdt.internal.INarCompilerSettings#getIncludePaths()
	 */
	public List<String> getIncludePaths() {
		return includePaths;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.m2e.cdt.internal.INarCompilerSettings#getSystemIncludePaths()
	 */
	public List<String> getSystemIncludePaths() {
		return systemIncludePaths;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.m2e.cdt.internal.INarCompilerSettings#getSourceDirectories()
	 */
	public List<File> getSourceDirectories() {
		return sourceDirectories;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.m2e.cdt.internal.INarCompilerSettings#isDebug()
	 */
	public boolean isDebug() {
		return debug;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.m2e.cdt.internal.INarCompilerSettings#isRtti()
	 */
	public boolean isRtti() {
		return rtti;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.m2e.cdt.internal.INarCompilerSettings#getOptimize()
	 */
	public OptimizationLevel getOptimize() {
		return optimize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.m2e.cdt.internal.INarCompilerSettings#isMultiThreaded()
	 */
	public boolean isMultiThreaded() {
		return multiThreaded;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.m2e.cdt.internal.INarCompilerSettings#isExceptions()
	 */
	public boolean isExceptions() {
		return exceptions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.m2e.cdt.internal.INarCompilerSettings#getDefines()
	 */
	public List<String> getDefines() {
		return defines;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.m2e.cdt.internal.INarCompilerSettings#getUndefines()
	 */
	public List<String> getUndefines() {
		return undefines;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.m2e.cdt.internal.INarCompilerSettings#getOptions()
	 */
	public List<String> getOptions() {
		return options;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setRtti(boolean rtti) {
		this.rtti = rtti;
	}

	public void setOptimize(OptimizationLevel optimize) {
		this.optimize = optimize;
	}

	public void setMultiThreaded(boolean multiThreaded) {
		this.multiThreaded = multiThreaded;
	}

	public void setExceptions(boolean exceptions) {
		this.exceptions = exceptions;
	}

	public Set<String> getIncludes() {
		return includes;
	}

	public Set<String> getExcludes() {
		return excludes;
	}

	public boolean isIgnoreOptionElements() {
		return ignoreOptionElements;
	}

	public void setIgnoreOptionElements(boolean ignoreOptionElements) {
		this.ignoreOptionElements = ignoreOptionElements;
	}

	
}
