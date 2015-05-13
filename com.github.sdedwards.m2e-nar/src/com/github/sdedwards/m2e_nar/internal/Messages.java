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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "com.github.sdedwards.m2e_nar.internal.messages"; //$NON-NLS-1$

  public static String CProjectConfigurator_task_name;

  public static String BuildPathManager_setting_paths;

  public static String DownloadSourcesJob_job_download;

  public static String MavenClasspathContainer_description;

  public static String MavenClasspathContainerInitializer_error_cannot_persist;

  public static String MavenClasspathContainerInitializer_job_name;

  public static String MavenClasspathContainerPage_control_desc;

  public static String MavenClasspathContainerPage_control_title;

  public static String MavenClasspathContainerPage_link;

  public static String MavenClasspathContainerPage_title;

  public static String MavenJdtPlugin_job_name;

  public static String MavenRuntimeClasspathProvider_error_unsupported;

  public static String OpenJavaDocAction_error_download;

  public static String OpenJavaDocAction_error_message;

  public static String OpenJavaDocAction_error_title;

  public static String OpenJavaDocAction_info_title;

  public static String OpenJavaDocAction_job_open_javadoc;

  public static String OpenJavaDocAction_message1;

  public static String CProjectConfigurator_error_configure_execution;
  
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}
