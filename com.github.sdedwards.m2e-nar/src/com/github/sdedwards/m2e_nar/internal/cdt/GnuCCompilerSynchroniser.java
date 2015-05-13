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

public class GnuCCompilerSynchroniser extends AbstractGnuCompilerSynchroniser {
	
	protected static final String cCompilerId = "cdt.managedbuild.tool.gnu.c.compiler";
	protected static final String cOptLevel = "gnu.c.compiler.option.optimization.level";
	protected static final String cDebugLevel = "gnu.c.compiler.option.debugging.level";
	protected static final String cUndefId = "gnu.c.compiler.option.preprocessor.undef.symbol";
	protected static final String cOtherFlags = "gnu.c.compiler.option.misc.other";
	protected static final String cfPIC = "gnu.c.compiler.option.misc.pic";
	
	protected enum COptimizationLevel {
		NONE("gnu.c.optimization.level.none"),
		OPTIMIZE("gnu.c.optimization.level.optimize"),
		MORE("gnu.c.optimization.level.more"),
		MOST("gnu.c.optimization.level.most"),
		SIZE("gnu.c.optimization.level.size");
		
		final String level;
		
		private COptimizationLevel(final String level) {
			this.level = level;
		}
		
		public String toString() {
			return level;
		}
	}
	
	protected enum CDebugLevel {
		NONE("gnu.c.debugging.level.none"),
		MINIMAL("gnu.c.debugging.level.minimal"),
		DEFAULT("gnu.c.debugging.level.default"),
		MAX("gnu.c.debugging.level.max");
		
		final String level;
		
		private CDebugLevel(final String level) {
			this.level = level;
		}
		
		public String toString() {
			return level;
		}
	}

	public GnuCCompilerSynchroniser() {
	}

	@Override
	public NarCompiler getCompilerSettings(NarBuildArtifact settings) {
		return settings.getCSettings();
	}

	@Override
	public String getToolId() {
		return cCompilerId;
	}

	@Override
	public String getUndefOptionId() {
		return cUndefId;
	}

	@Override
	public String getOptLevelOptionId() {
		return cOptLevel;
	}

	@Override
	public String getOptLevel(GnuOptimizationLevel optLevel) {
		switch (optLevel) {
		case NONE:
			return COptimizationLevel.NONE.toString();
		case OPTIMIZE:
			return COptimizationLevel.OPTIMIZE.toString();
		case MORE:
			return COptimizationLevel.MORE.toString();
		case MOST:
			return COptimizationLevel.MOST.toString();
		case SIZE:
			return COptimizationLevel.SIZE.toString();
		default:
			return null;
		}
	}

	@Override
	public String getDebugLevelOptionId() {
		return cDebugLevel;
	}

	@Override
	public String getDebugLevel(GnuDebugLevel debugLevel) {
		switch (debugLevel) {
		case NONE:
			return CDebugLevel.NONE.toString();
		case MINIMAL:
			return CDebugLevel.MINIMAL.toString();
		case DEFAULT:
			return CDebugLevel.DEFAULT.toString();
		case MAX:
			return CDebugLevel.MAX.toString();
		default:
			return null;
		}
	}

	@Override
	public String getOtherFlagsOptionId() {
		return cOtherFlags;
	}

	@Override
	public String getFPICOptionId() {
		return cfPIC;
	}
}
