/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

package com.intellij.rt.coverage.instrumentation.filters.lines;

import com.intellij.rt.coverage.instrumentation.Instrumenter;
import com.intellij.rt.coverage.instrumentation.filters.KotlinCoroutinesFilter;
import org.jetbrains.coverage.org.objectweb.asm.Label;
import org.jetbrains.coverage.org.objectweb.asm.MethodVisitor;

public class KotlinCoroutinesLinesFilter extends LinesFilter {
  @Override
  public void initFilter(MethodVisitor methodVisitor, Instrumenter context, String name, String desc) {
    super.initFilter(new InternalFilter(methodVisitor, context), context, name, desc);
  }

  /**
   * This filter is applied only in line coverage mode as in branch coverage mode it is applied as BranchesFilter.
   */
  public boolean isApplicable(Instrumenter context, int access, String name,
                              String desc, String signature, String[] exceptions) {
    return !context.isBranchCoverage() && KotlinCoroutinesFilter.isApplicable(context, name, desc);
  }

  private static class InternalFilter extends KotlinCoroutinesFilter {
    public InternalFilter(MethodVisitor methodVisitor, Instrumenter context) {
      super(methodVisitor, context);
    }

    protected void onIgnoredJump() {
    }

    protected void onIgnoredSwitch(Label dflt, Label... labels) {
    }
  }
}
