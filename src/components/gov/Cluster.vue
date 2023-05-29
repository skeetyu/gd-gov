<template>
    <div>
        <el-collapse v-model="activeNames" style="margin-left: 170px; width: 88%">
            <el-collapse-item title="Pods列表" name="1">
                <el-table :data="pods" :stripe="true" max-height="250" :border="true">
                    <el-table-column prop="metadata.name" label="Pod名称" width="180px"></el-table-column> 
                    <el-table-column prop="metadata.namespace" label="命名空间" width="180px"></el-table-column>
                    <el-table-column prop="metadata.selfLink" label="完整描述" width="360px"></el-table-column>
                    <el-table-column prop="spec.containers[0].image" label="镜像"></el-table-column>
                </el-table>
            </el-collapse-item>

            <el-collapse-item title="Deployments列表" name="2">
                <el-table :data="deployments" :stripe="true" max-height="250" :border="true">
                    <el-table-column prop="metadata.name" label="Deployment名称" width="135px"></el-table-column>
                    <el-table-column prop="metadata.namespace" label="命名空间" width="90px"></el-table-column>
                    <el-table-column prop="spec.replicas" label="负载数量" width="90px"></el-table-column>
                    <el-table-column prop="spec.selector.matchLabels.app" label="app标签" width="90px"></el-table-column>
                    <el-table-column prop="spec.selector.matchLabels.version" label="version标签" width="120px"></el-table-column>
                    <el-table-column prop="metadata.selfLink" label="完整描述" width="390px"></el-table-column>
                    <el-table-column prop="spec.template.spec.containers[0].image" label="镜像" ></el-table-column>
                </el-table>
            </el-collapse-item>

            <el-collapse-item title="StatefulSets列表" name="3">
                <el-table :data="statefulsets" :stripe="true" max-height="250" :border="true">
                    <el-table-column prop="metadata.name" label="StatefulSet名称" width="135px"></el-table-column>
                    <el-table-column prop="metadata.namespace" label="命名空间" width="90px"></el-table-column>
                    <el-table-column prop="spec.replicas" label="负载数量" width="90px"></el-table-column>
                    <el-table-column prop="spec.selector.matchLabels.app" label="app标签" width="90px"></el-table-column>
                    <el-table-column prop="spec.selector.matchLabels.version" label="version标签" width="120px"></el-table-column>
                    <el-table-column prop="metadata.selfLink" label="完整描述" width="390px"></el-table-column>
                    <el-table-column prop="spec.template.spec.containers[0].image" label="镜像" ></el-table-column>
                </el-table>
            </el-collapse-item>

            <el-collapse-item title="Services列表" name="4">
                <el-table :data="services" :stripe="true" max-height="250" :border="true">
                    <el-table-column prop="metadata.name" label="Service名称" width="180px"></el-table-column>
                    <el-table-column prop="metadata.namespace" label="命名空间" width="180px"></el-table-column>
                    <el-table-column prop="spec.selector.app" label="app标签" width="180px"></el-table-column>
                    <el-table-column prop="spec.type" label="类型" width="180px"></el-table-column>
                    <el-table-column prop="metadata.selfLink" label="完整描述"></el-table-column>
                </el-table>
            </el-collapse-item>

            <el-collapse-item title="Gateway信息" name="5">
                <el-table :data="gateway" max-height="250" :border="true">
                    <el-table-column prop="metadata.name" label="Gateway名称" width="180px"></el-table-column>
                    <el-table-column prop="metadata.namespace" label="命名空间" width="180px"></el-table-column>
                    <el-table-column label="网关规则" width="240px">
                        <el-button type="primary" plain size="small" @click="gatewayStrVisible = true" icon="el-icon-tickets">查看详情</el-button>
                    </el-table-column>
                    <el-table-column prop="metadata.selfLink" label="完整描述"></el-table-column>
                </el-table>
                <el-dialog title="网关规则" :visible.sync="gatewayStrVisible" width="60%" height="30%" :before-close="handleDialogClose">
                    <el-input v-model="gatewayStr" autosize type="textarea"></el-input>
                </el-dialog>
            </el-collapse-item>

            <el-collapse-item title="VirtualService信息" name="6">
                <el-button type="primary" plain size="small" @click="handleVSYamlOpen" icon="el-icon-tickets" style="float: left;margin-bottom: 5px;">查看yaml配置</el-button>
                <el-dialog title="VirtualService流量调度规则" :visible.sync="vsYamlVisible" width="60%" height="30%" :before-close="handleDialogClose">
                    <el-input v-model="vsYamlStr" autosize type="textarea"></el-input>
                </el-dialog>

                <el-button type="primary" plain size="small" @click="handleVSConfigOpen" icon="el-icon-edit-outline" style="float: left; margin-left: 10px; margin-bottom: 5px;">负载均衡（流量比例配置）</el-button>
                <el-dialog title="VirtualService负载权重配置" :visible.sync="vsConfigVisible" width="60%" height="30%" :before-close="handleConfigClose">
                    <el-button type="primary" plain size="small" @click="applyVirtualService" icon="el-icon-document-copy" style="float: left; margin-bottom: 5px;">保存配置</el-button>
                    <el-table :data="vsConfig" max-height="250" :border="true">
                        <el-table-column prop="destination.host" label="Host信息" width="180px"></el-table-column>
                        <el-table-column prop="destination.port.number" label="端口" width="180px"></el-table-column>
                        <el-table-column prop="destination.subset" label="子集" width="180px"></el-table-column>
                        <el-table-column prop="weight" label="权重">
                            <template slot-scope="scope">
                                <el-input v-model="scope.row.weight" placeholder="请输入权重比例"></el-input>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-dialog>
                
                <el-button type="primary" plain size="small" @click="applyTemplateVirtualService" v-if="clusterId == 1" icon="el-icon-reading" style="float: left; margin-left: 10px;  margin-bottom: 5px;">生成模板配置</el-button>
                
                <el-table :data="virtualService" max-height="250" :border="true">
                    <el-table-column prop="metadata.name" label="VS名称" width="180px"></el-table-column>
                    <el-table-column prop="metadata.namespace" label="命名空间" width="180px"></el-table-column>
                    <el-table-column prop="spec.http[0].route.length" label="服务子集数量" width="180px"></el-table-column>
                    <el-table-column prop="metadata.selfLink" label="完整描述"></el-table-column>
                </el-table>
            </el-collapse-item>

            <el-collapse-item title="DestinationRule信息" name="7">
                <el-button type="primary" plain size="small" @click="handleDRYamlOpen" icon="el-icon-tickets" style="float: left; margin-bottom: 5px;">查看yaml配置</el-button>
                <el-dialog title="DestinationRule完整配置" :visible.sync="drYamlVisible" :before-close="handleDialogClose">
                    <el-input v-model="drYamlStr" autosize type="textarea"></el-input>
                </el-dialog>

                <el-button type="primary" plain size="small" @click="handleDRGlobalLoadBalancerConfigOpen" icon="el-icon-edit-outline" style="float: left; margin-left:10px; margin-bottom: 5px;">全局负载均衡策略配置</el-button>
                <el-dialog title="全局负载均衡策略配置" :visible.sync="drGlobalLoadBalancerVisible" :before-close="handleDialogClose">
                    <el-radio v-model="drGlobalLoadBalancerConfig" :label="''">关闭</el-radio>
                    <el-radio v-model="drGlobalLoadBalancerConfig" :label="'ROUND_ROBIN'">ROUND_ROBIN</el-radio>
                    <el-radio v-model="drGlobalLoadBalancerConfig" :label="'LEAST_CONN'">LEAST_CONN</el-radio>
                    <el-radio v-model="drGlobalLoadBalancerConfig" :label="'RANDOM'">RANDOM</el-radio>
                    <el-radio v-model="drGlobalLoadBalancerConfig" :label="'PASSTHROUGH'">PASSTHROUGH</el-radio>
                    <el-button type="primary" plain size="small" @click="handleDRGlobalLoadBalancerConfigDone">生效</el-button>
                </el-dialog>

                <el-button type="primary" plain size="small" @click="applyTemplateDestinationRule" v-if="clusterId == 1" icon="el-icon-reading" style="float: left; margin-left:10px; margin-bottom: 5px;">生成模板目标规则</el-button>

                <el-button type="primary" plain size="small" @click="handleDRTemplateFuseConfigOpen" v-if="clusterId == 1" icon="el-icon-reading" style="float: left; margin-left:10px; margin-bottom: 5px;">生成模板熔断配置</el-button>
                <el-dialog title="模板熔断策略配置" :visible.sync="drTemplateFuseConfigVisible" :before-close="handleDRTemplateFuseConfigClose">
                    <div style="height: 150px;">
                        <el-select v-model="drTemplateFuseConfig.index" placeholder="服务子集" style="float: left;">
                            <el-option
                                v-for="item in destinationRule[0].spec.subsets" :key="item.name" :label="item.name" :value="item.name">
                            </el-option>
                        </el-select>
                        <el-select v-model="drTemplateFuseConfig.policy" placeholder="熔断等级" style="float: left; margin-left: 20px;">
                            <el-option
                                v-for="item in drTemplateFusePolicy" :key="item.level" :label="item.description" :value="item.level">
                            </el-option>
                        </el-select>
                        <el-button type="primary" plain size="small" v-if="clusterId == 1" @click="handleDRTemplateFuseConfigDone" style="float: left; margin-left: 20px;">生效</el-button>
                    </div>
                </el-dialog>

                <el-table :data="destinationRule" max-height="500" :border="true">
                    <el-table-column prop="metadata.name" label="DR名称" width="90px"></el-table-column>
                    <el-table-column prop="metadata.namespace" label="命名空间" width="90px"></el-table-column>
                    <el-table-column label="服务子集配置列表" width="720px">
                        <el-table :data="destinationRule[0].spec.subsets" :border="true">
                            <el-table-column prop="name" label="子集名称" width="90px"></el-table-column>
                            <el-table-column prop="labels.version" label="version标签" width="120px"></el-table-column>
                            <el-table-column label="">
                                <template slot-scope="scope">
                                    <el-button type="primary" plain size="small" @click="handleDROverrideLoadBalancerConfigOpen(scope.$index)" icon="el-icon-document-copy" style="float: left;">覆盖负载策略</el-button>
                                    <el-dialog title="覆盖负载均衡策略配置" :visible.sync="drOverrideLoadBalancerVisible" append-to-body :before-close="handleDialogClose">
                                        <el-radio v-model="drOverrideLoadBalancerConfig.policy" :label="''">关闭</el-radio>
                                        <el-radio v-model="drOverrideLoadBalancerConfig.policy" :label="'ROUND_ROBIN'">ROUND_ROBIN</el-radio>
                                        <el-radio v-model="drOverrideLoadBalancerConfig.policy" :label="'LEAST_CONN'">LEAST_CONN</el-radio>
                                        <el-radio v-model="drOverrideLoadBalancerConfig.policy" :label="'RANDOM'">RANDOM</el-radio>
                                        <el-radio v-model="drOverrideLoadBalancerConfig.policy" :label="'PASSTHROUGH'">PASSTHROUGH</el-radio>
                                        <el-button type="primary" plain size="small" @click="handleDROverrideLoadBalancerConfigDone">生效</el-button>
                                    </el-dialog>

                                    <el-button type="primary" plain size="small" v-if="isFuseOpen(scope.row)" @click="handleDRSubsetFuseConfigOpen(scope.$index)" icon="el-icon-edit-outline" style="float: left; margin-left:10px">熔断配置</el-button>
                                    <el-button type="primary" plain size="small" v-if="!isFuseOpen(scope.row)" @click="handleDRSubsetFuseConfigOpen(scope.$index)" icon="el-icon-edit" style="float: left; margin-left:10px">开启熔断</el-button>
                                    <el-dialog title="熔断配置" :visible.sync="drSubsetFuseConfigVisible" append-to-body :before-close="handleDialogClose">
                                        <el-form :ref="drSubsetFuseConfig" :model="drSubsetFuseConfig" label-position="left" label-width="210px">
                                            <el-form-item label="子集名称">{{ drSubsetFuseConfig.name }}</el-form-item>
                                            <el-form-item label="version标签">{{ drSubsetFuseConfig.labels.version }}</el-form-item>
                                            <el-form-item label="tcp最大连接数">
                                                <el-input v-model="drSubsetFuseConfig.trafficPolicy.maxConnections" style="width: 80%"></el-input>
                                            </el-form-item>
                                            <el-form-item label="tcp连接超时时间（单位：ms）">
                                                <el-input v-model="drSubsetFuseConfig.trafficPolicy.connectTimeout" style="width: 80%"></el-input>
                                            </el-form-item>
                                            <el-form-item label="tcp最大重试次数*">
                                                <el-input v-model="drSubsetFuseConfig.trafficPolicy.maxRetries" style="width: 80%"></el-input>
                                            </el-form-item>
                                            <el-form-item label="http1最大排队请求数">
                                                <el-input v-model="drSubsetFuseConfig.trafficPolicy.http1MaxPendingRequests" style="width: 80%"></el-input>
                                            </el-form-item>
                                            <el-form-item label="http单个连接最大请求数量">
                                                <el-input v-model="drSubsetFuseConfig.trafficPolicy.maxRequestsPerConnection" style="width: 80%"></el-input>
                                            </el-form-item>
                                            
                                            <el-form-item label="连续错误次数*">
                                                <el-input v-model="drSubsetFuseConfig.trafficPolicy.consecutiveErrors" style="width: 80%"></el-input>
                                            </el-form-item>
                                            <el-form-item label="连续5xx错误码数量*">
                                                <el-input v-model="drSubsetFuseConfig.trafficPolicy.consecutive5xxErrors" style="width: 80%"></el-input>
                                            </el-form-item>
                                            <el-form-item label="最小熔断时间（单位：s）*">
                                                <el-input v-model="drSubsetFuseConfig.trafficPolicy.baseEjectionTime" style="width: 80%"></el-input>
                                            </el-form-item>
                                            <el-form-item label="熔断时间间隔（单位：s）*">
                                                <el-input v-model="drSubsetFuseConfig.trafficPolicy.interval" style="width: 80%"></el-input>
                                            </el-form-item>
                                            <el-form-item label="最大熔断比例*">
                                                <el-input v-model="drSubsetFuseConfig.trafficPolicy.maxEjectionPercent" style="width: 80%"></el-input>
                                            </el-form-item>
                                        </el-form>
                                        <el-button type="primary" plain size="small" @click="handleDRSubsetFuseConfigDone" icon="el-icon-document-checked">生效</el-button>
                                    </el-dialog>

                                    <el-button type="primary" plain size="small" v-if="isFuseOpen(scope.row)" @click="handleDRSubsetFuseConfigDelete(scope.$index)" icon="el-icon-delete" style="float: left; margin-left:10px">关闭熔断</el-button>
                                    
                                    <el-button type="primary" plain size="small" @click="handleDRSubsetYamlOpen(scope.$index)" icon="el-icon-tickets" style="float:left; margin-left:10px">查看完整配置</el-button>
                                    <el-dialog title="完整配置" :visible.sync="drSubsetYamlVisible" append-to-body :before-close="handleDialogClose">
                                        <el-input v-model="drSubsetYamlStr" autosize type="textarea"></el-input>
                                    </el-dialog>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-table-column>
                    <el-table-column prop="metadata.selfLink" label="完整描述"></el-table-column>
                </el-table>
            </el-collapse-item>

            <el-collapse-item title="限流配置列表" name="8">
                <el-button type="primary" plain size="small" @click="handleEnvoyFilterNewConfigOpen" icon="el-icon-edit" style="float: left; margin-bottom: 5px;">新建限流配置</el-button>
                <el-dialog title="新建限流服务配置" :visible.sync="envoyFilterNewConfigVisible" width="60%" height="30%" :before-close="handleDialogClose">
                                <el-form :ref="envoyFilterConfigForm" :model="envoyFilterConfigForm" label-position="left" label-width="150px">
                                    <el-form-item label="限流服务名称">
                                        <el-input v-model="envoyFilterConfigForm.metadata.name" style="width: 80%"></el-input>
                                    </el-form-item>
                                    <el-form-item label="限流对象app标签">
                                        <el-input v-model="envoyFilterConfigForm.spec.workloadSelector.labels.app" style="width: 80%"></el-input>
                                    </el-form-item>
                                    <el-form-item label="限流对象version标签">
                                        <el-input v-model="envoyFilterConfigForm.spec.workloadSelector.labels.version" style="width: 80%"></el-input>
                                    </el-form-item>
                                    <el-form-item label="令牌数量">
                                        <el-input v-model="envoyFilterConfigForm.spec.configPatches[0].patch.value.typed_config.value.token_bucket.max_tokens" style="width: 80%"></el-input>
                                    </el-form-item>
                                    <el-form-item label="每次填充令牌数">
                                        <el-input v-model="envoyFilterConfigForm.spec.configPatches[0].patch.value.typed_config.value.token_bucket.tokens_per_fill" style="width: 80%"></el-input>
                                    </el-form-item>
                                </el-form>
                                <el-button type="primary" plain size="small" @click="applyEnvoyFilter" icon="el-icon-document-checked">创建</el-button>
                            </el-dialog>
                <el-table :data="envoyFilters" max-height="250" :border="true">
                    <el-table-column prop="metadata.name" label="限流服务名称" width="180px"></el-table-column>
                    <el-table-column prop="metadata.namespace" label="命名空间" width="120px"></el-table-column>
                    <el-table-column prop="spec.workloadSelector.labels.app" label="生效服务app" width="120px"></el-table-column>
                    <el-table-column prop="spec.workloadSelector.labels.version" label="生效服务version" width="150px"></el-table-column>
                    <el-table-column prop="spec.configPatches[0].patch.value.typed_config.value.token_bucket.max_tokens" label="令牌数量" width="120px"></el-table-column>
                    <el-table-column prop="spec.configPatches[0].patch.value.typed_config.value.token_bucket.tokens_per_fill" label="每次填充令牌数" width="150px"></el-table-column>
                    <el-table-column label="操作">
                        <template slot-scope="scope">
                            <el-button type="primary" plain size="small" @click="handleEnvoyFilterDialogOpen(scope.$index)"  icon="el-icon-tickets" style="float: left;">查看完整配置</el-button>
                            <el-dialog title="限流服务完整配置" :visible.sync="envoyFilterDialogVisible" width="60%" height="30%" :before-close="handleDialogClose">
                                <el-input v-model="envoyFilterStr" autosize type="textarea"></el-input>
                            </el-dialog>
                            <el-button type="primary" plain size="small" @click="handleEnvoyFilterConfigOpen(scope.$index)" icon="el-icon-edit-outline" style="float: left; margin-left:10px;">编辑配置</el-button>
                            <el-dialog title="限流服务配置编辑" :visible.sync="envoyFilterConfigVisible" width="60%" height="30%" :before-close="handleDialogClose">
                                <el-form :ref="envoyFilterConfigForm" :model="envoyFilterConfigForm" label-position="left" label-width="210px">
                                    <el-form-item label="限流服务名称">
                                        <el-input v-model="envoyFilterConfigForm.metadata.name" style="width: 80%"></el-input>
                                    </el-form-item>
                                    <el-form-item label="限流对象app标签">
                                        <el-input v-model="envoyFilterConfigForm.spec.workloadSelector.labels.app" style="width: 80%"></el-input>
                                    </el-form-item>
                                    <el-form-item label="限流对象version标签">
                                        <el-input v-model="envoyFilterConfigForm.spec.workloadSelector.labels.version" style="width: 80%"></el-input>
                                    </el-form-item>
                                    <el-form-item label="令牌数量">
                                        <el-input v-model="envoyFilterConfigForm.spec.configPatches[0].patch.value.typed_config.value.token_bucket.max_tokens" style="width: 80%"></el-input>
                                    </el-form-item>
                                    <el-form-item label="每次填充令牌数">
                                        <el-input v-model="envoyFilterConfigForm.spec.configPatches[0].patch.value.typed_config.value.token_bucket.tokens_per_fill" style="width: 80%"></el-input>
                                    </el-form-item>
                                </el-form>
                                <el-button type="primary" plain size="small" @click="applyEnvoyFilter" icon="el-icon-document-checked">生效</el-button>
                            </el-dialog>
                            <el-button type="primary" plain size="small" @click="deleteEnvoyFilter(scope.$index)" icon="el-icon-delete" style="float: left; margin-left:10px;">删除</el-button>
                        </template>
                    </el-table-column>
                    <!-- <el-table-column prop="metadata.selfLink" label="完整描述"></el-table-column> -->
                </el-table>
            </el-collapse-item>
        </el-collapse>
    </div>
