import inputFilter from './inputFilter'

const install = function (Vue) {
    Vue.directive('inputFilter', inputFilter)
}

if (window.Vue) {
    window.inputFilter = inputFilter
    Vue.use(install)
}

inputFilter.install = install
export default inputFilter
