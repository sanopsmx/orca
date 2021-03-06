/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.orca.clouddriver.tasks.securitygroup

import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus
import com.netflix.spinnaker.orca.api.pipeline.Task
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution
import com.netflix.spinnaker.orca.api.pipeline.TaskResult
import com.netflix.spinnaker.orca.clouddriver.CloudDriverCacheService
import com.netflix.spinnaker.orca.clouddriver.utils.CloudProviderAware

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.Nonnull

@Component
class DeleteSecurityGroupForceRefreshTask implements CloudProviderAware, Task {
  static final String REFRESH_TYPE = "SecurityGroup"

  @Autowired
  CloudDriverCacheService cacheService

  @Nonnull
  @Override
  TaskResult execute(@Nonnull StageExecution stage) {
    String cloudProvider = getCloudProvider(stage)
    String account = getCredentials(stage)

    String vpcId = stage.context.vpcId
    String name = stage.context.securityGroupName
    List<String> regions = stage.context.regions

    regions.each { region ->
      def model = [securityGroupName: name, vpcId: vpcId, region: region, account: account, evict: true]
      cacheService.forceCacheUpdate(cloudProvider, REFRESH_TYPE, model)
    }
    TaskResult.ofStatus(ExecutionStatus.SUCCEEDED)
  }
}
