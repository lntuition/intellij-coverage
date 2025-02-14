/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package com.intellij.rt.coverage.instrumentation.offline;

import com.intellij.rt.coverage.data.ClassData;
import com.intellij.rt.coverage.data.ProjectData;
import com.intellij.rt.coverage.offline.RawClassData;
import com.intellij.rt.coverage.offline.RawHitsReport;
import com.intellij.rt.coverage.offline.RawProjectData;
import com.intellij.rt.coverage.util.ErrorReporter;

import java.io.File;
import java.io.IOException;

public class RawReportLoader {
  public static void load(File file, ProjectData projectData) throws IOException {
    final RawProjectData rawProjectData = RawHitsReport.load(file);
    for (RawClassData rawClassData : rawProjectData.getClasses()) {
      final ClassData classData = projectData.getClassData(rawClassData.name);
      if (classData == null) {
        ErrorReporter.reportError("Tried to apply coverage for class " + rawClassData.name + " but there is no such class in ProjectData");
        continue;
      }
      classData.setHitsMask(rawClassData.hits);
      classData.applyHits();
    }
  }
}
