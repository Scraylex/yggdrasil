plugins {
  java
  `java-library`
  checkstyle
  pmd
  alias(libs.plugins.spotbugs)
}

checkstyle {
  config = resources.text.fromFile("${rootProject.projectDir}/checkstyle.xml")
  toolVersion = libs.versions.checkstyle.get()
}

pmd {
  toolVersion = libs.versions.pmd.get()
  ruleSetConfig = resources.text.fromFile("${rootProject.projectDir}/pmd.xml")
}

spotbugs {
  toolVersion = libs.versions.spotbugs
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
  implementation(project(":yggdrasil-utils"))

  implementation(libs.vertx.core)
  implementation(libs.vertx.web.client)

  implementation(libs.httpcomponents.core)

  implementation(libs.guava)

  compileOnly(libs.spotbugs.annotations)
  pmd(libs.pmd.java)
  pmd(libs.pmd.ant)

  testImplementation(libs.junit)
  testImplementation(libs.vertx.unit)

  testCompileOnly(libs.spotbugs.annotations)
}

tasks {
  spotbugsMain {
    reports.create("html") {
        required.set(true)
    }
  }
  spotbugsTest {
    reports.create("html") {
        required.set(true)
    }
  }
}

