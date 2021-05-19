#!/system/bin/sh
# Please don't hardcode /magisk/modname/... ; instead, please use $MODDIR/...
# This will make your scripts compatible even if Magisk change its mount point in the future

until [ $(getprop init.svc.bootanim) = "stopped" ]
do
sleep 2s
done
source /data/adb/modules/dozewhitelist/log.sh

#扫描时间隔建议小于等于10s，不可以小于0,修改后重启生效，建议为8s
scanInterval=8s

sleep 20s

echo "">/sdcard/Android/doze.log

Log "⚒开始执行doze.sh⚒"


Log "👉开机首次刷新白名单"
#首次刷新白名单
sh /data/adb/modules/dozewhitelist/whitelist.sh

while :
do
   sh /data/adb/modules/dozewhitelist/doze.sh
   sleep $scanInterval
done