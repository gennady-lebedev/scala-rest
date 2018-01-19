package com.company.rest

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.company.rest.model.Item
import com.company.rest.repository.JdbcRepository
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc._

import scala.concurrent.ExecutionContext

object AppContext extends LazyLogging {
  val config: Config = ConfigFactory.defaultApplication()
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContext = actorSystem.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val session: DBSession = session(config.getConfig("jdbc"))

  val itemsRepository = new JdbcRepository[Item]

  val router = new RootRouter()

  def session(config: Config): DBSession = {
    val driver = config.getString("driverClassName")
    val url = config.getString("jdbcUrl")
    val user = config.getString("username")
    val password = config.getString("password")
    Class.forName(driver)
    ConnectionPool.singleton(url, user, password)
    implicit val session: DBSession = AutoSession
    sql"SELECT 1+1".execute().apply()
    logger.debug("DB Connection started")
    session
  }
}
