package model

import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global.ctx
import com.mongodb.casbah.Imports.WriteConcern
import com.mongodb.casbah.MongoConnection
import com.novus.salat.annotations.Key
import utils.MongoHQConfig

case class UserEntity(
  @Key("_id") id: ObjectId,
  emailId: String,
  password: String)

object User {

  /**
   * Create New User
   * @param employer is the employer object to be created
   */
  def createUser(user: UserEntity): Option[ObjectId] = {
    UserDAO.insert(user)
  }

  /**
   * Authenticate User By Credentials Provided
   *
   * @param emailId is emailId of user to be searched
   * @param password is password of user to be searched
   */
  def findUser(emailId: String, password: String): List[UserEntity] = {
    UserDAO.find(MongoDBObject("emailId" -> emailId, "password" -> password)).toList
  }

  /**
   * Find User By User Id
   * @param userId is the id of the user to be searched
   */

  def findUserById(userId: String): Option[UserEntity] = {
    val userFound = UserDAO.find(MongoDBObject("_id" -> new ObjectId(userId))).toList
    (userFound.isEmpty) match {
      case true => None
      case false => Option(userFound.toList(0))
    }

  }
  /**
   * Update user Profile
   * @param user is user object to be updated
   * @param user is password of user to be updated
   */
  def updateUser(user: UserEntity): Unit = {
    UserDAO.update(MongoDBObject("_id" -> user.id), new UserEntity(user.id, user.emailId, user.password), false, false, new WriteConcern)
  }
  /**
   *  Find User By Email 
   *  @param emailId is the emailId of user to be searched
   */

  def findUserByEmail(emailId: String): List[UserEntity] = {
    println(UserDAO.find(MongoDBObject("emailId" -> emailId)).toList.isEmpty)
    UserDAO.find(MongoDBObject("emailId" -> emailId)).toList
  }
}

/**
 * Instantiate UserDAO
 * Access the settings from MongoHQConfig file
 */
object UserDAO extends SalatDAO[UserEntity, ObjectId](collection = MongoHQConfig.mongoDB("user"))