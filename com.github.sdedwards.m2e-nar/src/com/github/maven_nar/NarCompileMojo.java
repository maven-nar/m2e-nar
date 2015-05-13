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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Compiles native source files.
 * 
 * @goal nar-compile
 * @phase compile
 * @requiresSession
 * @requiresProject
 * @requiresDependencyResolution compile
 * @author Mark Donszelmann
 */
public class NarCompileMojo extends AbstractCompileMojo
{
    /**
     * @parameter default-value="${project.build.directory}/classes"
     * @readonly
     */
    private File outputDirectory;

    /**
     * The current build session instance.
     * 
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    protected MavenSession session;

}
