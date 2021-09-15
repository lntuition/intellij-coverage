/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.rt.coverage.report;

import com.intellij.rt.coverage.data.ClassData;
import com.intellij.rt.coverage.data.LineData;
import com.intellij.rt.coverage.data.ProjectData;
import com.intellij.rt.coverage.instrumentation.SaveHook;
import com.intellij.rt.coverage.util.ProjectDataLoader;
import jetbrains.coverage.report.ReportBuilderFactory;
import jetbrains.coverage.report.SourceCodeProvider;
import jetbrains.coverage.report.html.HTMLReportBuilder;
import jetbrains.coverage.report.idea.IDEACoverageData;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Reporter {
  private final File myDataFile;
  private final File mySourceMapFile;
  @Nullable
  private List<File> myOutputRoots;
  private ProjectData myProjectData;

  public Reporter(File dataFile, File sourceMapFile) {
    this(dataFile, sourceMapFile, null);
  }

  public Reporter(File dataFile, File sourceMapFile, @Nullable List<File> outputRoots) {
    myDataFile = dataFile;
    mySourceMapFile = sourceMapFile;
    myOutputRoots = outputRoots;
  }

  public File getDataFile() {
    return myDataFile;
  }

  public File getSourceMapFile() {
    return mySourceMapFile;
  }

  private ProjectData getProjectData() throws IOException {
    if (myProjectData == null) {
      final ProjectData projectData = ProjectDataLoader.load(myDataFile);
      if (myOutputRoots != null) {
        myProjectData = new ProjectData();
        final FileLocator fileLocator = new FileLocator(myOutputRoots);
        myOutputRoots = null;
        for (Map.Entry<String, ClassData> entry : projectData.getClasses().entrySet()) {
          if (fileLocator.locateClassFile(entry.getKey()).isEmpty()) continue;
          final ClassData classData = myProjectData.getOrCreateClassData(entry.getKey());
          classData.setLines((LineData[]) entry.getValue().getLines());
        }
      } else {
        myProjectData = projectData;
      }
      if (mySourceMapFile != null && mySourceMapFile.exists()) {
        SaveHook.loadAndApplySourceMap(myProjectData, mySourceMapFile);
      }
    }
    return myProjectData;
  }

  public void createXMLReport(File xmlFile) throws IOException {
    final XMLCoverageReport report = new XMLCoverageReport();
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(xmlFile);
      report.write(out, getProjectData());
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  public void createHTMLReport(File htmlDir, final List<File> sourceDirectories) throws IOException {
    final HTMLReportBuilder builder = ReportBuilderFactory.createHTMLReportBuilder();
    builder.setReportDir(htmlDir);
    final SourceCodeProvider sourceCodeProvider = new DirectorySourceCodeProvider(getProjectData(), sourceDirectories);
    builder.generateReport(new IDEACoverageData(getProjectData(), sourceCodeProvider));
  }
}