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

import com.github.sdedwards.m2e_nar.internal.model.NarBuildArtifact;
import com.github.sdedwards.m2e_nar.internal.model.NarCompiler;

public class GnuCppCompilerSynchroniser extends AbstractGnuCompilerSynchroniser {
	
	protected static final String cppCompilerId = "cdt.managedbuild.tool.gnu.cpp.compiler";
	protected static final String cppUndefId = "gnu.cpp.compiler.option.preprocessor.undef";
	protected static final String cppOptLevel = "gnu.cpp.compiler.option.optimization.level";
	protected static final String cppDebugLevel = "gnu.cpp.compiler.option.debugging.level";
	protected static final String cppOtherFlags = "gnu.cpp.compiler.option.other.other";
	protected static final String cppfPIC = "gnu.cpp.compiler.option.other.pic";

	private static final String noRtti = "-fno-rtti";
	
	protected enum CppOptimizationLevel {
		NONE("gnu.cpp.compiler.optimization.level.none"),
		OPTIMIZE("gnu.cpp.compiler.optimization.level.optimize"),
		MORE("gnu.cpp.compiler.optimization.level.more"),
		MOST("gnu.cpp.compiler.optimization.level.most"),
		SIZE("gnu.cpp.compiler.optimization.level.size");
		
		final String level;
		
		private CppOptimizationLevel(final String level) {
			this.level = level;
		}
		
		public String toString() {
			return level;
		}
	}

	protected enum CppDebugLevel {
		NONE("gnu.cpp.compiler.debugging.level.none"),
		MINIMAL("gnu.cpp.compiler.debugging.level.minimal"),
		DEFAULT("gnu.cpp.compiler.debugging.level.default"),
		MAX("gnu.cpp.compiler.debugging.level.max");
		
		final String level;
		
		private CppDebugLevel(final String level) {
			this.level = level;
		}
		
		public String toString() {
			return level;
		}
	}

	public GnuCppCompilerSynchroniser() {
	}

	@Override
	public NarCompiler getCompilerSettings(NarBuildArtifact settings) {
		return settings.getCppSettings();
	}

	@Override
	public String getToolId() {
		return cppCompilerId;
	}

	@Override
	public String getUndefOptionId() {
		return cppUndefId;
	}

	@Override
	public String getOptLevelOptionId() {
		return cppOptLevel;
	}

	@Override
	public String getOptLevel(GnuOptimizationLevel optLevel) {
		switch (optLevel) {
		case NONE:
			return CppOptimizationLevel.NONE.toString();
		case OPTIMIZE:
			return CppOptimizationLevel.OPTIMIZE.toString();
		case MORE:
			return CppOptimizationLevel.MORE.toString();
		case MOST:
			return CppOptimizationLevel.MOST.toString();
		case SIZE:
			return CppOptimizationLevel.SIZE.toString();
		default:
			return null;
		}
	}

	@Override
	public String getDebugLevelOptionId() {
		return cppDebugLevel;
	}

	@Override
	public String getDebugLevel(GnuDebugLevel debugLevel) {
		switch (debugLevel) {
		case NONE:
			return CppDebugLevel.NONE.toString();
		case MINIMAL:
			return CppDebugLevel.MINIMAL.toString();
		case DEFAULT:
			return CppDebugLevel.DEFAULT.toString();
		case MAX:
			return CppDebugLevel.MAX.toString();
		default:
			return null;
		}
	}

	@Override
	public String getOtherFlagsOptionId() {
		return cppOtherFlags;
	}

	@Override
	public String getFPICOptionId() {
		return cppfPIC;
	}
	
	@Override
	public String getFlags(final NarCompiler compilerSettings) {
		final String flags = super.getFlags(compilerSettings);
		if (!compilerSettings.isRtti()) {
			return flags + " " + noRtti;
		}
		return flags;
	}
}
