package forex.components.cache

import forex.services.rates.errors._


trait Algebra {

  def put[A](key:String, value: A) : Boolean

  def get(key:String) : Either[Error, String]
}
