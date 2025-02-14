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

package com.intellij.rt.coverage.verify;

import com.intellij.rt.coverage.report.ArgParseException;

import java.io.IOException;

public class Main {
  public static void main(String[] argsList) {
    try {
      final VerifierArgs args = VerifierArgs.from(argsList);

      final Verifier verifier = new Verifier(args.rules);
      verifier.processRules(args.resultFile);

    } catch (ArgParseException e) {
      e.printStackTrace(System.err);

      for (String arg : argsList) {
        System.err.println(arg);
      }

      System.err.println();
      System.err.println(VerifierArgs.getHelpString());
      System.exit(1);
    } catch (IOException e) {
      e.printStackTrace(System.err);
      System.exit(1);
    }
  }
}
