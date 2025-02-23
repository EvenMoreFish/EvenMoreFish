plugins {
    id("com.oheers.evenmorefish.java-conventions")
}

dependencies {
    compileOnly(libs.jooq)
    compileOnly(libs.jooq.codegen)

    implementation(libs.annotations)
}

java.sourceCompatibility = JavaVersion.VERSION_17