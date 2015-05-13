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
package com.github.sdedwards.m2e_nar.internal.cdt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.github.sdedwards.m2e_nar.MavenNarPlugin;

public final class SynchroniserFactory {

	public static AbstractSettingsSynchroniser getSettingsSynchroniser(
			final String os, final String linkerName) throws CoreException {
		if ("Windows".equals(os)) {
			if ("msvc".equals(linkerName)) {
				// TODO "Microsoft Visual C++";
			}
			if ("icl".equals(linkerName)) {
				// TODO "Windows ICL";
			}
			if ("g++".equals(linkerName)) {
				// TODO "Cygwin GCC";
			}
		} else if ("Linux".equals(os)) {
			if ("gcc".equals(linkerName)) {
				return new CLinuxGccSynchroniser();
			}
			if ("g++".equals(linkerName)) {
				return new CppLinuxGccSynchroniser();
			}
			if ("icc".equals(linkerName)) {
				return new CLinuxGccSynchroniser();
			}
			if ("icpc".equals(linkerName)) {
				return new CppLinuxGccSynchroniser();
			}
			if ("ecc".equals(linkerName)) {
				// TODO "ecc";
			}
			if ("ecpc".equals(linkerName)) {
				// TODO "ecpc";
			}
		} else if ("MacOSX".equals(os)) {
			if ("gcc".equals(linkerName)) {
				return new CMacosxGccSynchroniser();
			}
			if ("g++".equals(linkerName)) {
				return new CppMacosxGccSynchroniser();
			}
			if ("icc".equals(linkerName)) {
				// TODO "MacOSX Intel C Compiler";
			}
			if ("icpc".equals(linkerName)) {
				// TODO "MacOSX Intel C++ Compiler";
			}

		} else if ("SunOS".equals(os)) {
			if ("gcc".equals(linkerName)) {
				// TODO "SunOS GCC";
			}
			if ("g++".equals(linkerName)) {
				// TODO "SunOS GCC";
			}
			if ("CC".equals(linkerName)) {
				// TODO "CC";
			}
		} else if ("AIX".equals(os)) {
			if ("gcc".equals(linkerName)) {
				// TODO "AIX GCC";
			}
			if ("g++".equals(linkerName)) {
				// TODO "AIX GCC";
			}
		}
		throw new CoreException(new Status(IStatus.ERROR,
				MavenNarPlugin.PLUGIN_ID,
				"Unknown os-linker combination \"" + os + "-" + linkerName
						+ "\""));
	}
}
