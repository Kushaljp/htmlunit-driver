// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.testing.drivers;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;

public class DefaultDriverSupplier implements Supplier<WebDriver> {

  private static final Logger log = Logger.getLogger(DefaultDriverSupplier.class.getName());
  private Class<? extends WebDriver> driverClass;
  private final Capabilities desiredCapabilities;

  public DefaultDriverSupplier(Capabilities desiredCapabilities) {
    this.desiredCapabilities = desiredCapabilities;
    driverClass = null;
  }

  public WebDriver get() {
    log.info("Providing default driver instance");

    try {
      return driverClass.getConstructor(Capabilities.class).newInstance(desiredCapabilities);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getTargetException());
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
