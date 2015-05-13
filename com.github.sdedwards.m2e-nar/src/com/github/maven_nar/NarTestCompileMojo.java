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
 * 2015/04/18 Modified by Stephen Edwards:
 *  Make a public API for extracting NAR config
 */
package com.github.maven_nar;

/**
 * Compiles native test source files.
 *
 * @goal nar-testCompile
 * @phase test-compile
 * @requiresDependencyResolution test
 * @threadSafe
 * @author Mark Donszelmann
 */
public class NarTestCompileMojo
    extends AbstractCompileMojo
{
    /**
     * Skip running of NAR integration test plugins.
     *
     * @parameter property="skipNar" default-value="false"
     */
    protected boolean skipNar;

}
