/*
 * Copyright 2024 The Vert.x Community.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.reactiverse.vertx.maven.plugin.mojos;

/**
 * Information about the Vert.x application
 */
public class VertxApplicationInfo {

    private final String mainClass;
    private final String mainVerticle;
    private final boolean isVertxLauncher;
    private final boolean isLegacyVertxLauncher;
    private final boolean isVertx5;

    public VertxApplicationInfo(String mainClass, String mainVerticle, boolean isVertxLauncher, boolean isLegacyVertxLauncher, boolean isVertx5) {
        if (mainVerticle == null && isLegacyVertxLauncher) {
            throw new IllegalArgumentException("Main verticle cannot be null if the main class is a legacy Vert.x launcher");
        }
        if (isLegacyVertxLauncher && !isVertxLauncher) {
            throw new IllegalArgumentException("A legacy launcher is a Vert.x launcher");
        }
        this.mainClass = mainClass;
        this.mainVerticle = mainVerticle;
        this.isVertxLauncher = isVertxLauncher;
        this.isLegacyVertxLauncher = isLegacyVertxLauncher;
        this.isVertx5 = isVertx5;
    }

    /**
     * @return the main class of the Vert.x application
     */
    public String mainClass() {
        return mainClass;
    }

    /**
     * @return the main verticle of the Vert.x application, can be {@code null} if the main class is not a Vert.x launcher
     */
    public String mainVerticle() {
        return mainVerticle;
    }

    /**
     * Whether the main class is a Vert.x launcher.
     */
    public boolean isVertxLauncher() {
        return isVertxLauncher;
    }

    /**
     * Whether the main class is a legacy Vert.x launcher.
     */
    public boolean isLegacyVertxLauncher() {
        return isLegacyVertxLauncher;
    }

    /**
     * Whether the application uses Vert.x 5.
     */
    public boolean isVertx5() {
        return isVertx5;
    }
}
