/*
 * Copyright 2012 Sebastian Annies, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.coremedia.iso;

import java.nio.charset.StandardCharsets;

/**
 * Converts <code>byte[]</code> -> <code>String</code> and vice versa.
 */
public final class Ascii {
  public static byte[] convert(String s) {
    if (s != null) {
      return s.getBytes(StandardCharsets.US_ASCII);
    } else {
      return null;
    }
  }

  public static String convert(byte[] b) {
    if (b != null) {
      return new String(b, StandardCharsets.US_ASCII);
    } else {
      return null;
    }
  }
}