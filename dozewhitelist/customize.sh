SKIPUNZIP=0
REPLACE="
"
if [[ `find /data/app -name "com.github.hwwwwwlemon.dozeconfig*" -type d` ]]; then
echo " * 已存在Toast依赖"
rm -rf $MODPATH/dozeconfig.apk
else
echo "正在安装DozeConfig"
echo "酷安@Hwwwww"
pm install -r -g $MODPATH/dozeconfig.apk >/dev/null 2>&1
 if [[ $? = 0 ]]; then
   echo " * 安裝成功"
 else
   echo " * 安裝失败"
 fi
rm -rf $MODPATH/dozeconfig.apk
fi

echo ""
echo "  正在创建白名单   "
echo "Path:/Android/doze.conf"
cat $MODPATH/doze.conf > /storage/emulated/0/Android/doze.conf

if [[ $? = 0 ]]; then
echo "******************"
echo "      安装完成    "
echo "******************"
else
echo "******************"
echo "      安装失败    "
echo "******************"
fi
