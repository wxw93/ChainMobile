package cn.bupt.util

import java.text.SimpleDateFormat

object TimeUtiles {
  def caculateRqt(startTime:String,endTime:String):Long ={

    val dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS")

    val st = dateFormat.parse(startTime.substring(0, 17)).getTime
    val et = dateFormat.parse(endTime).getTime

    et - st
  }
}
