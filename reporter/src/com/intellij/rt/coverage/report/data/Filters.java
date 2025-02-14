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

package com.intellij.rt.coverage.report.data;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class Filters {
  public final List<Pattern> includeClasses;
  public final List<Pattern> excludeClasses;
  public final List<Pattern> excludeAnnotations;

  public Filters(List<Pattern> includeClasses, List<Pattern> excludeClasses, List<Pattern> excludeAnnotations) {
    this.includeClasses = includeClasses;
    this.excludeClasses = excludeClasses;
    this.excludeAnnotations = excludeAnnotations;
  }

  public static final Filters EMPTY = new Filters(Collections.<Pattern>emptyList(), Collections.<Pattern>emptyList(), Collections.<Pattern>emptyList());
}
