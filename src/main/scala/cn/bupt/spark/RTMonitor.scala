package cn.bupt.spark

import com.typesafe.config.ConfigFactory
import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.{KafkaCluster, KafkaUtils}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import scalikejdbc.{DB, SQL}
import scalikejdbc.config.DBs

class RTMonitor {
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
    conf.setMaster("local[*]").setAppName("实时统计")
    val ssc = new StreamingContext(conf,Seconds(2))

    //从kafka消费数据 --程序第一次启动
    // 加载配置信息
    DBs.setup()
    val fromOffsets: Map[TopicAndPartition, Long] = DB.readOnly{implicit session =>
      SQL("select * from streaming_offset_24 where groupid=?").bind(load.getString("kafka.group.id")).map(rs => {
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
    ssc.start()
    ssc.awaitTermination()

  }
}
