import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import java.nio.file.Paths

object Publishing {
    const val versionName = "3.0.2"

    const val url = "https://github.com/trbnb/MvvmBase"
    const val gitUrl = "https://github.com/trbnb/MvvmBase.git"
    const val licenseUrl = "https://github.com/trbnb/MvvmBase/blob/master/LICENSE"
    const val groupId = "de.trbnb"

    fun getOssrhUsername(project: Project) = project.rootProject.extra["private_ossrh_user"].toString()
    fun getOssrhPassword(project: Project) = project.rootProject.extra["private_ossrh_password"].toString()

    fun setupSigning(project: Project) {
        project.rootProject.extra["signing.keyId"] = project.rootProject.extra["private_ossrh_signing_keyid"].toString()
        project.rootProject.extra["signing.password"] = project.rootProject.extra["private_ossrh_signing_passphrase"].toString()
        project.rootProject.extra["signing.secretKeyRingFile"] = Paths.get(project.rootDir.canonicalPath, "signing.gpg")
    }
}

fun PublicationContainer.create(publication: Publication, project: Project) = create<MavenPublication>("release") {
    from(project.components["release"])

    project.getTasksByName("sourcesJar", false).forEach { artifact(it) }

    groupId = Publishing.groupId
    artifactId = publication.artifactId
    version = Publishing.versionName

    pom {
        packaging = "aar"

        name.set(publication.artifactId)
        description.set(publication.description)
        url.set(Publishing.url)

        developers {
            developer {
                id.set("trbnb")
                name.set("Thorben Buchta")
                email.set("thorbenbuchta@gmail.com")
                url.set("https://www.trbnb.de")
            }
        }

        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set(Publishing.licenseUrl)
            }
        }

        scm {
            connection.set(Publishing.gitUrl)
            developerConnection.set(Publishing.gitUrl)
            url.set(Publishing.url)
        }
    }
}