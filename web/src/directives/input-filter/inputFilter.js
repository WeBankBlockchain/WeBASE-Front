/**
 *  实现功能
 *  1、默认情况下只禁止空格输入
 *  2、限制只能输入整数
 *  3、限制只能输入整数和小数（价格类）
 *  4、限制只能输入手机号
 *  5、限制最大值和最小值(抛出错误给回调函数)
 */
const addListener = function (el, type, fn) {
    el.addEventListener(type, fn, false)
}
const spaceFilter = function (el) {
    addListener(el, 'keyup', () => {
        el.value = el.value.replace(/\s+/, '')
    })
}
const intFilter = function (el) {
    addListener(el, 'keyup', () => {
        el.value = el.value.replace(/\D/g, '')
    })
}
const priceFilter = function (el) {
    addListener(el, 'keyup', () => {
        el.value = el.value.replace(/[^\d.]*/g, '')
        if (isNaN(el.value)) {
            el.value = ''
        }
    })
}
const specialFilter = function (el) {
    addListener(el, 'keyup', () => {
        el.value = el.value.replace(/[^\a-\z\A-\Z0-9\u4E00-\u9FA5]/g, '')
    })
}
const phoneFilter = function (el) {
    addListener(el, 'blur', () => {
        if (!el.value) {
            return
        }
        const phoneReg = new RegExp('^(13|14|15|16|17|18|19)[0-9]{9}$')
        if (!phoneReg.test(el.value)) {
            alert('手机号输入错误')
            el.value = ''
        }
    })
}
const urlFilter = function (el) {
    addListener(el, 'blur', () => {
        if (!el.value) {
            return
        }
        const urlReg = /(^#)|(^http(s*):\/\/[^\s]+\.[^\s]+)/
        if (!urlReg.test(el.value)) {
            alert('url输入错误')
            el.value = ''
        }
    })
}

export default {
    bind(el, binding) {
        if (el.tagName.toLowerCase() !== 'input') {
            el = el.getElementsByTagName('input')[0]
        }
        spaceFilter(el)
        console.log(binding.arg)
        switch (binding.arg) {
            case 'int':
                intFilter(el)
                break
            case 'price':
                priceFilter(el)
                break
            case 'special':
                specialFilter(el)
                break
            case 'phone':
                phoneFilter(el)
                break
            case 'url':
                urlFilter(el)
                break
            default:
                break
        }
    }
}

