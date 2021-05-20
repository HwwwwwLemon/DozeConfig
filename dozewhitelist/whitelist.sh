#!/system/bin/sh
source /data/adb/modules/dozewhitelist/log.sh

#执行移出电池优化名单

if [ -f "/sdcard/Android/doze.conf" ]; then
  source /sdcard/Android/doze.conf
  noDozes=$(pm list packages -e | sed "s/package:/-/g")$whitelist
  dumpsys deviceidle whitelist $noDozes
  check=$?
  Log "执行清理电池优化名单，应用白名单 code:$check"
  if [[ $check == 0 ]]; then
    Log "清理电池优化名单成功!"
  else
    Log "清理电池优化名单成功!"
  fi
else
  Log "未找到doze.conf，Doze白名单优化未执行，请到APP生成白名单或者自行创建白名单。"
fi
