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

package org.openqa.selenium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.openqa.selenium.Platform.LINUX;
import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleIs;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElementsLocatedBy;
import static org.openqa.selenium.testing.Driver.CHROME;
import static org.openqa.selenium.testing.Driver.FIREFOX;
import static org.openqa.selenium.testing.Driver.IE;
import static org.openqa.selenium.testing.Driver.MARIONETTE;
import static org.openqa.selenium.testing.Driver.SAFARI;
import static org.openqa.selenium.testing.TestUtilities.getEffectivePlatform;
import static org.openqa.selenium.testing.TestUtilities.isChrome;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.testing.Ignore;
import org.openqa.selenium.testing.JUnit4TestBase;
import org.openqa.selenium.testing.NotYetImplemented;
import org.openqa.selenium.testing.SwitchToTopAfterTest;
import org.openqa.selenium.testing.drivers.SauceDriver;

import com.google.common.collect.Sets;

/**
 * Test screenshot feature.
 *
 * 1. check output for all possible types
 *
 * 2. check screenshot image
 *
 * Logic of screenshot check test is simple:
 * - open page with fixed amount of fixed sized and coloured areas
 * - take screenshot
 * - calculate expected colors as in tested HTML page
 * - scan screenshot for actual colors * compare
 *
 */

// TODO(user): verify expected behaviour after frame switching

// TODO(user): test screenshots at guaranteed maximized browsers
// TODO(user): test screenshots at guaranteed non maximized browsers
// TODO(user): test screenshots at guaranteed minimized browsers
// TODO(user): test screenshots at guaranteed fullscreened/kiosked browsers (WINDOWS platform specific)

public class TakesScreenshotTest extends JUnit4TestBase {

  private TakesScreenshot screenshooter;
  private File tempFile = null;

  @Before
  public void setUp() {
    assumeTrue(driver instanceof TakesScreenshot);
    screenshooter = (TakesScreenshot) driver;
  }

  @After
  public void tearDown() {
    if (tempFile != null) {
      tempFile.delete();
      tempFile = null;
    }
  }

  @Test
  public void testGetScreenshotAsFile() {
    driver.get(pages.simpleTestPage);
    tempFile = screenshooter.getScreenshotAs(OutputType.FILE);
    assertTrue(tempFile.exists());
    assertTrue(tempFile.length() > 0);
  }

  @Test
  public void testGetScreenshotAsBase64() {
    driver.get(pages.simpleTestPage);
    String screenshot = screenshooter.getScreenshotAs(OutputType.BASE64);
    assertTrue(screenshot.length() > 0);
  }

  @Test
  public void testGetScreenshotAsBinary() {
    driver.get(pages.simpleTestPage);
    byte[] screenshot = screenshooter.getScreenshotAs(OutputType.BYTES);
    assertTrue(screenshot.length > 0);
  }

  @Test
  public void testShouldCaptureScreenshotOfCurrentViewport() {
    // Fails on Sauce for whatever reason; probably display or window manager.
    assumeFalse(SauceDriver.shouldUseSauce()
        && getEffectivePlatform(driver).is(LINUX)
        && isChrome(driver));
    driver.get(appServer.whereIs("screen/screen.html"));

    BufferedImage screenshot = getImage();

    Set<String> actualColors = scanActualColors(screenshot,
                                                /* stepX in pixels */ 5,
                                                /* stepY in pixels */ 5);

    Set<String> expectedColors = generateExpectedColors( /* initial color */ 0x0F0F0F,
                                                         /* color step */ 1000,
                                                         /* grid X size */ 6,
                                                         /* grid Y size */ 6);

    compareColors(expectedColors, actualColors);
  }

  @Test
  @Ignore(value = CHROME)
  @Ignore(value = FIREFOX)
  @NotYetImplemented(value = SAFARI)
  public void testShouldCaptureScreenshotOfAnElement() throws Exception {
    driver.get(appServer.whereIs("screen/screen.html"));
    WebElement element = driver.findElement(By.id("cell11"));

    byte[] imageData = element.getScreenshotAs(OutputType.BYTES);
    assertTrue(imageData != null);
    assertTrue(imageData.length > 0);
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
    assertTrue(image != null);

    Raster raster = image.getRaster();
    String hex = String.format("#%02x%02x%02x",
                               (raster.getSample(1, 1, 0)),
                               (raster.getSample(1, 1, 1)),
                               (raster.getSample(1, 1, 2)));
    assertEquals("#0f12f7", hex);
  }

