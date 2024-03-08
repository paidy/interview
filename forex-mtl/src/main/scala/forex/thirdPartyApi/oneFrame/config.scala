package forex.thirdPartyApi.oneFrame

import sttp.model.Uri
import sttp.client4.quick._

object config {
    object token {
        def get(): String = {
            // TODO: get token from somewhere more secure like env variable
            return "10dc303535874aeccc86a8251e6992f5"
        }
    }
    object endpoint {
        def get(): Uri = {
            // if there are different endpoints for dev and prod, then list them here
            return uri"http://localhost:8000/rates"
        }
    }

}