</template>

<script>
    export default {
        name: 'Cluster',
        data () {
            return {
                clusterId: 1,
                activeNames: ['6', '7', '8'],
                pods: [
                    {
                        "metadata": {
                            "name": "mysql1-0",
                            "namespace": "default",
                            "selfLink": "/api/v1/namespaces/default/pods/mysql1-0",
                        },
                        "spec": {
                            "containers": [
                                {
                                    "image": "registry.cn-hangzhou.aliyuncs.com/skeet/mysql:8.0.30"
                                }
                            ]
                        }
                    },
                    {
                        "metadata": {
                            "name": "mysql2-0",
                            "namespace": "default",
                            "selfLink": "/api/v1/namespaces/default/pods/mysql2-0",
                        },
                        "spec": {
                            "containers": [
                                {
                                    "image": "registry.cn-hangzhou.aliyuncs.com/skeet/mysql:8.0.30"
                                }
                            ]
                        }
                    },
                    {
                        "metadata": {
                            "name": "mysql3-0",
                            "namespace": "default",
                            "selfLink": "/api/v1/namespaces/default/pods/mysql3-0",
                        },
                        "spec": {
                            "containers": [
                                {
                                    "image": "registry.cn-hangzhou.aliyuncs.com/skeet/mysql:8.0.30"
                                }
                            ]
                        }
                    }
                ],
                deployments: [
                    {
                        "metadata": {
                            "name": "server-v1",
                            "namespace": "default",
                            "selfLink": "/apis/apps/v1/namespaces/default/deployments/server-v1"
                        },
                        "spec": {
                            "replicas": 1,
                            "selector": {
                                "matchLabels": {
                                    "app": "server",
                                    "version": "v1"
                                }
                            },
                            "template": {
                                "spec": {
                                    "containers": [
                                        {
                                            "image": "registry.cn-hangzhou.aliyuncs.com/skeet/music-server:v1"
                                        }
                                    ]
                                }
                            }
                        }
                    },
                    {
                        "metadata": {
                            "name": "server-v2",
                            "namespace": "default",
                            "selfLink": "/apis/apps/v1/namespaces/default/deployments/server-v2"
                        },
                        "spec": {
                            "replicas": 1,
                            "selector": {
                                "matchLabels": {
                                    "app": "server",
                                    "version": "v2"
                                }
                            },
                            "template": {
                                "spec": {
                                    "containers": [
                                        {
                                            "image": "registry.cn-hangzhou.aliyuncs.com/skeet/music-server:v2"
                                        }
                                    ]
                                }
                            }
                        }
                    },
                    {
                        "metadata": {
                            "name": "server-v3",
                            "namespace": "default",
                            "selfLink": "/apis/apps/v1/namespaces/default/deployments/server-v3"
                        },
                        "spec": {
                            "replicas": 1,
                            "selector": {
                                "matchLabels": {
                                    "app": "server",
                                    "version": "v3"
                                }
                            },
                            "template": {
                                "spec": {
                                    "containers": [
                                        {
                                            "image": "registry.cn-hangzhou.aliyuncs.com/skeet/music-server:v1"
                                        }
                                    ]
                                }
                            }
                        }
                    }
                ],
                statefulsets: [
                    {
                        "metadata": {
                            "name": "mysql1",
                            "namespace": "default",
                            "selfLink": "/apis/apps/v1/namespaces/default/statefulsets/mysql1"
                        },
                        "spec": {
                            "replicas": 1,
                            "selector": {
                                "matchLabels": {
                                    "app": "mysql",
                                    "version": "v1"
                                }
                            },
                            "template": {
                                "spec": {
                                    "containers": [
                                        {
                                            "image": "registry.cn-hangzhou.aliyuncs.com/skeet/mysql:8.0.30"
                                        }
                                    ]
                                }
                            }
                        }
                    },
                    {
                        "metadata": {
                            "name": "mysql2",
                            "namespace": "default",
                            "selfLink": "/apis/apps/v1/namespaces/default/statefulsets/mysql2"
                        },
                        "spec": {
                            "replicas": 1,
                            "selector": {
                                "matchLabels": {
                                    "app": "mysql",
                                    "version": "v2"
                                }
                            },
                            "template": {
                                "spec": {
                                    "containers": [
                                        {
                                            "image": "registry.cn-hangzhou.aliyuncs.com/skeet/mysql:8.0.30"
                                        }
                                    ]
                                }
                            }
                        }
                    },
                    {
                        "metadata": {
                            "name": "mysql3",
                            "namespace": "default",
                            "selfLink": "/apis/apps/v1/namespaces/default/statefulsets/mysql3"
                        },
                        "spec": {
                            "replicas": 1,
                            "selector": {
                                "matchLabels": {
                                    "app": "mysql",
                                    "version": "v3"
                                }
                            },
                            "template": {
                                "spec": {
                                    "containers": [
                                        {
                                            "image": "registry.cn-hangzhou.aliyuncs.com/skeet/mysql:8.0.30"
                                        }
                                    ]
                                }
                            }
                        }
                    }
                ],
                services: [
                    {
                        "metadata": {
                            "name": "server",
                            "namespace": "default",
                            "selfLink": "/api/v1/namespaces/default/services/server"
                        },
                        "spec": {
                            "selector": {
                                "app": "server"
                            },
                            "type": "ClusterIP"
                        }
                    },
                    {
                        "metadata": {
                            "name": "mysql",
                            "namespace": "default",
                            "selfLink": "/api/v1/namespaces/default/services/mysql"
                        },
                        "spec": {
                            "selector": {
                                "app": "mysql"
                            },
                            "type": "ClusterIP"
                        }
                    }
                ],
                gateway: [
                    {
                        "metadata": {
                            "name": "server-gateway",
                            "namespace": "default",
                            "selfLink": "/apis/networking.istio.io/v1alpha3/namespaces/default/gateways/server-gateway"
                        },
                        "spec": {
                            "servers": [
                                {
                                    "hosts": [
                                        "*"
                                    ],
                                    "port": {
                                        "name": "http",
                                        "number": 80,
                                        "protocol": "HTTP"
                                    }
                                },
                                {
                                    "hosts": [
                                        "*"
                                    ],
                                    "port": {
                                        "name": "https",
                                        "number": 443,
                                        "protocol": "HTTP"
                                    }
                                }
                            ]
                        }
                    }
                ],
                gatewayStr: '',
                gatewayStrVisible: false,

                virtualService: [
                    {
                        "metadata": {
                            "name": "server",
                            "namespace": "default",
                            "selfLink": "/apis/networking.istio.io/v1alpha3/namespaces/default/virtualservices/server",
                        },
                        "spec": {
                            "gateways": [
                                "server-gateway"
                            ],
                            "hosts": [
                                "*"
                            ],
                            "http": [
                                {
                                    "match": [
                                        {
                                            "port": 443
                                        }
                                    ],
                                    "route": [
                                        {
                                            "destination": {
                                                "host": "server",
                                                "port": {
                                                    "number": 8088
                                                },
                                                "subset": "v1"
                                            },
                                            "weight": 40
                                        },
                                        {
                                            "destination": {
                                                "host": "server",
                                                "port": {
                                                    "number": 8088
                                                },
                                                "subset": "v2"
                                            },
                                            "weight": 20
                                        },
                                        {
                                            "destination": {
                                                "host": "server",
                                                "port": {
                                                    "number": 8088
                                                },
                                                "subset": "v3"
                                            },
                                            "weight": 40
                                        }
                                    ]
                                }
                            ]
                        }
                    }
                ],
                vsConfig: [
                    {
                        "destination": {
                            "host": "server",
                            "port": {
                                "number": 8088
                            },
                            "subset": "v1"
                        },
                        "weight": 40
                    },
                    {
                        "destination": {
                            "host": "server",
                            "port": {
                                "number": 8088
                            },
                            "subset": "v2"
                        },
                        "weight": 20
                    },
                    {
                        "destination": {
                            "host": "server",
                            "port": {
                                "number": 8088
                            },
                            "subset": "v3"
                        },
                        "weight": 40
                    }
                ],
                vsYamlVisible: false,
                vsYamlStr: '',
                vsConfigVisible: false,

                destinationRule: [
                    {
                        "metadata": {
                            "name": "server",
                            "namespace": "default",
                            "selfLink": "/apis/networking.istio.io/v1alpha3/namespaces/default/destinationrules/server",
                        },
                        "spec": {
                            "host": "server",
                            "subsets": [
                                {
                                    "labels": {
                                        "version": "v1"
                                    },
                                    "name": "v1",
                                    "trafficPolicy": {}
                                },
                                {
                                    "labels": {
                                        "version": "v2"
                                    },
                                    "name": "v2",
                                    "trafficPolicy": {
                                        "loadBalancer": {
                                            "simple": "RANDOM"
                                        }
                                    }
                                },
                                {
                                    "labels": {
                                        "version": "v3"
                                    },
                                    "name": "v3",
                                    "trafficPolicy": {
                                        "connectionPool": {
                                            "http": {
                                                "http1MaxPendingRequests": 1,
                                                "maxRequestsPerConnection": 1,
                                                "maxRetries": 5
                                            },
                                            "tcp": {
                                                "connectTimeout": "30ms",
                                                "maxConnections": 1,
                                                "tcpKeepalive": {
                                                    "interval": "75s",
                                                    "time": "7200s"
                                                }
                                            }
                                        },
                                        "outlierDetection": {
                                            "baseEjectionTime": "30s",
                                            "consecutiveErrors": 5,
                                            "consecutive5xxErrors": 5,
                                            "interval": "10s",
                                            "maxEjectionPercent": 80,
                                        },
                                        "loadBalancer": {
                                            "simple": "RANDOM"
                                        }
                                    }
                                }
                            ],
                            "trafficPolicy": {
                                "loadBalancer": {
                                    "simple": "ROUND_ROBIN"
                                }
                            }
                        }
                    }
                ],
                // destinationRule: [],
                drYamlVisible: false,
                drYamlStr: '',

                drGlobalLoadBalancerVisible: false,
                drGlobalLoadBalancerConfig: '',

                drTemplateFuseConfigVisible: false,
                drTemplateFuseConfig: {
                    "index": '',
                    "policy": ''
                },
                drTemplateFusePolicy: [
                    {
                        "level": "0",
                        "description": "强（对事务处理能力要求高的重要业务）"
                    },
                    {
                        "level": "1",
                        "description": "中（对事务处理能力有一定要求的基础业务）"
                    },
                    {
                        "level": "2",
                        "description": "弱（次要业务）"
                    },
                ],

                drOverrideLoadBalancerVisible: false,
                drOverrideLoadBalancerConfig: {
                    "index": '',
                    "policy": ''
                },

                drSubsetFuseConfig: {
                    "index": '',
                    "labels": {
                        "version": ""
                    },
                    "name": "",
                    "trafficPolicy": {
                        "http1MaxPendingRequests": '',
                        "maxRequestsPerConnection": '',
                        "connectTimeout": '',
                        "maxConnections": '',
                        "maxRetries": '',
                    
                        "consecutive5xxErrors": '',
                        "consecutiveErrors": '',
                        "baseEjectionTime": '',
                        "interval": '',
                        "maxEjectionPercent": ''
                    }
                },
                drSubsetFuseConfigVisible: false,

                drSubsetYamlVisible: false,
                drSubsetYamlStr: '',

                envoyFilters: [
                    {
                        "metadata": {
                            "name": "filter-local-ratelimit-svc",
                            "namespace": "istio-system",
                            "selfLink": "/apis/networking.istio.io/v1alpha3/namespaces/istio-system/envoyfilters/filter-local-ratelimit-svc"
                        },
                        "spec": {
                            "configPatches": [
                                {
                                    "patch": {
                                        "value": {
                                            "typed_config": {
                                                "value": {
                                                    "token_bucket": {
                                                        "fill_interval": "60s",
                                                        "max_tokens": 5,
                                                        "tokens_per_fill": 10
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            ],
                            "workloadSelector": {
                                "labels": {
                                    "app": "server"
                                }
                            }
                        }
                    },
                    {
                        "metadata": {
                            "name": "filter-local-ratelimit-svc2",
                            "namespace": "istio-system",
                            "selfLink": "/apis/networking.istio.io/v1alpha3/namespaces/istio-system/envoyfilters/filter-local-ratelimit-svc"
                        },
                        "spec": {
                            "configPatches": [
                                {
                                    "patch": {
                                        "value": {
                                            "typed_config": {
                                                "value": {
                                                    "token_bucket": {
                                                        "fill_interval": "60s",
                                                        "max_tokens": 10,
                                                        "tokens_per_fill": 10
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            ],
                            "workloadSelector": {
                                "labels": {
                                    "app": "server",
                                    "version": "v2"
                                }
                            }
                        }
                    }
                ],
                envoyFilterDialogVisible: false,
                envoyFilterStr: '',
                envoyFilterNewConfigVisible: false,
                envoyFilterConfigVisible: false,
                envoyFilterCurrentIndex: '',
                envoyFilterConfigForm: {
                    "metadata": {
                        "name": '',
                        "namespace": 'istio-system',
                        "selfLink": ''
                    },
                    "spec": {
                        "configPatches": [
                            {
                                "patch": {
                                    "value": {
                                        "typed_config": {
                                            "value": {
                                                "token_bucket": {
                                                    "fill_interval": "60s",
                                                    "max_tokens": '',
                                                    "tokens_per_fill": ''
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        ],
                        "workloadSelector": {
                            "labels": {
                                "app": '',
                                "version": ''
                            }
                        }
                    }
                }
            }
        },
        mounted: function() {
            this.init()
        },
        watch: {
            '$route' (to, from) {
                this.init()
            }
        },
        methods: {
            init() {
                this.clusterId = this.$route.query.clusterId
                this.getPods()
                this.getDeployments()
                this.getStatefulSets()
                this.getServices()
                this.getGateway()
                this.getVirtualService()
                this.getDestinationRule()
                this.getEnvoyFilters()
                this.$forceUpdate()
            },
            handleDialogClose(done) {
                done();
            },
            handleConfigClose(done) {
                this.$confirm('确认关闭？')
                    .then(_ => {
                        done();
                    })
                    .catch(_ => {});
            },

            handleVSConfigOpen() {
                this.vsConfigVisible = true
                this.vsConfig = JSON.parse(JSON.stringify(this.virtualService[0].spec.http[0].route))
            },
            handleVSYamlOpen() {
                this.vsYamlVisible = true
                this.vsYamlStr = JSON.stringify(this.virtualService[0], null, '\t')
            },

            handleDRYamlOpen() {
                this.drYamlVisible = true
                this.drYamlStr = JSON.stringify(this.destinationRule[0], null, '\t')
            },
            handleDRGlobalLoadBalancerConfigOpen( ) {
                this.drGlobalLoadBalancerVisible = true
                var spec = this.destinationRule[0].spec
                if (spec.trafficPolicy) {
                    this.drGlobalLoadBalancerConfig = spec.trafficPolicy.loadBalancer.simple
                } else {
                    this.drGlobalLoadBalancerConfig = ''
                }
            },
            handleDRGlobalLoadBalancerConfigDone() {
                var trafficPolicy = {
                    "loadBalancer": {
                        "simple": ""
                    }
                }
                trafficPolicy.loadBalancer.simple = this.drGlobalLoadBalancerConfig
                this.destinationRule[0].spec.trafficPolicy = trafficPolicy
                this.applyDestinationRule()
            },
            handleDROverrideLoadBalancerConfigOpen(index) {
                this.drOverrideLoadBalancerVisible = true
                this.drOverrideLoadBalancerConfig.index = index
                if (this.destinationRule[0].spec.subsets[index].trafficPolicy.loadBalancer) {
                    this.drOverrideLoadBalancerConfig.policy = this.destinationRule[0].spec.subsets[index].trafficPolicy.loadBalancer.simple
                }
            },
            handleDROverrideLoadBalancerConfigDone() {
                var index = this.drOverrideLoadBalancerConfig.index
                this.destinationRule[0].spec.subsets[index].trafficPolicy.loadBalancer.simple = this.drOverrideLoadBalancerConfig.policy 
                this.applyDestinationRule()
            },
            handleDRTemplateFuseConfigOpen() {
                this.drTemplateFuseConfigVisible = true
            },
            handleDRTemplateFuseConfigClose() {
                this.drTemplateFuseConfigVisible = false
                this.drTemplateFuseConfig.index = ''
                this.drTemplateFuseConfig.policy = ''
            },
            handleDRTemplateFuseConfigDone() {
                this.applyTemplateFusePolicy()
            },

            handleDRSubsetFuseConfigOpen(index) {
                this.drSubsetFuseConfigVisible = true

                var subset = this.destinationRule[0].spec.subsets[index]
                this.drSubsetFuseConfig.index = index
                this.drSubsetFuseConfig.labels.version = subset.labels.version
                this.drSubsetFuseConfig.name = subset.name
                if (subset.trafficPolicy && subset.trafficPolicy.connectionPool) {
                    this.drSubsetFuseConfig.trafficPolicy.maxConnections = subset.trafficPolicy.connectionPool.tcp.maxConnections
                    this.drSubsetFuseConfig.trafficPolicy.connectTimeout = subset.trafficPolicy.connectionPool.tcp.connectTimeout.substring(0, subset.trafficPolicy.connectionPool.tcp.connectTimeout.length - 2)
                    this.drSubsetFuseConfig.trafficPolicy.http1MaxPendingRequests = subset.trafficPolicy.connectionPool.http.http1MaxPendingRequests
                    this.drSubsetFuseConfig.trafficPolicy.maxRequestsPerConnection = subset.trafficPolicy.connectionPool.http.maxRequestsPerConnection
                    this.drSubsetFuseConfig.trafficPolicy.maxRetries = subset.trafficPolicy.connectionPool.tcp.maxRetries
                } else {
                    this.drSubsetFuseConfig.trafficPolicy.maxConnections = ''
                    this.drSubsetFuseConfig.trafficPolicy.connectTimeout = ''
                    this.drSubsetFuseConfig.trafficPolicy.http1MaxPendingRequests = ''
                    this.drSubsetFuseConfig.trafficPolicy.maxRequestsPerConnection = ''
                    this.drSubsetFuseConfig.trafficPolicy.maxRetries = ''
                }
                
                if (subset.trafficPolicy && subset.trafficPolicy.outlierDetection) {
                    this.drSubsetFuseConfig.trafficPolicy.consecutive5xxErrors = subset.trafficPolicy.outlierDetection.consecutive5xxErrors
                    this.drSubsetFuseConfig.trafficPolicy.consecutiveErrors = subset.trafficPolicy.outlierDetection.consecutiveErrors
                    this.drSubsetFuseConfig.trafficPolicy.baseEjectionTime = subset.trafficPolicy.outlierDetection.baseEjectionTime.substring(0, subset.trafficPolicy.outlierDetection.baseEjectionTime.length - 1)
                    this.drSubsetFuseConfig.trafficPolicy.interval = subset.trafficPolicy.outlierDetection.interval.substring(0, subset.trafficPolicy.outlierDetection.interval.length - 1)
                    this.drSubsetFuseConfig.trafficPolicy.maxEjectionPercent = subset.trafficPolicy.outlierDetection.maxEjectionPercent
                } else {
                    this.drSubsetFuseConfig.trafficPolicy.consecutive5xxErrors = ''
                    this.drSubsetFuseConfig.trafficPolicy.consecutiveErrors = ''
                    this.drSubsetFuseConfig.trafficPolicy.baseEjectionTime = ''
                    this.drSubsetFuseConfig.trafficPolicy.interval = ''
                    this.drSubsetFuseConfig.trafficPolicy.maxEjectionPercent = ''
                }
            },
            handleDRSubsetFuseConfigDone() {
                var index = this.drSubsetFuseConfig.index
                var subset = this.destinationRule[0].spec.subsets[index]

                var connectionPool = {
                    "http": {
                        "http1MaxPendingRequests": '',
                        "maxRequestsPerConnection": '',
                        "maxRetries": ''
                    },
                    "tcp": {
                        "connectTimeout": '',
                        "maxConnections": ''
                    }
                }
                var trafficPolicy = this.drSubsetFuseConfig.trafficPolicy
                connectionPool.tcp.maxConnections = trafficPolicy.maxConnections
                connectionPool.tcp.connectTimeout = trafficPolicy.connectTimeout + 'ms'
                connectionPool.http.maxRetries = trafficPolicy.maxRetries
                connectionPool.http.http1MaxPendingRequests = trafficPolicy.http1MaxPendingRequests
                connectionPool.http.maxRequestsPerConnection = trafficPolicy.maxRequestsPerConnection

                var outlierDetection = {
                    "consecutiveErrors": '',
                    "baseEjectionTime": '',
                    "consecutive5xxErrors": '',
                    "interval": '',
                    "maxEjectionPercent": ''
                }
                outlierDetection.consecutive5xxErrors = trafficPolicy.consecutive5xxErrors
                outlierDetection.consecutiveErrors = trafficPolicy.consecutiveErrors
                outlierDetection.baseEjectionTime = trafficPolicy.baseEjectionTime
                outlierDetection.interval = trafficPolicy.interval
                outlierDetection.maxEjectionPercent = trafficPolicy.maxEjectionPercent

                var myTrafficPolicy = {
                    "connectionPool": { },
                    "outlierDetection": { }
                }
                myTrafficPolicy.connectionPool = connectionPool
                myTrafficPolicy.outlierDetection = outlierDetection
                subset.trafficPolicy = myTrafficPolicy

                this.applyDestinationRule()
            },
            handleDRSubsetFuseConfigDelete(index) {
                var subset = this.destinationRule[0].spec.subsets[index]
                if (subset.trafficPolicy.connectionPool) {
                    subset.trafficPolicy.connectionPool.tcp.connectTimeout = ''
                    subset.trafficPolicy.connectionPool.tcp.maxConnections = ''
                    subset.trafficPolicy.connectionPool.http.http1MaxPendingRequests = ''
                    subset.trafficPolicy.connectionPool.http.maxRequestsPerConnection = ''
                    subset.trafficPolicy.connectionPool.http.maxRetries = ''
                }
                if (subset.trafficPolicy.outlierDetection){ 
                    subset.trafficPolicy.outlierDetection.consecutiveErrors = ''
                    subset.trafficPolicy.outlierDetection.consecutive5xxErrors = ''
                    subset.trafficPolicy.outlierDetection.baseEjectionTime = ''
                    subset.trafficPolicy.outlierDetection.interval = ''
                    subset.trafficPolicy.outlierDetection.maxEjectionPercent = ''
                }

                this.applyDestinationRule()
            },
            handleDRSubsetYamlOpen(index) {
                this.drSubsetYamlVisible = true
                this.drSubsetYamlStr = JSON.stringify(this.destinationRule[0].spec.subsets[index], null, '\t')
            },
            
            handleEnvoyFilterDialogOpen(index) {
                this.envoyFilterDialogVisible = true
                this.envoyFilterStr = JSON.stringify(this.envoyFilters[index], null, '\t')
            },
            handleEnvoyFilterConfigOpen(index) {
                this.envoyFilterConfigVisible = true
                this.envoyFilterCurrentIndex = index
                var var1 = this.envoyFilters[index]
                this.envoyFilterConfigForm.metadata.name = var1.metadata.name
                this.envoyFilterConfigForm.spec.workloadSelector.labels.app = var1.spec.workloadSelector.labels.app
                if (var1.spec.workloadSelector.labels.version) {
                    this.envoyFilterConfigForm.spec.workloadSelector.labels.version = var1.spec.workloadSelector.labels.version
                } else {
                    this.envoyFilterConfigForm.spec.workloadSelector.labels.version = ''
                }
                this.envoyFilterConfigForm.spec.configPatches[0].patch.value.typed_config.value.token_bucket.max_tokens = var1.spec.configPatches[0].patch.value.typed_config.value.token_bucket.max_tokens
                this.envoyFilterConfigForm.spec.configPatches[0].patch.value.typed_config.value.token_bucket.tokens_per_fill = var1.spec.configPatches[0].patch.value.typed_config.value.token_bucket.tokens_per_fill
            },
            handleEnvoyFilterNewConfigOpen() {
                this.envoyFilterNewConfigVisible = true
                this.envoyFilterConfigForm.metadata.name = ''
                this.envoyFilterConfigForm.spec.workloadSelector.labels.app = ''
                this.envoyFilterConfigForm.spec.workloadSelector.labels.version = ''
                this.envoyFilterConfigForm.spec.configPatches[0].patch.value.typed_config.value.token_bucket.max_tokens = ''
                this.envoyFilterConfigForm.spec.configPatches[0].patch.value.typed_config.value.token_bucket.tokens_per_fill = ''
            },

            isFuseOpen(subset) {
                var result = false
                if (subset.trafficPolicy && (subset.trafficPolicy.connectionPool || subset.trafficPolicy.outlierDetection)) {
                    result = true
                } 
                return result
            },

            getPods() {
                var _this = this
                var url = '/pods?clusterId=' + _this.clusterId + '&namespace=default&deployment='
                _this.$axios.get(url).then(successResponse => {
                    if (successResponse && successResponse.data.code === 200) {
                        _this.pods = successResponse.data.data
                    } else if (successResponse && successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error("Pods列表查询出错")
                    }
                })
            },
            getDeployments() {
                var _this = this
                var url = '/deployments?clusterId=' + _this.clusterId + '&namespace=default'
                _this.$axios.get(url).then(successResponse => {
                    if (successResponse && successResponse.data.code === 200) {
                        _this.deployments = successResponse.data.data
                    } else if (successResponse && successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error("Deployments列表查询出错")
                    }
                })
            },
            getStatefulSets() {
                var _this = this
                var url = '/statefulSets?clusterId=' + _this.clusterId + '&namespace=default'
                _this.$axios.get(url).then(successResponse => {
                    if (successResponse && successResponse.data.code === 200) {
                        _this.statefulsets = successResponse.data.data
                    } else if (successResponse && successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error("StatefulSets列表查询出错")
                    }
                })
            },
            getServices() {
                var _this = this
                var url = '/services?clusterId=' + _this.clusterId
                _this.$axios.get(url).then(successResponse => {
                    if (successResponse && successResponse.data.code === 200) {
                        _this.services = successResponse.data.data
                    } else if (successResponse && successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error("Services列表查询出错")
                    }
                })
            },
            getGateway() {
                var _this = this
                var url = '/gateway?clusterId=' + _this.clusterId
                _this.$axios.get(url).then(successResponse => {
                    if (successResponse && successResponse.data.code === 200) {
                        _this.gateway[0] = successResponse.data.data
                        _this.gatewayStr = JSON.stringify(_this.gateway[0].spec, null, '\t')
                    } else if (successResponse && successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error("Gateway信息查询出错")
                    }
                })
            },

            getVirtualService() {
                var _this = this
                var url = '/virtualService?clusterId=' + _this.clusterId
                _this.$axios.get(url).then(successResponse => {
                    if (successResponse && successResponse.data.code === 200) {
                        _this.virtualService[0] = successResponse.data.data
                    } else if (successResponse && successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error("VirtualService信息查询出错")
                    }
                })
            },
            applyVirtualService() {
                var _this = this
                var versions = [ ]
                var weights = [ ]
                for (let i = 0; i < _this.vsConfig.length; ++i) {
                    const version = _this.vsConfig[i].destination.subset
                    const weight = _this.vsConfig[i].weight
                    versions[i] = version
                    weights[i] = weight
                }
                _this.$axios.post('virtualService/apply', {
                    clusterId: _this.clusterId,
                    namespace: _this.virtualService[0].metadata.namespace,
                    vsName: _this.virtualService[0].metadata.name,
                    versions: versions,
                    weights: weights
                }).then(successResponse => {
                    if (successResponse.data.code === 200 && successResponse.data.data === true) {
                        _this.$message.success('负载均衡配置成功！')
                        
                        // _this.vsConfig = JSON.parse(JSON.stringify(this.virtualService[0].spec.http[0].route))
                        _this.virtualService[0].spec.http[0].route = JSON.parse(JSON.stringify(_this.vsConfig))
                        _this.$forceUpdate()
                        // _this.getVirtualService()
                    } else if(successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error('负载均衡配置出错！')
                    }
                })
                // _this.handleVSConfigOpen()
            },
            applyTemplateVirtualService() {
                var _this = this
                _this.$axios.post('virtualService/template', {
                    clusterId: _this.clusterId
                }).then(successResponse => {
                    if (successResponse.data.code === 200 && successResponse.data.data === true) {
                        _this.$message.success('Virtual Service模板配置成功！')
                        _this.getVirtualService()
                        _this.$forceUpdate()
                    } else if(successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error('Virtual Service模板配置出错！')
                    }
                })
            },

            getDestinationRule() {
                var _this = this
                var url = '/destinationRule?clusterId=' + _this.clusterId
                _this.$axios.get(url).then(successResponse => {
                    if (successResponse && successResponse.data.code === 200) {
                        _this.destinationRule[0] = successResponse.data.data
                    } else if (successResponse && successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error("DestinationRule信息查询出错")
                    }
                })
            },
            applyDestinationRule() {
                var _this = this
                var policies = []
                var subsets = _this.destinationRule[0].spec.subsets
                for (let i = 0; i < subsets.length; ++i) {
                    var subset = subsets[i]
                    var policy = {
                        "version": '',
                        "connectionPool": {
                            "tcp_maxConnections": '',
                            "tcp_connectTimeout": '',
                            "http_maxRetries": '',
                            "http_http1MaxPendingRequests": '',
                            "http_maxRequestPerConnection": ''
                        },
                        "outlierDetection": {
                            "consecutiveErrors": '',
                            "baseEjectionTime": '',
                            "consecutive5xxErrors": '',
                            "interval": '',
                            "maxEjectionPercent": ''
                        },
                        "loadBalancer": ''
                    }
                    policy.version = subset.labels.version
                    var trafficPolicy = subset.trafficPolicy
                    if (trafficPolicy && trafficPolicy.connectionPool) {
                        policy.connectionPool.tcp_maxConnections = trafficPolicy.connectionPool.tcp.maxConnections
                        policy.connectionPool.tcp_connectTimeout = trafficPolicy.connectionPool.tcp.connectTimeout.substring(0, trafficPolicy.connectionPool.tcp.connectTimeout.length - 2)
                        policy.connectionPool.http_http1MaxPendingRequests = trafficPolicy.connectionPool.http.http1MaxPendingRequests
                        policy.connectionPool.http_maxRequestPerConnection = trafficPolicy.connectionPool.http.maxRequestsPerConnection
                        policy.connectionPool.http_maxRetries = trafficPolicy.connectionPool.http.maxRetries
                    }
                    if (trafficPolicy && trafficPolicy.outlierDetection) {
                        policy.outlierDetection.consecutiveErrors = trafficPolicy.outlierDetection.consecutiveErrors
                        policy.outlierDetection.consecutive5xxErrors = trafficPolicy.outlierDetection.consecutive5xxErrors
                        policy.outlierDetection.baseEjectionTime = trafficPolicy.outlierDetection.baseEjectionTime.substring(0, trafficPolicy.outlierDetection.baseEjectionTime.length - 1)
                        policy.outlierDetection.interval = trafficPolicy.outlierDetection.interval.substring(0, trafficPolicy.outlierDetection.interval.length - 1)
                        policy.outlierDetection.maxEjectionPercent = trafficPolicy.outlierDetection.maxEjectionPercent
                    }
                    if (trafficPolicy && trafficPolicy.loadBalancer) policy.loadBalancer = trafficPolicy.loadBalancer.simple
                    policies[i] = policy
                }

                var globalLoadBalancer = ''
                if (_this.destinationRule[0].spec.trafficPolicy) {
                    globalLoadBalancer = _this.destinationRule[0].spec.trafficPolicy.loadBalancer.simple
                }

                _this.$axios.post('destinationRule/apply', {
                    clusterId: _this.clusterId,
                    namespace: _this.destinationRule[0].metadata.namespace,
                    drName: _this.destinationRule[0].metadata.name,
                    globalLoadBalancer: globalLoadBalancer,
                    policies: policies
                }).then(successResponse => {
                    if (successResponse.data.code === 200 && successResponse.data.data === true) {
                        _this.$message.success('配置成功！')
                        _this.getDestinationRule()
                        _this.$forceUpdate()
                    } else if(successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error('配置出错！')
                    }
                })
            },
            applyTemplateDestinationRule() {
                var _this = this
                _this.$axios.post('destinationRule/template', {
                    clusterId: _this.clusterId
                }).then(successResponse => {
                    if (successResponse.data.code === 200 && successResponse.data.data === true) {
                        _this.$message.success('Destination Rule模板配置成功！')
                        _this.getDestinationRule()
                        _this.$forceUpdate()
                    } else if(successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error('Destination Rule模板配置出错！')
                    }
                })
            },
            applyTemplateFusePolicy() {
                var _this = this
                _this.$axios.post('destinationRule/fuse/template', {
                    clusterId: _this.clusterId,
                    version: _this.drTemplateFuseConfig.index,
                    level: _this.drTemplateFuseConfig.policy
                }).then(successResponse => {
                    if (successResponse.data.code === 200 && successResponse.data.data === true) {
                        _this.$message.success('模板熔断策略配置成功！')
                        _this.getDestinationRule()
                        _this.$forceUpdate()
                    } else if(successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error('模板熔断策略配置出错！')
                    }
                })

            },

            getEnvoyFilters() {
                var _this = this
                var url = '/envoyFilters?clusterId=' + _this.clusterId
                _this.$axios.get(url).then(successResponse => {
                    if (successResponse && successResponse.data.code === 200) {
                        _this.envoyFilters = successResponse.data.data
                    } else if (successResponse && successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error("限流配置列表查询出错")
                    }
                })
            },
            applyEnvoyFilter() {
                var _this = this
                var labels = {
                    "app": '',
                    "version": ''
                }
                labels.app = _this.envoyFilterConfigForm.spec.workloadSelector.labels.app
                labels.version = _this.envoyFilterConfigForm.spec.workloadSelector.labels.version
                _this.$axios.post('envoyFilter/apply', {
                    clusterId: _this.clusterId,
                    efName: _this.envoyFilterConfigForm.metadata.name,
                    state: true,
                    labels: labels,
                    max_tokens: _this.envoyFilterConfigForm.spec.configPatches[0].patch.value.typed_config.value.token_bucket.max_tokens,
                    tokens_per_fill: _this.envoyFilterConfigForm.spec.configPatches[0].patch.value.typed_config.value.token_bucket.tokens_per_fill
                }).then(successResponse => {
                    if (successResponse.data.code === 200) {
                        _this.$message.success('配置成功！')
                        _this.getEnvoyFilters()
                    } else if(successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error('配置出错！')
                    }
                })
            },
            deleteEnvoyFilter(index) {
                var _this = this
                _this.envoyFilterCurrentIndex = index
                var var1 = _this.envoyFilters[index]
                _this.$axios.post('envoyFilter/apply', {
                    clusterId: _this.clusterId,
                    efName: var1.metadata.name,
                    state: false,
                    labels: var1.spec.workloadSelector.labels,
                    max_tokens: var1.spec.configPatches[0].patch.value.typed_config.value.token_bucket.max_tokens,
                    tokens_per_fill: var1.spec.configPatches[0].patch.value.typed_config.value.token_bucket.tokens_per_fill
                }).then(successResponse => {
                    if (successResponse.data.code === 200) {
                        _this.$message.success('删除成功！')
                        _this.getEnvoyFilters()
                    } else if(successResponse.data.code === 400) {
                        _this.$message.warning(successResponse.data.message)
                    } else {
                        _this.$message.error('删除出错！')
                    }
                })
            }
        }
    }
</script>

<style scoped>

</style>