  @Test
  @Ignore(value = CHROME, reason = "takes only visible viewport")
  @Ignore(MARIONETTE)
  @Ignore(value = IE, reason = "takes only visible viewport")
  public void testShouldCaptureScreenshotOfPageWithLongX() {
    driver.get(appServer.whereIs("screen/screen_x_long.html"));

    BufferedImage screenshot = getImage();

    Set<String> actualColors = scanActualColors(screenshot,
                                               /* stepX in pixels */ 50,
                                               /* stepY in pixels */ 5);

    Set<String> expectedColors = generateExpectedColors( /* initial color */ 0x0F0F0F,
                                                    /* color step*/ 1000,
                                                    /* grid X size */ 6,
                                                    /* grid Y size */ 6);

    compareColors(expectedColors, actualColors);
  }

  @Test
  @Ignore(value = CHROME, reason = "takes only visible viewport")
  @Ignore(MARIONETTE)
  @Ignore(value = IE, reason = "takes only visible viewport")
  public void testShouldCaptureScreenshotOfPageWithLongY() {
    driver.get(appServer.whereIs("screen/screen_y_long.html"));

    BufferedImage screenshot = getImage();

    Set<String> actualColors = scanActualColors(screenshot,
                                               /* stepX in pixels */ 5,
                                               /* stepY in pixels */ 50);

    Set<String> expectedColors = generateExpectedColors( /* initial color */ 0x0F0F0F,
                                                    /* color step*/ 1000,
                                                    /* grid X size */ 6,
                                                    /* grid Y size */ 6);

    compareColors(expectedColors, actualColors);
  }

  @Test
  @Ignore(value = IE, reason = "cuts captured image at driver level")
  @Ignore(value = FIREFOX, reason = "captured image is cut at driver level")
  @Ignore(value = CHROME, reason = "takes only visible viewport")
  @Ignore(MARIONETTE)
  public void testShouldCaptureScreenshotOfPageWithTooLongX() {
    driver.get(appServer.whereIs("screen/screen_x_too_long.html"));

    BufferedImage screenshot = getImage();

    Set<String> actualColors = scanActualColors(screenshot,
                                               /* stepX in pixels */ 100,
                                               /* stepY in pixels */ 5);

    Set<String> expectedColors = generateExpectedColors( /* initial color */ 0x0F0F0F,
                                                    /* color step*/ 1000,
                                                    /* grid X size */ 6,
                                                    /* grid Y size */ 6);

    compareColors(expectedColors, actualColors);
  }

  @Test
  @Ignore(value = IE, reason = "cuts captured image at driver level")
  @Ignore(value = FIREFOX, reason = "captured image is cut at driver level")
  @Ignore(value = CHROME, reason = "takes only visible viewport")
  @Ignore(MARIONETTE)
  public void testShouldCaptureScreenshotOfPageWithTooLongY() {
    driver.get(appServer.whereIs("screen/screen_y_too_long.html"));

    BufferedImage screenshot = getImage();

    Set<String> actualColors = scanActualColors(screenshot,
                                               /* stepX in pixels */ 5,
                                               /* stepY in pixels */ 100);

    Set<String> expectedColors = generateExpectedColors( /* initial color */ 0x0F0F0F,
                                                    /* color step*/ 1000,
                                                    /* grid X size */ 6,
                                                    /* grid Y size */ 6);

    compareColors(expectedColors, actualColors);
  }

  @Test
  @Ignore(value = IE, reason = "returns null")
  @Ignore(value = FIREFOX, reason = "failed due NS_ERROR_FAILURE at context.drawWindow")
  @NotYetImplemented(value = SAFARI, reason = "An unknown server-side error")
  @Ignore(value = CHROME, reason = "takes only visible viewport")
  @Ignore(MARIONETTE)
  public void testShouldCaptureScreenshotOfPageWithTooLongXandY() {
    driver.get(appServer.whereIs("screen/screen_too_long.html"));

    BufferedImage screenshot = getImage();

    Set<String> actualColors = scanActualColors(screenshot,
                                               /* stepX in pixels */ 100,
                                               /* stepY in pixels */ 100);

    Set<String> expectedColors = generateExpectedColors( /* initial color */ 0x0F0F0F,
                                                    /* color step*/ 1000,
                                                    /* grid X size */ 6,
                                                    /* grid Y size */ 6);

    compareColors(expectedColors, actualColors);
  }

