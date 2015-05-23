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
 *  Make a public API for extracting NAR config
 */
package com.github.maven_nar;

import org.apache.maven.plugin.MojoFailureException;

/**
 * Keeps info on a system library
 * 
 * @author Mark Donszelmann
 */
public class SysLib implements ISysLib {
	/**
	 * Name of the system library
	 * 
	 * @parameter default-value=""
	 * @required
	 */
	private String name;

	/**
	 * Type of linking for this system library
	 * 
	 * @parameter default-value="shared"
	 * @required
	 */
	private String type = ILibrary.SHARED;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.github.maven_nar.ISysLib#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.github.maven_nar.ISysLib#getType()
	 */
	public String getType() {
		return type;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setType(String type) {
		this.type = type;
	}

}
