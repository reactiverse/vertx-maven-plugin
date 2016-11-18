/*
 *   Copyright 2016 Kamesh Sampath
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.fabric8.vertx.maven.plugin.utils;

import sun.misc.Signal;

/**
 * @author kameshs
 *         can this be made Functional Interface ??
 */
public class SignalListener {

    private static final Signal SIG_INT = new Signal("INT");

    public static void handle(final Runnable runnable) {
        Signal.handle(SIG_INT, signal -> runnable.run());
    }
}
