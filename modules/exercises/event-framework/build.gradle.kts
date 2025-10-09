dependencies {
    implementation(libs.swissknife.ddd.domain)
    implementation(libs.swissknife.correlation.core.domain)
    implementation(libs.swissknife.logging.standard.slf4j.configuration)

//    implementation(libs.swissknife.protected.value.factory.aes)
//    implementation(libs.swissknife.cryptography.implementation.bouncy.castle)
//    implementation(libs.pillar.protected.value.domain)

    testImplementation(libs.swissknife.ddd.test.utils)
}