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
package com.github.sdedwards.m2e_nar.internal;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.github.sdedwards.m2e_nar.MavenNarPlugin;

public class NarClassloader extends ClassLoader {

	private final String mavenPackage = "org.eclipse.m2e.cdt.internal.nar.maven";
	private final String builder = "org.eclipse.m2e.cdt.internal.nar.maven.NarExecutionBuilder";

	private ClassRealm mavenClassloader;

	public NarClassloader(ClassRealm mavenClassloader) {
		this.mavenClassloader = mavenClassloader;
	}

	public INarExecutionBuilder createNarExecutionBuilder(final MavenProject mavenProject, final AbstractMojo mojo) throws CoreException {
		try {
			Class<?> clazz = Class.forName(builder, true, this);
			Constructor<?> constructor = clazz.getConstructor(MavenProject.class, AbstractMojo.class);
			return (INarExecutionBuilder) constructor.newInstance(mavenProject, mojo);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, MavenNarPlugin.PLUGIN_ID, "NAR Classloader problem", e));
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (!name.startsWith(mavenPackage)) {
			// Try the parent classloader by default
			try {
				return NarClassloader.class.getClassLoader().loadClass(name);
			} catch (ClassNotFoundException e) {
				// Now try the Maven plugin realm classloader
				return mavenClassloader.loadClass(name);
			}
		}
		// Find the class file in the bundle
		File file = new File(File.separator + name.replace('.', File.separatorChar) + ".class");
		URL url = getClass().getResource(file.getPath());
		if (url != null) {
			// Found it, so read it and define the class
			return readClass(name, url);
		}
		throw new ClassNotFoundException(name + " cannot be found");
	}

	private Class<?> readClass(final String name, final URL url) throws ClassNotFoundException {
		try {
			BufferedInputStream in = new BufferedInputStream(url.openStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int b = in.read();
			while (b != -1) {
				out.write(b);
				b = in.read();
			}
			byte[] buf = out.toByteArray();
			return defineClass(name, buf, 0, buf.length);
		} catch (IOException e) {
			throw new ClassNotFoundException(name + " cannot be loaded", e);
		}
	}
}
