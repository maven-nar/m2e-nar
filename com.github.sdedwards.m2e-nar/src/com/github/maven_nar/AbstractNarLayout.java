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
 */
package com.github.maven_nar;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 * @version $Id$
 */
public abstract class AbstractNarLayout implements NarLayout, NarConstants {
	private Log log;

	protected AbstractNarLayout(Log log) {
		this.log = log;
	}

	protected Log getLog() {
		return log;
	}

	/**
	 * @return
	 * @throws MojoExecutionException
	 */
	public static NarLayout getLayout(String layoutName, Log log) throws MojoExecutionException {
		String className = layoutName.indexOf('.') < 0 ? NarLayout21.class.getPackage().getName() + "." + layoutName : layoutName;
		log.debug("Using " + className);
		Class cls;
		try {
			cls = Class.forName(className);
			Constructor ctor = cls.getConstructor(new Class[] { Log.class });
			return (NarLayout) ctor.newInstance(new Object[] { log });
		} catch (ClassNotFoundException e) {
			throw new MojoExecutionException("Cannot find class for layout " + className, e);
		} catch (InstantiationException e) {
			throw new MojoExecutionException("Cannot instantiate class for layout " + className, e);
		} catch (IllegalAccessException e) {
			throw new MojoExecutionException("Cannot access class for layout " + className, e);
		} catch (SecurityException e) {
			throw new MojoExecutionException("Cannot access class for layout " + className, e);
		} catch (NoSuchMethodException e) {
			throw new MojoExecutionException("Cannot find ctor(Log) for layout " + className, e);
		} catch (IllegalArgumentException e) {
			throw new MojoExecutionException("Wrong arguments ctor(Log) for layout " + className, e);
		} catch (InvocationTargetException e) {
			throw new MojoExecutionException("Cannot invokector(Log) for layout " + className, e);
		}
	}

}
