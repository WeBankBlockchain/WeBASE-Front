/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
<template>
    <div>
        <el-dialog :title="$t('title.detailsAbi')" :visible.sync="dialogVisible" :before-close="modelClose" class="dialog-wrapper" width="600px">
            <div >
                <json-viewer
                    :value="blcokContent"
                    :expand-depth=5
                    copyable
                    sort
                    boxed></json-viewer>
                    <div style="text-align: center" v-if="noData">{{$t('text.noData')}}</div>
                </div>
        </el-dialog>
    </div>
</template>
<script>
export default {
    name: "abiDialog",
    props: ["data","show"],
    data: function(){
        return {
            dialogVisible: this.show,
            noData: false,
            blcokContent: null
        }
    },
    mounted: function(){
        let data = {
            list: JSON.parse(this.data)
        }
        this.blcokContent = JSON.parse(this.data)
        this.$nextTick(function () {
            if(this.blcokContent){
                this.noData = false
            }else{
                this.noData = true
            }
        })
    },
    methods: {
        modelClose: function(){
            this.$emit("close")
        }
    }
}
</script>

