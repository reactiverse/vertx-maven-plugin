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

public class Vertx4VersionAdapter implements VertxVersionAdapter {

    public static final String IO_VERTX_CORE_LAUNCHER = "io.vertx.core.Launcher";

    private final String mainClass;
    private final String mainVerticle;
    private final boolean isVertxLauncher;

    public Vertx4VersionAdapter(String mainClass, String mainVerticle, boolean isVertxLauncher) {
        this.mainClass = mainClass;
        this.mainVerticle = mainVerticle;
        this.isVertxLauncher = isVertxLauncher;
    }

    @Override
    public boolean isVertxLauncher() {
        return isVertxLauncher;
    }

    @Override
    public String mainClass() {
        return mainClass;
    }

    @Override
    public String mainVerticle() {
        return mainVerticle;
    }
}
