package users.http.validation

import cats.implicits.*

import users.domain.EmailAddress
import users.http.dto.SignupForm

object Validation:

  private val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  def validateEmail(email: EmailAddress): Either[ValidationError, EmailAddress] =
    emailRegex.findFirstIn(email.value) match
      case Some(_) => email.asRight[ValidationError]
      case None => InvalidEmail.asLeft[EmailAddress]

  def validateSignupForm(form: SignupForm): Either[ValidationError, SignupForm] =
    validateEmail(form.emailAddress).map(_ => form)
