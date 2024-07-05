<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>${mProjectGroupId}</groupId>
    <artifactId>${mProjectArtifactId}</artifactId>
    <version>${mProjectVersion}</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>${javaVersion}</maven.compiler.source>
        <maven.compiler.target>${javaVersion}</maven.compiler.target>
        <!-- vert.x properties -->
        <vertx.version>${vertxVersion}</vertx.version>
        <#if vertxVerticle??><vertx.verticle>${vertxVerticle}</vertx.verticle></#if>
        <!-- Maven plugins -->
        <vertx-maven-plugin.version>${vmpVersion}</vertx-maven-plugin.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.vertx</groupId>
                <artifactId>${vertxBom}</artifactId>
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
        <#if vertxVersion?starts_with("5.")>
            <dependency>
                <groupId>io.vertx</groupId>
                <artifactId>vertx-launcher-application</artifactId>
            </dependency>
        </#if>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>io.reactiverse</groupId>
                <artifactId>vertx-maven-plugin</artifactId>
                <version>${r"${vertx-maven-plugin.version}"}</version>
                <executions>
                    <execution>
                        <id>vmp</id>
                        <goals>
                            <goal>initialize</goal>
                            <goal>package</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
