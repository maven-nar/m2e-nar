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



public class GnuCppLinkerSynchroniser extends AbstractGnuLinkerSynchroniser {
	
	protected static final String cppLinkerId = "cdt.managedbuild.tool.gnu.cpp.linker";
	protected static final String cppLdFlags = "gnu.cpp.link.option.flags";
	protected static final String cppShared = "gnu.cpp.link.option.shared";
	
	public GnuCppLinkerSynchroniser() {
	}

	@Override
	public String getToolId() {
		return cppLinkerId;
	}

	@Override
	public String getFlagsOptionId() {
		return cppLdFlags;
	}

	@Override
	public String getSharedOptionId() {
		return cppShared;
	}
}
