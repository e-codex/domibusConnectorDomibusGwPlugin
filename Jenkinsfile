@Library(['jueulib@feature/build_oc_pipeline', 'ju30lib']) _

import at.gv.brz.justiz3.jenkins.JU30BuildConfig
import at.gv.brz.jueu.job.JuEuConfiguration


def buildConfig = new JU30BuildConfig()
def euJobConfig = new JuEuConfiguration()

buildConfig.mailTo = "stephan.spindler@brz.gv.at"

genericPipeline(buildConfig, euJobConfig)


