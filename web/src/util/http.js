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
import Axios from 'axios'
import router from '../router'
import { Message, Loading } from 'element-ui'
// let loading
// function startLoading() {
//     loading = Loading.service({
//         lock: true,
//         text: '加载中...',
//         background: 'rgba(0,0,0,0.7)'
//     })
// }
// function endLoading() {
//     loading.close()
// }
// let needLoadingRequestCount = 0

// export function showFullScreenLoading() {
//     if (needLoadingRequestCount === 0) {
//         startLoading()
//     }
//     needLoadingRequestCount++
// }

// export function tryHideFullScreenLoading() {
//     if (needLoadingRequestCount <= 0) return
//         needLoadingRequestCount--
//     if (needLoadingRequestCount === 0) {
//         endLoading()
//     }
// }


let axiosIns = Axios.create({
    timeout: 30 * 1000
});
// axiosIns.defaults.baseURL = 'http://127.0.0.1:8081';
axiosIns.defaults.headers.post['X-Requested-With'] = 'XMLHttpRequest';
// axiosIns.defaults.headers.get['X-Requested-With'] = 'XMLHttpRequest';
axiosIns.defaults.responseType = 'json';
// axiosIns.defaults.baseURL = 'http:127.0.0.1:8080'
axiosIns.defaults.validateStatus = function () {
    return true
};
//http request 拦截器
axiosIns.interceptors.request.use(
    config => {

        // showFullScreenLoading()
        return config;
    },
    error => {
        return Promise.reject(err);
    }
);
// http response interceptor
axiosIns.interceptors.response.use(
    response => {
        if (response.data && response.data.code === 302000) {
            router.push({
                path: '/',
                query: { redirect: router.currentRoute.fullPath }
            })
        }
        // tryHideFullScreenLoading()
        return response;
    },
    error => {
        if (localStorage.getItem('lang') === "en") {
            error.data = 'Timeout'
        } else {
            error.data = '请求超时'
        }
        // if (error.response) {
        //     switch (error.response.status) {
        //         case 401:
        //             store.commit(types.LOGOUT);
        //             router.replace({
        //                 path: 'login',
        //                 query: {redirect: router.currentRoute.fullPath}
        //             })
        //     }
        // }
        return Promise.reject(error)
    });

export default {
    axiosIns
}
/**post
 * @param options
 * @return {Promise}
 */
export function post(options) {
    return new Promise((resolve, reject) => {
        axiosIns(options).then(response => {
            resolve(response)
        })
            .catch(error => {
                reject(error)
            })
    })
};
/**get
 * @param options
 * @return {Promise}
 */
export function get(options) {
    return new Promise((resolve, reject) => {
        axiosIns(options).then(response => {
            resolve(response)
        })
            .catch(error => {
                reject(error)
            })
    })
};

/**patch
 * @param options
 * @return {Promise}
 */
export function patch(options) {
    return new Promise((resolve, reject) => {
        axiosIns(options).then(response => {
            resolve(response)
        })
            .catch(error => {
                reject(error)
            })
    })
};
/**put
 * @param options
 * @return {Promise}
 */
export function put(options) {
    return new Promise((resolve, reject) => {
        axiosIns(options).then(response => {
            resolve(response)
        })
            .catch(error => {
                reject(error)
            })
    })
};
/**delete
 * @param options
 * @return {Promise}
 */
export function deleted(options) {
    return new Promise((resolve, reject) => {
        axiosIns(options).then(response => {
            resolve(response)
        })
            .catch(error => {
                reject(error)
            })
    })
};
