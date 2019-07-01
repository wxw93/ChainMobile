package cn.bupt.util

import scalikejdbc.config.DBs
import scalikejdbc.{DB, SQL}

object ScalikeJDBC {
  def main(args: Array[String]): Unit = {
    // 加载配置
    DBs.setup()
    // 更新使用的是autoCommit
    DB.autoCommit(implicit session=>{
      // SQL里面是普通的sql语句，后面bind里面是语句中"?"的值，update().apply()是执行语句
      SQL("update ScalaJDBCTest set id=? where name=?").bind(1,"hello").update().apply()
    })
    // 插入使用的是localTx
    DB.localTx(implicit session=>{
      SQL("insert into ScalaJDBCTest values(?,?)").bind(8020,"Mechail").update.apply()
    })
    // 删除使用的也是autoCommit
    DB.autoCommit(implicit session=>{
      SQL("delete from ScalaJDBCTest where id=?").bind(1).update().apply()
    })
    // 读取使用的是readOnly
    val a: List[String] = DB.readOnly(implicit session => {
      SQL("select * from ScalaJDBCTest").map(rs => {
        rs.string("name")
      }).list().apply()
    })
    System.out.println(a)
  }
}
