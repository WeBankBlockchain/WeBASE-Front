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
import Vue from 'vue'
import Vuex from 'vuex'
import { getLanguage } from '@/lang/index'
import Cookies from 'js-cookie'
Vue.use(Vuex)

const state = {
    creatUserVisible: false, //creat account
    isLogin: 0,
    loading: false,
    language: getLanguage(),
    importRivateKey: false
}
export default new Vuex.Store({
    state,
    getters: {
        not_show(state) {
            return !state.creatUserVisible
        },
        language: state => state.language,
    },
    mutations: {
        switch_creat_user_dialog(state) {
            state.creatUserVisible = !state.creatUserVisible
        },
        switch_import_rivate_key_dialog(state) {
            state.importRivateKey = !state.importRivateKey
        },
        changeLogin(state, status) {
            state.isLogin = status
        },
        showLoading(state) {
            state.loading = true
        },
        hideLoading(state) {
            state.loading = false
        },
        SET_LANGUAGE: (state, language) => {
            state.language = language
            Cookies.set('language', language)
        },
    },
    actions: {
        switch_creat_user_dialog(context) {
            context.commit('switch_creat_user_dialog')
        },
        switch_import_rivate_key_dialog(context) {
            context.commit('switch_import_rivate_key_dialog')
        },
        loginAction({ commit }) {
            commit('changeLogin', 1)
        },
        setLanguage({ commit }, language) {
            commit('SET_LANGUAGE', language)
        },
    }
})