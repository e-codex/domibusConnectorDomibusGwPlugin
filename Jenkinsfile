@Library(['jueulib@feature/build_oc_pipeline', 'ju30lib@main']) _

import at.gv.brz.justiz3.jenkins.JU30BuildConfig
import at.gv.brz.jueu.config.JuEuConfiguration
import at.gv.brz.jueu.config.JuEuOcBuildConfig


def buildConfig = new JU30BuildConfig()
def euJobConfig = new JuEuConfiguration()

buildConfig.mailTo = "stephan.spindler@brz.gv.at"
buildConfig.mavenSettingsId = "ecodex-maven-settings"

buildConfig.sonarQubeTokenId = "JUEUECODEX_SonarQubeToken"
//buildConfig.sonarqubeProjectKey = "jueucx:domibusConnectorPlugin"
buildConfig.sonarqubeProjectKey = false

JuEuOcBuildConfig ocBuildConfig = new JuEuOcBuildConfig()
ocBuildConfig.containerBuildName = "dc-domibus-gw"
ocBuildConfig.projectDirectory = "gw-with-plugin/target/gw"
ocBuildConfig.dockerFile = "gw-with-plugin/src/main/docker/Dockerfile"

euJobConfig.ocBuildConfigs.add(ocBuildConfig)

genericPipeline(buildConfig, euJobConfig)


