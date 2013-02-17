package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms
import play.api.data.Form
import play.api.data.Forms._
import model.UserEntity
import org.bson.types.ObjectId
import model.User
import routes.javascript._

case class LoginForm(email: String, password: String)
case class UserSignUpForm(email: String, password: String)
object Application extends Controller {

  /**
   * Login In Form
   */
  val loginForm = Form(
    Forms.mapping(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 6))(LoginForm.apply)(LoginForm.unapply))

  /**
   * Sign Up Form
   */
  val signupForm: Form[UserSignUpForm] = Form(
    mapping(
      "email" -> email,
      "password" -> tuple(
        "main" -> text(minLength = 6),
        "confirm" -> text).verifying(
          //  Add an additional constraint: both passwords must match
          "Passwords don't match", passwords => passwords._1 == passwords._2)) {
        // Binding: Create a User from the mapping result (ignore the second password and the accept field)
        (email, passwords) => UserSignUpForm(email, passwords._1)
      } {
        // Unbinding: Create the mapping values from an existing User value
        user => Some(user.email, (user.password, ""))
      })

  /**
   * Redirect To index page
   */
  def index = Action {
    Ok(views.html.index(loginForm, "Form Demo in Play2.0"))
  }

  /**
   * Redirect To Sign Up Page
   */
  def siginUpForm = Action {
    Ok(views.html.signUpForm(signupForm, "Sign Up Form"))
  }

  /**
   * Show a simple message
   */
  def printMessage = Action {
    Ok("This is my message")
  }

  /**
   * Authenticate User For Login
   */
  def authenticateUser = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index(errors, "There is some error")),
      logInForm => {
        val email = logInForm.email
        val password = logInForm.password
        val users = User.findUser(email, password)
        users.isEmpty match {
          case true => Ok(views.html.index(loginForm, "Invalid Credentials"))
          case false =>
            val userEntity = users(0)
            Ok(views.html.userDetail(userEntity))
        }
      })

  }
  /**
   * Register a new User
   */
  def createUser = Action { implicit request =>
    signupForm.bindFromRequest.fold(
      errors => BadRequest(views.html.signUpForm(errors, "There is some error")),
      signupForm => {
        User.findUserByEmail(signupForm.email).isEmpty match {
          case false =>
            Ok(views.html.signUpForm(Application.signupForm, "Email Id Already Exist"))
          case true =>
            val userEntity = UserEntity(new ObjectId, signupForm.email, signupForm.password)
            User.createUser(userEntity)
            Ok(views.html.userDetail(userEntity))
        }

      })

  }

  def editUserProfile(userId: String) = Action { implicit request =>
    val userDetail = User.findUserById(userId)
    userDetail match {
      case None => Ok("There is some error")
      case Some(user: UserEntity) =>
        val userDetailForm = signupForm.fill(UserSignUpForm(user.emailId, ""))
        Ok(views.html.edit(userDetailForm, user))
    }

  }

  def editUser(userId: String) = Action { implicit request =>
    signupForm.bindFromRequest.fold(
      errors => BadRequest(views.html.signUpForm(errors, "There is some error")),
      signupForm => {
        val userDetail = User.findUserById(userId)
        val userEntity = UserEntity(userDetail.get.id, signupForm.email, signupForm.password)
        User.updateUser(userEntity)
        Ok(views.html.userDetail(userEntity))
      })
  }

  def isEmailExist(email: String) = Action { implicit request =>
    User.findUserByEmail(email).isEmpty match {
      case true => Ok("false")
      case false => Ok("true")
    }
  }
  //  public static Result javascriptRoutes() {
  //    response().setContentType("text/javascript");
  //    return ok(Routes.javascriptRouter(
  //        "jsRoutes",
  //        routes.javascript.Application.sayHello(),
  //        routes.javascript.Application.sayHelloToString(),
  //        routes.javascript.Application.sayHelloToJson(),
  //        routes.javascript.Application.sayHelloWithJson()));
  //  }

  // Javascript routing
  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")( routes.javascript.Application.isEmailExist
      )).as("text/javascript")
  }
}
