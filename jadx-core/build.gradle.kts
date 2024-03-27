plugins {
	id("jadx-library")
}

dependencies {
	api(project(":jadx-plugins:jadx-input-api"))

	implementation("com.google.code.gson:gson:2.10.1")

	// TODO: move resources decoding to separate plugin module
	implementation("com.android.tools.build:aapt2-proto:8.3.0-10880808")
	implementation("com.google.protobuf:protobuf-java") {
		version {
			require("3.25.3") // version 4 conflict with `aapt2-proto`
		}
	}

	testImplementation("org.apache.commons:commons-lang3:3.14.0")

	testImplementation(project(":jadx-plugins:jadx-dex-input"))
	testRuntimeOnly(project(":jadx-plugins:jadx-smali-input"))
	testRuntimeOnly(project(":jadx-plugins:jadx-java-convert"))
	testRuntimeOnly(project(":jadx-plugins:jadx-java-input"))
	testRuntimeOnly(project(":jadx-plugins:jadx-raung-input"))

	testImplementation("org.eclipse.jdt:ecj") {
		version {
			prefer("3.33.0")
			strictly("[3.33, 3.34[") // from 3.34 compiled with Java 17
		}
	}
	testImplementation("tools.profiler:async-profiler:3.0")
}

tasks.test {
	exclude("**/tmp/*")
}