  @Test
  public void testShouldCaptureScreenshotAtFramePage() {
    // Fails on Sauce for whatever reason; probably display or window manager.
    assumeFalse(SauceDriver.shouldUseSauce()
        && getEffectivePlatform(driver).is(LINUX)
        && isChrome(driver));
    driver.get(appServer.whereIs("screen/screen_frames.html"));
    wait.until(frameToBeAvailableAndSwitchToIt(By.id("frame1")));
    wait.until(visibilityOfAllElementsLocatedBy(By.id("content")));

    driver.switchTo().defaultContent();
    wait.until(frameToBeAvailableAndSwitchToIt(By.id("frame2")));
    wait.until(visibilityOfAllElementsLocatedBy(By.id("content")));

    driver.switchTo().defaultContent();
    wait.until(titleIs("screen test"));
    BufferedImage screenshot = getImage();

    Set<String> actualColors = scanActualColors(screenshot,
                                               /* stepX in pixels */ 5,
                                               /* stepY in pixels */ 5);

    Set<String> expectedColors = new HashSet<>();
    expectedColors.addAll(generateExpectedColors( /* initial color */ 0x0F0F0F,
                                             /* color step*/ 1000,
                                             /* grid X size */ 6,
                                             /* grid Y size */ 6));
    expectedColors.addAll(generateExpectedColors( /* initial color */ 0xDFDFDF,
                                             /* color step*/ 1000,
                                             /* grid X size */ 6,
                                             /* grid Y size */ 6));

    // expectation is that screenshot at page with frames will be taken for full page
    compareColors(expectedColors, actualColors);
  }

  @Test
  @Ignore(CHROME)
  public void testShouldCaptureScreenshotAtIFramePage() {
    driver.get(appServer.whereIs("screen/screen_iframes.html"));

    BufferedImage screenshot = getImage();

    Set<String> actualColors = scanActualColors(screenshot,
                                               /* stepX in pixels */ 5,
                                               /* stepY in pixels */ 5);

    Set<String> expectedColors = new HashSet<>();
    expectedColors.addAll(generateExpectedColors( /* initial color */ 0x0F0F0F,
                                             /* color step*/ 1000,
                                             /* grid X size */ 6,
                                             /* grid Y size */ 6));
    expectedColors.addAll(generateExpectedColors( /* initial color */ 0xDFDFDF,
                                             /* color step*/ 1000,
                                             /* grid X size */ 6,
                                             /* grid Y size */ 6));

    // expectation is that screenshot at page with Iframes will be taken for full page
    compareColors(expectedColors, actualColors);
  }

  @SwitchToTopAfterTest
  @Test
  @Ignore(MARIONETTE)
  public void testShouldCaptureScreenshotAtFramePageAfterSwitching() {
    // Fails on Sauce for whatever reason; probably display or window manager.
    assumeFalse(SauceDriver.shouldUseSauce()
        && getEffectivePlatform(driver).is(LINUX)
        && isChrome(driver));
    driver.get(appServer.whereIs("screen/screen_frames.html"));

    driver.switchTo().frame(driver.findElement(By.id("frame2")));

    BufferedImage screenshot = getImage();

    Set<String> actualColors = scanActualColors(screenshot,
                                               /* stepX in pixels */ 5,
                                               /* stepY in pixels */ 5);

    Set<String> expectedColors = new HashSet<>();
    expectedColors.addAll(generateExpectedColors( /* initial color */ 0x0F0F0F,
                                             /* color step*/ 1000,
                                             /* grid X size */ 6,
                                             /* grid Y size */ 6));
    expectedColors.addAll(generateExpectedColors( /* initial color */ 0xDFDFDF,
                                             /* color step*/ 1000,
                                             /* grid X size */ 6,
                                             /* grid Y size */ 6));

    // expectation is that screenshot at page with frames after switching to a frame
    // will be taken for full page
    compareColors(expectedColors, actualColors);
  }

  @SwitchToTopAfterTest
  @Test
  @Ignore(CHROME)
  @Ignore(MARIONETTE)
  public void testShouldCaptureScreenshotAtIFramePageAfterSwitching() {
    driver.get(appServer.whereIs("screen/screen_iframes.html"));

    driver.switchTo().frame(driver.findElement(By.id("iframe2")));

    BufferedImage screenshot = getImage();

    Set<String> actualColors = scanActualColors(screenshot,
                                               /* stepX in pixels */ 5,
                                               /* stepY in pixels */ 5);

    Set<String> expectedColors = new HashSet<>();
    expectedColors.addAll(generateExpectedColors( /* initial color */ 0x0F0F0F,
                                             /* color step*/ 1000,
                                             /* grid X size */ 6,
                                             /* grid Y size */ 6));
    expectedColors.addAll(generateExpectedColors( /* initial color */ 0xDFDFDF,
                                             /* color step*/ 1000,
                                             /* grid X size */ 6,
                                             /* grid Y size */ 6));

    // expectation is that screenshot at page with Iframes after switching to a Iframe
    // will be taken for full page
    compareColors(expectedColors, actualColors);
  }

