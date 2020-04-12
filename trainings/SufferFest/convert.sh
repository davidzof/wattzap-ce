#
# format: time,power level,rpm,speed
#
cat $1 | while read line
do
if  echo $line | egrep -q '^ *#'
then
  echo "#\n${line}"
  continue
fi

field1=$(echo $line | cut -d',' -f1)
RPE=$(echo $line | cut -d',' -f2)
field3=$(echo $line | cut -d',' -f3)

# time
hh=$(echo $field1 | cut -d':' -f1)
mm=$(echo $field1 | cut -d':' -f2)
ss=$(echo $field1 | cut -d':' -f3)

if [ $hh -gt 0 ]
then
  mm=$(expr $mm + 60)
fi

echo -n "${mm}:${ss},"

# PE to Coggan Power
# < 2  1
# 3-4  2
# 5-6  3
# 7-8  4
# 8-9  5
# 10   6
if awk "BEGIN {exit $RPE > 9 ? 0 : 1}"
then
  echo -n 6
else
  if awk "BEGIN {exit $RPE > 8 ? 0 : 1}"
  then
    echo -n 5
  else
    if awk "BEGIN {exit $RPE > 7 ? 0 : 1}"
    then
      echo -n 4
    else
      if awk "BEGIN {exit $RPE > 5 ? 0 : 1}"
      then
        echo -n 3
      else
        if awk "BEGIN {exit $RPE > 3 ? 0 : 1}"
        then
          echo -n 2
        else
          echo -n 1
        fi
      fi
    fi
  fi
fi
echo ",90,30"
done
