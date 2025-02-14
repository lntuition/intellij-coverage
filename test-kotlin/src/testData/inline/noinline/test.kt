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

package testData.inline.noinline

// classes: ALL

private inline fun foo(
    noinline a: () -> Int,
    b: () -> Int,
    noinline c: () -> Int
): Int {
    return a() + b() + c() // coverage: FULL
}

fun test() {
    foo(                   // coverage: FULL
        {
            41             // coverage: FULL
        },
        {
            42             // coverage: FULL
        },
        {
            43             // coverage: FULL
        }
    )
}

fun main() {
    test()                 // coverage: FULL
}
