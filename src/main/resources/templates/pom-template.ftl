<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>${mProjectGroupId}</groupId>
    <artifactId>${mProjectArtifactId}</artifactId>
    <version>${mProjectVersion}</version>
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <!-- vert.x properties -->
        <vertx.version>${vertxVersion}</vertx.version>
        <vertx.verticle>${vertxVerticle}</vertx.verticle>
        <!-- Maven plugins -->
        <fabric8-vertx-maven-plugin.version>${fabric8VMPVersion}</fabric8-vertx-maven-plugin.version>
    </properties>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.vertx</groupId>
                <artifactId>vertx-dependencies</artifactId>
                <version>${r"${vertx.version}"}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.vertx</groupId>
            <artifactId>vertx-core</artifactId>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>io.fabric8</groupId>
                <artifactId>vertx-maven-plugin</artifactId>
                <version>${r"${fabric8-vertx-maven-plugin.version}"}</version>
                <executions>
                    <execution>
                        <id>vmp</id>
                        <goals>
                            <goal>initialize</goal>
                            <goal>package</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <redeploy>true</redeploy>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
