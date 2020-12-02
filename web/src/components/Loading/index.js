import loading from './Loading'
export default {
    install(Vue) {
        Vue.directive('Loaded', loading)
    }
}