  /**
   * get actual image screenshot
   *
   * @return Image object
   */
  private BufferedImage getImage() {
    BufferedImage image = null;
    try {
      byte[] imageData = screenshooter.getScreenshotAs(OutputType.BYTES);
      assertTrue(imageData != null);
      assertTrue(imageData.length > 0);
      image = ImageIO.read(new ByteArrayInputStream(imageData));
      assertTrue(image != null);
    } catch (IOException e) {
      fail("Image screenshot file is invalid: " + e.getMessage());
    }

    //saveImageToTmpFile(image);
    return image;
  }

  /**
   * generate expected colors as in checked page.
   *
   * @param initialColor - initial color of first (right top) cell of grid
   * @param stepColor    - step b/w grid colors as number
   * @param nX           - grid size at X dimension
   * @param nY           - grid size at Y dimension
   * @return set of colors in string hex presentation
   */
  private Set<String> generateExpectedColors(final int initialColor, final int stepColor,
                                             final int nX, final int nY) {
    Set<String> colors = new TreeSet<>();
    int cnt = 1;
    for (int i = 1; i < nX; i++) {
      for (int j = 1; j < nY; j++) {
        int color = initialColor + (cnt * stepColor);
        String hex =
            String.format("#%02x%02x%02x", ((color & 0xFF0000) >> 16), ((color & 0x00FF00) >> 8),
                          ((color & 0x0000FF)));
        colors.add(hex);
        cnt++;
      }
    }

    return colors;
  }

  /**
   * Get colors from image from each point at grid defined by stepX/stepY.
   *
   * @param image - image
   * @param stepX - interval in pixels b/w point in X dimension
   * @param stepY - interval in pixels b/w point in Y dimension
   * @return set of colors in string hex presentation
   */
  private Set<String> scanActualColors(BufferedImage image, final int stepX, final int stepY) {
    Set<String> colors = new TreeSet<>();

    try {
      int height = image.getHeight();
      int width = image.getWidth();
      assertTrue(width > 0);
      assertTrue(height > 0);

      Raster raster = image.getRaster();
      for (int i = 0; i < width; i = i + stepX) {
        for (int j = 0; j < height; j = j + stepY) {
          String hex = String.format("#%02x%02x%02x",
                              (raster.getSample(i, j, 0)),
                              (raster.getSample(i, j, 1)),
                              (raster.getSample(i, j, 2)));
          colors.add(hex);
        }
      }
    } catch (Exception e) {
      fail("Unable to get actual colors from screenshot: " + e.getMessage());
    }

    assertTrue(colors.size() > 0);

    return colors;
  }

  /**
   * Compares sets of colors are same.
   *
   * @param expectedColors - set of expected colors
   * @param actualColors   - set of actual colors
   */
  private void compareColors(Set<String> expectedColors, Set<String> actualColors) {
    assertFalse("Actual image has only black color", onlyBlack(actualColors));
    assertFalse("Actual image has only white color", onlyWhite(actualColors));

    // Ignore black and white for further comparison
    Set<String> cleanActualColors = Sets.newHashSet(actualColors);
    cleanActualColors.remove("#000000");
    cleanActualColors.remove("#ffffff");

    if (! expectedColors.containsAll(cleanActualColors)) {
      fail("There are unexpected colors on the screenshot: " +
           Sets.difference(cleanActualColors, expectedColors));
    }

    if (! cleanActualColors.containsAll(expectedColors)) {
      fail("There are expected colors not present on the screenshot: " +
           Sets.difference(expectedColors, cleanActualColors));
    }
  }

  private boolean onlyBlack(Set<String> colors) {
    return colors.size() == 1 && "#000000".equals(colors.toArray()[0]);
  }

  private boolean onlyWhite(Set<String> colors) {
    return colors.size() == 1 && "#ffffff".equals(colors.toArray()[0]);
  }

  /**
   * Simple helper to save screenshot to tmp file. For debug purposes.
   *
   * @param im image
   */
  @SuppressWarnings("unused")
  private void saveImageToTmpFile(BufferedImage im) {

    File outputfile = new File( testName.getMethodName() + "_image.png");
    System.out.println("Image file is at " + outputfile.getAbsolutePath());
    System.out.println("Sizes  -> " + im.getWidth() + "x" + im.getHeight());
    try {
      ImageIO.write(im, "png", outputfile);
    } catch (IOException e) {
      fail("Unable to write image to file: " + e.getMessage());
    }
  }

}
