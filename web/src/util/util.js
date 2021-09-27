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
"use strict"
let changeDate = function (date) {
  let newData = new Date(date);
  let Y = newData.getFullYear();
  let M = newData.getMonth() + 1 > 9 ? newData.getMonth() + 1 : "0" + (newData.getMonth() + 1);
  let D = newData.getDate() > 9 ? newData.getDate() : "0" + newData.getDate();
  return Y + "-" + M + "-" + D
};
var _ = require('lodash');
export function date(date) {
  let newData = new Date(date.getTime());
  let Y = newData.getFullYear();
  let M = newData.getMonth() + 1 > 9 ? newData.getMonth() + 1 : "0" + (newData.getMonth() + 1);
  let D = newData.getDate() > 9 ? newData.getDate() : "0" + newData.getDate();
  let h = newData.getHours();
  let m = newData.getMinutes();
  let s = newData.getSeconds();
  return Y + "-" + M + "-" + D + " " + h + ":" + m + ":" + s
}
export function getDate(val) {
  let date = new Date(val)
  let Y = date.getFullYear();
  let M = date.getMonth() + 1 > 9 ? date.getMonth() + 1 : "0" + (date.getMonth() + 1);
  let D = date.getDate() > 9 ? date.getDate() : "0" + date.getDate();
  let h = date.getHours();
  let m = date.getMinutes();
  let s = date.getSeconds();
  return Y + "-" + M + "-" + D + " " + h + ":" + m + ":" + s
}

export function changWeek(data) {

  let lastDate = (new Date()).getTime();
  let firstDate = lastDate - 6 * 24 * 3600 * 1000;
  let dateList = [];
  dateList[0] = {};
  dateList[0].transCount = 0;
  dateList[0].day = firstDate;
  for (let i = 1; i < 7; i++) {
    dateList[i] = {};
    dateList[i].day = dateList[i - 1].day + 24 * 3600 * 1000;
    dateList[i].transCount = 0;
  };
  for (let i = 0; i < 7; i++) {
    dateList[i].day = changeDate(dateList[i].day)
  }
  dateList.forEach(function (value) {
    if (data && data.length) {
      for (let j = 0; j < data.length; j++) {
        if (value.day === data[j].day) {
          value.transCount = data[j].transCount
        }
      }
    }
  });
  return dateList;
}
/**Get request parameter processing
 * @param necessary Required
 * @param query Optional
 * @return {Object}
 */
export function reviseParam(necessary, query) {
  let params = arguments[0],
    querys = arguments[1],
    arr = [],
    str = '';
  for (var i in params) {
    arr.push(params[i])
  }
  str = arr.join('/');

  return {
    str,
    querys
  }
}


/** get cookie*/
export function getCookie(name) {
  var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
  if (arr = document.cookie.match(reg))
    return (arr[2]);
  else
    return null;
}

/**set cookie*/
export function setCookie(c_name, value, expiredays) {
  var exdate = new Date();
  exdate.setDate(exdate.getDate() + expiredays);
  document.cookie = c_name + "=" + escape(value) + ((expiredays == null) ? "" : ";expires=" + exdate.toGMTString());
};

/**delete cookie*/
export function delCookie(name) {
  var exp = new Date();
  exp.setTime(exp.getTime() - 1);
  var cval = getCookie(name);
  if (cval != null)
    document.cookie = name + "=" + cval + ";expires=" + exp.toGMTString();
};

/**
 * format Timestamp example："2018-01-17 15:39:34"
 * @param d Timestamp
 * @param fmt "yyyy-MM-dd" || "yyyy-MM-dd HH:mm:ss"
 * @returns {string}
 */
export function format(d, fmt) {
  let date = {};
  if (!(d instanceof Date)) {
    date = new Date(parseInt(d));
  }
  let o = {
    "M+": date.getMonth() + 1, //month
    "d+": date.getDate(), //day 
    "H+": date.getHours(), //hour
    "m+": date.getMinutes(), //minute 
    "s+": date.getSeconds(), //second 
    "q+": Math.floor((date.getMonth() + 3) / 3), //quarter 
    "S": date.getMilliseconds() //millisecond
  };
  if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
  for (var k in o)
    if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
  return fmt;
}

/**
 * number：formatted number
 * decimals：Keep a few decimals
 * dec_point：decimal point symbol
 * thousands_sep：thousands of symbols
 * */
export function numberFormat(number, decimals, dec_point, thousands_sep) {
  number = (number + '').replace(/[^0-9+-Ee.]/g, '');
  var n = !isFinite(+number) ? 0 : +number,
    prec = !isFinite(+decimals) ? 0 : Math.abs(decimals),
    sep = (typeof thousands_sep === 'undefined') ? ',' : thousands_sep,
    dec = (typeof dec_point === 'undefined') ? '.' : dec_point,
    s = '',
    toFixedFix = function (n, prec) {
      var k = Math.pow(10, prec);
      return '' + Math.ceil(n * k) / k;
    };

  s = (prec ? toFixedFix(n, prec) : '' + Math.round(n)).split('.');
  var re = /(-?\d+)(\d{3})/;
  while (re.test(s[0])) {
    s[0] = s[0].replace(re, "$1" + sep + "$2");
  }

  if ((s[1] || '').length < prec) {
    s[1] = s[1] || '';
    s[1] += new Array(prec - s[1].length + 1).join('0');
  }
  return s.join(dec);
}

