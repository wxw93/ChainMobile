package cn.bupt.spark

import cn.bupt.util.{JedisUtils, TimeUtiles}
import com.alibaba.fastjson.JSON
import com.typesafe.config.ConfigFactory
import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaCluster, KafkaUtils}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import scalikejdbc.{DB, SQL}
import scalikejdbc.config.DBs

object RTMonitor {
  // 屏蔽日志
  Logger.getLogger("org.apache").setLevel(Level.WARN)
  def main(args: Array[String]): Unit = {
    val load = ConfigFactory.load()
    // 创建kafka相关参数
    val kafkaParams = Map(
      "metadata.broker.list" -> load.getString("kafka.broker.list"),
      "group.id" -> load.getString("kafka.group.id"),
      "auto.offset.reset" -> "smallest"
    )
    val topics = load.getString("kafka.topics").split(",").toSet

    //创建 StreamingContext
    val conf = new SparkConf()
    conf.set("spark.testing.memory", "2147480000")
    conf.setMaster("local[*]").setAppName("实时统计")
    val ssc = new StreamingContext(conf,Seconds(2))

    //从kafka消费数据 --程序第一次启动
    // 加载配置信息
    DBs.setup()
    val fromOffsets: Map[TopicAndPartition, Long] = DB.readOnly{implicit session =>
      SQL("select * from stream_offset where groupid=?").bind(load.getString("kafka.group.id")).map(rs => {
        (TopicAndPartition(rs.string("topic"), rs.int("partitions")), rs.long("offset"))
      }).list().apply()
    }.toMap


    val stream = if (fromOffsets.isEmpty) { // 假设程序第一次启动
      KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)
    } else {
      var checkedOffset = Map[TopicAndPartition, Long]()
      val kafkaCluster = new KafkaCluster(kafkaParams)
      val earliestLeaderOffsets = kafkaCluster.getEarliestLeaderOffsets(fromOffsets.keySet)
      if (earliestLeaderOffsets.isRight) {
        val topicAndPartitionToOffset = earliestLeaderOffsets.right.get
        // 开始对比
        checkedOffset = fromOffsets.map(owner => {
          val clusterEarliestOffset = topicAndPartitionToOffset.get(owner._1).get.offset
          if (owner._2 >= clusterEarliestOffset) {
            owner
          } else {
            (owner._1, clusterEarliestOffset)
          }
        })
      }
      // 程序非第一次启动
      val messageHandler = (mm: MessageAndMetadata[String, String]) => (mm.key(), mm.message())
      KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, (String, String)](ssc, kafkaParams, checkedOffset, messageHandler)
    }
    /**
      * receiver 接受数据是在Executor端 cache -- 如果使用的窗口函数的话，没必要进行cache, 默认就是cache， WAL ；
      * 如果采用的不是窗口函数操作的话，你可以cache, 数据会放做一个副本放到另外一台节点上做容错
      * direct 接受数据是在Driver端
      */
    // 处理数据 ---- 根据业务--需求
    stream.foreachRDD(rdd => {
      //rdd.foreach(println)
      // rdd.foreach(println)
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      val baseData = rdd.map(t => JSON.parseObject(t._2))
        .filter(_.getString("serviceName").equalsIgnoreCase("reChargeNotifyReq"))
        .map(jsObj => {
          val result = jsObj.getString("bussinessRst")
          val fee: Double = if (result.equals("0000")) jsObj.getDouble("chargefee") else 0
          val isSucc: Double = if (result.equals("0000")) 1 else 0
          val receiveTime = jsObj.getString("receiveNotifyTime")
          val startTime = jsObj.getString("requestId")
          val pCode = jsObj.getString("provinceCode")
          // 消耗时间
          val costime = if (result.equals("0000")) TimeUtiles.caculateRqt(startTime, receiveTime) else 0
          ("A-" + startTime.substring(0, 8), startTime.substring(0, 10), List[Double](1, isSucc, fee, costime.toDouble), pCode, startTime.substring(0, 12))
        })
      // 实时报表 -- 业务概况
      /**
        * 1)统计全网的充值订单量, 充值金额, 充值成功率及充值平均时长.
        */
      baseData.map(t => (t._1, t._3)).reduceByKey((list1, list2) => {
        (list1 zip list2) map(x => x._1 + x._2)
      }).foreachPartition(itr => {

        val client = JedisUtils.getJedis()
        itr.foreach(tp => {
          client.hincrBy(tp._1, "total", tp._2(0).toLong)
          client.hincrBy(tp._1, "succ", tp._2(1).toLong)
          client.hincrByFloat(tp._1, "money", tp._2(2))
          client.hincrBy(tp._1, "timer", tp._2(3).toLong)
          client.expire(tp._1, 60 * 60 * 24 * 2)
        })
        client.close()
      })

      // 每个省份充值成功数据
      baseData.map(t => ((t._2, t._4), t._3)).reduceByKey((list1, list2) => {
        (list1 zip list2) map(x => x._1 + x._2)
      }).foreachPartition(itr => {

        val client = JedisUtils.getJedis()

        itr.foreach(tp => {
          client.hincrBy("P-"+tp._1._1.substring(0, 8), tp._1._2, tp._2(1).toLong)
          client.expire("P-"+tp._1._1.substring(0, 8), 60 * 60 * 24 * 2)
        })
        client.close()
      })

      // 每分钟的数据分布情况统计
      baseData.map(t => ("C-"+t._5, t._3)).reduceByKey((list1, list2) => {
        (list1 zip list2) map(x => x._1 + x._2)
      }).foreachPartition(itr => {

        val client = JedisUtils.getJedis()

        itr.foreach(tp => {
          client.hincrBy(tp._1, "succ", tp._2(1).toLong)
          client.hincrByFloat(tp._1, "money", tp._2(2))
          client.expire(tp._1, 60 * 60 * 24 * 2)
        })
        client.close()
      })

      // 每个小时的数据分布情况统计
      baseData.map(t => ("B-"+t._2, t._3)).reduceByKey((list1, list2) => {
        (list1 zip list2) map(x => x._1 + x._2)
      }).foreachPartition(itr => {

        val client = JedisUtils.getJedis()
        itr.foreach(tp => {
          // B-2017111816
          client.hincrBy(tp._1, "total", tp._2(0).toLong)
          client.hincrBy(tp._1, "succ", tp._2(1).toLong)
          client.expire(tp._1, 60 * 60 * 24 * 2)
        })
        client.close()
      })
      // 记录偏移量
      offsetRanges.foreach(osr => {
        DB.localTx{ implicit session =>
          SQL("REPLACE INTO stream_offset(topic, groupid, partitions, offset) VALUES(?,?,?,?)")
            .bind(osr.topic, load.getString("kafka.group.id"), osr.partition, osr.untilOffset).update().apply()
        }
        // println(s"${osr.topic} ${osr.partition} ${osr.fromOffset} ${osr.untilOffset}")
      })
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
