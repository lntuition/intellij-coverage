/*
 * Copyright 2000-2018 JetBrains s.r.o.
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

package com.intellij.rt.coverage.instrumentation;

import com.intellij.rt.coverage.data.LineData;
import com.intellij.rt.coverage.data.ProjectData;
import com.intellij.rt.coverage.instrumentation.dataAccess.CoverageDataAccess;
import com.intellij.rt.coverage.instrumentation.dataAccess.DataAccessUtil;
import com.intellij.rt.coverage.instrumentation.util.LinesUtil;
import com.intellij.rt.coverage.instrumentation.util.LocalVariableInserter;
import org.jetbrains.coverage.org.objectweb.asm.ClassVisitor;
import org.jetbrains.coverage.org.objectweb.asm.Label;
import org.jetbrains.coverage.org.objectweb.asm.MethodVisitor;

/**
 * Insert coverage hits in line coverage mode.
 */
public class LineInstrumenter extends Instrumenter {

  private final CoverageDataAccess myDataAccess;
  private int myLastId = 0;

  public LineInstrumenter(final ProjectData projectData,
                          final ClassVisitor classVisitor,
                          final String className,
                          final boolean shouldSaveSource,
                          final CoverageDataAccess dataAccess) {
    super(projectData, classVisitor, className, shouldSaveSource);
    myDataAccess = dataAccess;
  }

  public MethodVisitor createMethodLineEnumerator(MethodVisitor mv, final String name, final String desc,
                                                  int access, String signature, String[] exceptions) {
    mv = new LocalVariableInserter(mv, access, desc, "__$localHits$__", DataAccessUtil.HITS_ARRAY_TYPE) {

      public void visitLineNumber(final int line, final Label start) {
        final LineData lineData = getOrCreateLineData(line, name, desc);
        if (lineData != null) {
          if (lineData.getId() == -1) {
            lineData.setId(myLastId++);
          }

          InstrumentationUtils.touchById(mv, getLVIndex(), lineData.getId());
        }

        super.visitLineNumber(line, start);
      }

      public void visitCode() {
        myDataAccess.onMethodStart(mv, getLVIndex());
        super.visitCode();
      }
    };
    return myDataAccess.createMethodVisitor(mv, name, true);
  }

  @Override
  public void visitEnd() {
    myDataAccess.onClassEnd(this);
    super.visitEnd();
  }

  @Override
  protected void initLineData() {
    myClassData.setLines(LinesUtil.calcLineArray(myMaxLineNumber, myLines));
    myClassData.createHitsMask(myLastId);
  }
}
