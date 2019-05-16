/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package rx100.internal.util;

import java.io.Serializable;

/**
 * Padding up to 128 bytes at the front.
 * Based on netty's implementation
 */
abstract class FrontPadding implements Serializable {
    /** */
    private static final long serialVersionUID = -596356687591714352L;
    /** Padding. */
    public transient long p1, p2, p3, p4, p5, p6;           // 48 bytes (header is 16 bytes)
    /** Padding. */
    public transient long p8, p9, p10, p11, p12, p13, p14, p15; // 64 bytes
}