/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    `maven-publish`
    signing
}

interface KoverPublicationExtension {
    val description: Property<String>
}

val extension = extensions.create<KoverPublicationExtension>("koverPublication")

extension.description.convention("")

publishing {
    repositories {
        addSonatypeRepository()
    }

    val sources = sourceSets.main.map { it.allSource }

    publications {
        register<MavenPublication>("Kover") {
            // add jar with module
            from(components["java"])

            addEmptyJavadocArtifact()
            addSourcesArtifact(sources)
        }
    }

    publications.withType<MavenPublication>().configureEach {
        addMetadata()
        signPublicationIfKeyPresent()
    }
}


fun MavenPublication.addEmptyJavadocArtifact() {
    val javadocJar by tasks.creating(org.gradle.jvm.tasks.Jar::class) {
        archiveClassifier.set("javadoc")
        // contents are deliberately left empty
    }
    artifact(javadocJar)
}

fun MavenPublication.addSourcesArtifact(sources: Provider<SourceDirectorySet>) {
    val sourcesJar by tasks.creating(org.gradle.jvm.tasks.Jar::class) {
        archiveClassifier.set("sources")
        from(sources)
    }
    artifact(sourcesJar)
}


fun RepositoryHandler.addSonatypeRepository() {
    maven {
        url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        credentials {
            username = acquireProperty("libs.sonatype.user")
            password = acquireProperty("libs.sonatype.password")
        }
    }
}

fun MavenPublication.signPublicationIfKeyPresent() {
    val keyId = acquireProperty("libs.sign.key.id")
    val signingKey = acquireProperty("libs.sign.key.private")
    val signingKeyPassphrase = acquireProperty("libs.sign.passphrase")
    if (!signingKey.isNullOrBlank()) {
        extensions.configure<SigningExtension>("signing") {
            useInMemoryPgpKeys(keyId, signingKey, signingKeyPassphrase)
            sign(this@signPublicationIfKeyPresent)
        }
    }
}

fun MavenPublication.addMetadata() {
    pom {
        if (!name.isPresent) {
            name.set(artifactId)
        }
        groupId = "org.jetbrains.kotlinx"

        description.set(extension.description)

        url.set("https://github.com/JetBrains/intellij-coverage")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("JetBrains")
                name.set("JetBrains Team")
                organization.set("JetBrains")
                organizationUrl.set("https://www.jetbrains.com")
            }
        }
        scm {
            connection.set("scm:git:git@github.com:JetBrains/intellij-coverage.git")
            developerConnection.set("scm:git:git@github.com:JetBrains/intellij-coverage.git")
            url.set("https://github.com/JetBrains/intellij-coverage")
        }
    }
}


fun Project.acquireProperty(name: String): String? {
    return project.findProperty(name) as? String ?: System.getenv(name)
}

val Project.sourceSets: SourceSetContainer get() =
    (this as ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer

val SourceSetContainer.main: NamedDomainObjectProvider<SourceSet>
    get() = named<SourceSet>("main")

