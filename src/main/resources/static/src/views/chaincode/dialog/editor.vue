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
    <el-dialog :title="$t('title.detailsTxn')" :visible.sync="editorDialog" @close="modelClose" width="600px" top="10vh">
        <div slot :style="{'height':editorHeight + 'px'}" style="overflow-y:auto">
            <json-viewer :value="transationData" :expand-depth='5' copyable></json-viewer>
        </div>
    </el-dialog>
</template>
<script>
export default {
    name: 'editor',
    props: ['data', 'show'],
    data: function () {
        return {
            editorShow: true,
            aceEditor: null,
            transationData: this.data || null,
            modePath: 'ace/mode/solidity',
            editorDialog: this.show || false,
            editorHeight: ''
        }
    },
    mounted() {
        this.editorHeight = document.body.offsetHeight * 0.75
    },
    methods: {
        modelClose: function () {
            this.$emit('close')
        }
    }
}
</script>

