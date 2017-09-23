#!/usr/bin/groovy
/*
 *
 *   Copyright (c) 2016-2017 Red Hat, Inc.
 *
 *   Red Hat licenses this file to you under the Apache License, version
 *   2.0 (the "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *   implied.  See the License for the specific language governing
 *   permissions and limitations under the License.
 */

@Library('github.com/fabric8io/fabric8-pipeline-library@master')
def utils = new io.fabric8.Utils()
mavenNode {
  checkout scm
  readTrusted 'release.groovy'
  if (utils.isCI()){
    echo 'CI not provided by pipelines for this project yet'

  } else if (utils.isCD()){
    sh "git remote set-url origin git@github.com:fabric8io/vertx-maven-plugin.git"

    def pipeline = load 'release.groovy'
    def stagedProject
    stage ('Stage'){
      stagedProject = pipeline.stage()
    }

    // disable generating website until it works so we can release
    // stage ('Website'){
    //   pipeline.website(stagedProject)
    // }
    
    stage ('Promote'){
      pipeline.release(stagedProject)
    }
  }
}
