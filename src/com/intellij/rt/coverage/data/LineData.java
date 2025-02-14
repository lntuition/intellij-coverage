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

package com.intellij.rt.coverage.data;

import com.intellij.rt.coverage.util.CoverageIOUtil;

import java.io.DataOutputStream;
import java.io.IOException;

public class LineData implements CoverageData {
  private final int myLineNumber;
  private String myMethodSignature;

  private int myId = -1;
  private int myHits = 0;

  private byte myStatus = -1;
  private String myUniqueTestName = null;
  private boolean myMayBeUnique = true;

  private JumpsAndSwitches myJumpsAndSwitches;

  public LineData(final int line, final String desc) {
    myLineNumber = line;
    myMethodSignature = desc;
  }

  public void touch() {
    myHits++;
  }

  public int getHits() {
    return myHits;
  }

  JumpsAndSwitches getOrCreateJumpsAndSwitches() {
    if (myJumpsAndSwitches == null) {
      myJumpsAndSwitches = new JumpsAndSwitches();
    }
    return myJumpsAndSwitches;
  }

  public void setJumpsAndSwitches(JumpsAndSwitches jumpsAndSwitches) {
    myJumpsAndSwitches = jumpsAndSwitches;
  }

  public int getStatus() {
    if (myStatus != -1) return myStatus;
    if (myHits == 0) {
      myStatus = LineCoverage.NONE;
      return myStatus;
    }

    if (myJumpsAndSwitches != null) {
      JumpData[] jumps = getOrCreateJumpsAndSwitches().getJumps();
      if (jumps != null) {
        for (final JumpData jumpData : jumps) {
          if ((jumpData.getFalseHits() > 0 ? 1 : 0) + (jumpData.getTrueHits() > 0 ? 1 : 0) < 2) {
            myStatus = LineCoverage.PARTIAL;
            return myStatus;
          }
        }
      }

      SwitchData[] switches = getOrCreateJumpsAndSwitches().getSwitches();
      if (switches != null) {
        for (final SwitchData switchData : switches) {
          if (switchData.getDefaultHits() == 0) {
            myStatus = LineCoverage.PARTIAL;
            return myStatus;
          }
          for (int i = 0; i < switchData.getHits().length; i++) {
            int hit = switchData.getHits()[i];
            if (hit == 0) {
              myStatus = LineCoverage.PARTIAL;
              return myStatus;
            }
          }
        }
      }
    }

    myStatus = LineCoverage.FULL;
    return myStatus;
  }

  public void save(final DataOutputStream os) throws IOException {
    CoverageIOUtil.writeINT(os, myLineNumber);
    CoverageIOUtil.writeUTF(os, myUniqueTestName != null ? myUniqueTestName : "");
    CoverageIOUtil.writeINT(os, myHits);
    if (myHits > 0) {
      if (myJumpsAndSwitches != null) {
        getOrCreateJumpsAndSwitches().save(os);
      } else {
        new JumpsAndSwitches().save(os);
      }
    }
  }

  public void merge(final CoverageData data) {
    LineData lineData = (LineData) data;
    setHits(myHits + lineData.getHits());
    if (myJumpsAndSwitches != null || lineData.myJumpsAndSwitches != null) {
      getOrCreateJumpsAndSwitches().merge(lineData.getOrCreateJumpsAndSwitches());
    }
    if (myMethodSignature == null) {
      myMethodSignature = lineData.myMethodSignature;
    }
    if (myStatus != -1) {
      byte status = (byte) lineData.getStatus();
      if (status > myStatus) {
        myStatus = status;
      }
    }
  }

  public int jumpsCount() {
    if (myJumpsAndSwitches == null) return 0;
    return myJumpsAndSwitches.jumpsCount();
  }

  public int switchesCount() {
    if (myJumpsAndSwitches == null) return 0;
    return myJumpsAndSwitches.switchesCount();
  }

  public JumpData addJump(final int jump) {
    return getOrCreateJumpsAndSwitches().addJump(jump);
  }

  public JumpData getJumpData(int jump) {
    return getOrCreateJumpsAndSwitches().getJumpData(jump);
  }

  public SwitchData addSwitch(final int switchNumber, final int[] keys) {
    return getOrCreateJumpsAndSwitches().addSwitch(switchNumber, keys);
  }

  public void removeSwitch(int switchNumber) {
    getOrCreateJumpsAndSwitches().removeSwitch(switchNumber);
  }

  public SwitchData getSwitchData(int switchNumber) {
    return getOrCreateJumpsAndSwitches().getSwitchData(switchNumber);
  }

  public int getLineNumber() {
    return myLineNumber;
  }

  public String getMethodSignature() {
    return myMethodSignature;
  }

  public void setMethodSignature(String methodSignature) {
    myMethodSignature = methodSignature;
  }

  public void setStatus(final byte status) {
    myStatus = status;
  }

  public void setTrueHits(final int jumpNumber, final int trueHits) {
    addJump(jumpNumber).setTrueHits(trueHits);
  }

  public void setFalseHits(final int jumpNumber, final int falseHits) {
    addJump(jumpNumber).setFalseHits(falseHits);
  }

  public void setDefaultHits(final int switchNumber, final int[] keys, final int defaultHit) {
    addSwitch(switchNumber, keys).setDefaultHits(defaultHit);
  }

  public void setSwitchHits(final int switchNumber, final int[] keys, final int[] hits) {
    addSwitch(switchNumber, keys).setKeysAndHits(keys, hits);
  }

  public JumpData[] getJumps() {
    if (myJumpsAndSwitches == null) return null;
    return getOrCreateJumpsAndSwitches().getJumps();
  }

  public SwitchData[] getSwitches() {
    if (myJumpsAndSwitches == null) return null;
    return getOrCreateJumpsAndSwitches().getSwitches();
  }

  public BranchData getBranchData() {
    if (myJumpsAndSwitches == null) return null;
    int total = 0;
    int covered = 0;

    JumpData[] jumps = myJumpsAndSwitches.getJumps();
    if (jumps != null) {
      for (JumpData jump : jumps) {
        total += 2;
        if (jump.getFalseHits() > 0) covered++;
        if (jump.getTrueHits() > 0) covered++;
      }
    }

    SwitchData[] switches = myJumpsAndSwitches.getSwitches();
    if (switches != null) {
      for (SwitchData switchData : switches) {
        for (int hit : switchData.getHits()) {
          total++;
          if (hit > 0) covered++;
        }
      }
    }

    return new BranchData(total, covered);
  }

  public void setHits(final int hits) {
    myHits = ClassData.trimHits(hits);
  }

  public void setTestName(String testName) {
    if (testName != null) {
      if (myUniqueTestName == null) {
        if (myMayBeUnique) myUniqueTestName = testName;
      } else if (!testName.equals(myUniqueTestName)) {
        myUniqueTestName = null;
        myMayBeUnique = false;
      }
    }
  }

  @SuppressWarnings("unused") // Used in IntelliJ
  public boolean isCoveredByOneTest() {
    return myUniqueTestName != null && myUniqueTestName.length() > 0;
  }

  public void removeJump(final int jump) {
    if (myJumpsAndSwitches == null) return;
    getOrCreateJumpsAndSwitches().removeJump(jump);
  }

  public void fillArrays() {
    if (myJumpsAndSwitches == null) return;
    getOrCreateJumpsAndSwitches().fillArrays();
  }

  public int getId() {
    return myId;
  }

  /**
   * Line ID is used to store coverage data in an array at runtime.
   */
  public void setId(int id) {
    myId = id;
  }
}
