#!/system/bin/sh
export TZ=Asia/Shanghai
source /data/adb/modules/dozewhitelist/log.sh

#识别屏幕状态
screen=$(dumpsys window policy | grep "mInputRestricted" | cut -d= -f2)

#Log "运行状态: $screen,DEEP：$(dumpsys deviceidle get deep),FORCE: $(dumpsys deviceidle get force)"

dumpsys deviceidle | grep -q Enabled=true
check=$?

if [[ $screen == true ]]; then
  if [[ $check == 1 ]]; then
    #执行移出电池优化名单
    sh /data/adb/modules/dozewhitelist/whitelist.sh
    sleep 3s
    #level 1 dumpsys deviceidle enable
    #level 2 dumpsys deviceidle enable deep
    #默认开启level 3
    dumpsys deviceidle enable deep
    dumpsys deviceidle force-idle deep

    #修改记得注释掉
    Log "😴熄灭屏幕进入深度 Doze😴"
    #日志
    Log "运行状态: $screen,DEEP：$(dumpsys deviceidle get deep),FORCE: $(dumpsys deviceidle get force)"
  fi
else
  if [[ $check == 0 ]]; then

    dumpsys deviceidle disable all
    dumpsys deviceidle unforce

    Log "🥱点亮屏幕退出深度 Doze🥱"
    #日志
    Log "运行状态: $screen,DEEP：$(dumpsys deviceidle get deep),FORCE: $(dumpsys deviceidle get force)"
  fi
fi