export function isNumber(obj) {

  return typeof obj === 'number' && !isNaN(obj);

}

/** 
 * startTime
 * endTime
 * data
 * */
export function completionDateData(startTime, endTime, data) {
  var lastDate = new Date(endTime).getTime()
  var firstDate = new Date(startTime).getTime()
  var len = (lastDate - firstDate) / 86400000;
  var dateList = [];
  dateList[0] = {};
  dateList[0].time = firstDate;
  dateList[0].transCount = 0;
  for (let i = 1; i < len + 1; i++) {
    dateList[i] = {};
    dateList[i].time = dateList[i - 1].time + 24 * 3600 * 1000;
    dateList[i].transCount = 0;
  };
  for (let i = 0; i < len + 1; i++) {
    dateList[i].time = format(dateList[i].time, "MM-dd")
  }
  for (let i = 0; i < dateList.length; i++) {
    for (let j = 0; j < data.length; j++) {
      if (dateList[i]['time'] === data[j]['time']) {
        dateList[i]['transCount'] = data[j]['transCount']
      }
    }
  }
  return dateList;
}


export function unique(array, onlyKey) {
  let result = {},
    finalResult = [],
    oneKey = onlyKey;
  for (let i = 0; i < array.length; i++) {
    result[array[i][oneKey]] = array[i];

  }
  for (let key in result) {
    finalResult.push(result[key]);
  }
  return finalResult;
}
/**
 * array:[]
 * */
export function unique1(array) {
  var tmp = Array.from(new Set(array));
  return tmp;
}
export function isJson(str) {
  if (typeof str == 'string') {
    try {
      var obj = JSON.parse(str);
      if (typeof obj == 'object' && obj) {
        return true;
      } else {
        return false;
      }

    } catch (e) {
      console.log('error：' + str + '!!!' + e);
      return false;
    }
  }
}

export function dataType(type, value) {
  switch (type) {
    case 'bool':
      if ((value === 'true' || value === 'false')) return eval(value.toLowerCase())
      break;
    case 'uint[]':
      try {
        return JSON.parse(value)
      } catch (error) {
        console.log('error：' + value + '!!!' + error);
        return 'error：' + value + '!!!' + error
      }
      break;
    case 'uint8[]':
      try {
        return JSON.parse(value)
      } catch (error) {
        console.log('error：' + value + '!!!' + error);
        return 'error：' + value + '!!!' + error
      }
      break;
    case 'uint32[]':
      try {
        return JSON.parse(value)
      } catch (error) {
        console.log('error：' + value + '!!!' + error);
        return 'error：' + value + '!!!' + error
      }
      break;
    case 'uint128[]':
      try {
        return JSON.parse(value)
      } catch (error) {
        console.log('error：' + value + '!!!' + error);
        return 'error：' + value + '!!!' + error
      }
      break;
    case 'uint256[]':
      try {
        return JSON.parse(value)
      } catch (error) {
        console.log('error：' + value + '!!!' + error);
        return 'error：' + value + '!!!' + error
      }
      break;
    case 'uint8[]':
      try {
        return JSON.parse(value)
      } catch (error) {
        console.log('error：' + value + '!!!' + error);
        return 'error：' + value + '!!!' + error
      }
      break;
    case 'bytes32[]':
      try {
        var value = value.replace(/\^\[.*\]\$/, '')
        return JSON.parse(value)
      } catch (error) {
        console.log('error：' + value + '!!!' + error);
        return 'error：' + value + '!!!' + error
      }
      break;
    case 'address[]':
      try {
        var value = value.replace(/\^\[.*\]\$/, '')
        return JSON.parse(value)
      } catch (error) {
        console.log('error：' + value + '!!!' + error);
        return 'error：' + value + '!!!' + error
      }
      break;
    default:
      try {
        var value = value.replace(/\^\[.*\]\$/, '')
        return value
      } catch (error) {
        console.log('error：' + value + '!!!' + error);
        return 'error：' + value + '!!!' + error
      }
      break;
  }
}

export function getUrlLastValue(url) {
  var index = url.lastIndexOf("\/");
  var str = url.substring(index + 1, url.length);
  var name = str.substring(0, str.lastIndexOf("."))
  return name;
}

export function timestampUrl(url) {
  //  var getTimestamp=Math.random();
  var getTimestamp = new Date().getTime();
  if (url.indexOf("?") > -1) {
    url = url + "&timestamp=" + getTimestamp
  } else {
    url = url + "?timestamp=" + getTimestamp
  }
  return url;
}
