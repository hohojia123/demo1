function timeago(timestamp) { //将时间转化成离现在几分钟，几小时的格式

  if (!timestamp) {
    return '未知时间';
  }

  // 后端返回格式: "2024-01-15 10:30:00"，转换为 "2024/01/15 10:30:00" 兼容所有浏览器
  const date = new Date(timestamp.replace(/-/g, '/'));

  var diffMs = Date.now() - date.getTime();//相差的毫秒数
  var diff = (diffMs) / 1000 / 60 / 60;  //相差的时间 小时数
  if (diff >= 24) {  //时间差大于24个小时 显示标准时间 例：2019.6.5 00:26:11
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hours = date.getHours();
    if (hours < 10)
      hours = '0' + hours;
    var minutes = date.getMinutes();
    if (minutes < 10)
      minutes = '0' + minutes;
    var seconds = date.getSeconds();
    if (seconds < 10)
      seconds = '0' + seconds;

    return year + '.' + month + '.' + day + ' ' + hours + ':' + minutes + ':' + seconds;
  }
  else { //小于24小时 显示 时间差 例：2小时前

    var mistiming = Math.round((diffMs) / 1000);
    var arrr = ['年', '个月', '星期', '天', '小时', '分钟', '秒'];
    var arrn = [31536000, 2592000, 604800, 86400, 3600, 60, 1];
    for (var i = 0; i < arrn.length; i++) {
      var inm = Math.floor(mistiming / arrn[i]);
      if (arrr[i] == arrr[6]) {
        if (inm < 1) {
          return '刚刚';
        }
      }
      if (inm != 0)
        return inm + arrr[i] + '前';
    }
  }
}

function format(date) {

  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  let hours = date.getHours();
  if (hours < 10)
    hours = '0' + hours;
  let minutes = date.getMinutes();
  if (minutes < 10)
    minutes = '0' + minutes;
  let seconds = date.getSeconds();
  if (seconds < 10)
    seconds = '0' + seconds;
  let ms = date.getMilliseconds();
  if (ms < 10) {
    ms = '00' + ms;
  } else if (ms < 100) {
    ms = '0' + ms;
  }

  return year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds + ':' + ms;
}


export default {
  timeago,
  format
}
