/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hive.ql.plan;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;

import org.apache.hadoop.hive.ql.session.SessionState;

/**
 * CreateFunctionDesc.
 * 
 */
@Explain(displayName = "Create Function")
public class CreateFunctionDesc implements Serializable {
  private static final long serialVersionUID = 1L;

  private String functionName;
  private String className;

  /**
   * For serialization only.
   */
  public CreateFunctionDesc() {
  }

  // Can manifest itself as upper case or lower case in the Hive query
  private static final String HFORGE_PREFIX = "hforge:";
  private static final int HFORGE_PREFIX_LEN = HFORGE_PREFIX.length();

  public CreateFunctionDesc(String functionName, String className) {
    this.functionName = functionName;
    if (className.substring(0, HFORGE_PREFIX_LEN).toLowerCase().equals(HFORGE_PREFIX)) {
      String actualClassName = getClassName(className.substring(HFORGE_PREFIX_LEN));
      this.className = actualClassName;
    }
    else {
      this.className = className;
    }
  }

  @Explain(displayName = "name")
  public String getFunctionName() {
    return functionName;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  @Explain(displayName = "class")
  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  private String getClassName(String hForgeName) {
    String className = null;
    if (isValidHForgeName(hForgeName)) {
      String urlForClass = "http://172.22.2.117:3000/list/" + hForgeName + "/class";
      URL url = null;
      ReadableByteChannel rbc = null;
      ByteArrayOutputStream baos = null;
      try {
        url = new URL(urlForClass);
        url.openConnection();
        InputStream is = url.openStream();
        byte[] buffer = new byte[1 << 24];
        int bytesRead;
        // TODO: try to use the same buffer
        baos = new ByteArrayOutputStream(1 << 24);
        while ((bytesRead = is.read(buffer)) > 0) {
          baos.write(buffer, 0, bytesRead);
          className = baos.toString();
          SessionState.getConsole().printInfo("Class is " + className);
        }
        is.close();
        // rbc = Channels.newChannel(url.openStream());
        // String localDest = getLocalJar(hForgeName);
        // fos = new FileOutputStream(localDest);
        // fos.getChannel().transferFrom(rbc, 0, 1 << 24);
      } catch (IOException e) {
        return null;
      } finally {
        try {
          if (rbc != null) {
            rbc.close();
          }
          if (baos != null) {
            baos.close();
          }
        } catch (IOException e) {
          // Can't do much if there was a problem closing
        }
      }
    }
    return className;
  }

  public static Boolean isValidHForgeName(String hForgeName) {

    if (hForgeName == null || !hForgeName.contains("/")) {
      return false;
    }
    else {
      return true;
    }
  }

}
