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

package testData.deserializeLambda;

import java.io.Serializable;
import java.util.function.Supplier;

// classes: ALL
// patterns: testData.deserializeLambda.*

interface Foo { // generated $deserializeLambda$ is ignored here
  default void bar() {
    ((Supplier & Serializable) () -> {  // coverage: FULL
      System.out.println("I am covered..."); // coverage: FULL
      return null; // coverage: FULL
    }).get(); // coverage: FULL
  }
}

public class Test { // coverage: NONE

  public static void main(String[] args) {
    new Foo() {}.bar(); // coverage: FULL
  }
}
