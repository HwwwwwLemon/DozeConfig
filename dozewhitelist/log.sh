#!/system/bin/sh
export TZ=Asia/Shanghai
Log() {
  txt="/sdcard/Android/doze.log"
  echo "$(date '+%Y-%m-%d %H:%M:%S') $1" >> $txt
  
}