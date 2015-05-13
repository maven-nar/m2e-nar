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

public class NarLinker {

	private String name;
	private List<NarLib> libs = new ArrayList<NarLib>();
	private List<NarSysLib> sysLibs = new ArrayList<NarSysLib>();
	
	private boolean incremental;
	private boolean map;
	private final List<String> options = new ArrayList<String>();
	private boolean linkCpp;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.m2e.cdt.internal.INarLinkerSettings#getLibs()
	 */
	public List<NarLib> getLibs() {
		return libs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.m2e.cdt.internal.INarLinkerSettings#getSysLibs()
	 */
	public List<NarSysLib> getSysLibs() {
		return sysLibs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.m2e.cdt.internal.INarLinkerSettings#isIncremental()
	 */
	public boolean isIncremental() {
		return incremental;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.m2e.cdt.internal.INarLinkerSettings#isMap()
	 */
	public boolean isMap() {
		return map;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.m2e.cdt.internal.INarLinkerSettings#getOptions()
	 */
	public List<String> getOptions() {
		return options;
	}

	public void setIncremental(boolean incremental) {
		this.incremental = incremental;
	}

	public void setMap(boolean map) {
		this.map = map;
	}

	public boolean isLinkCpp() {
		return linkCpp;
	}

	public void setLinkCpp(boolean linkCpp) {
		this.linkCpp = linkCpp;
	}
	
}
