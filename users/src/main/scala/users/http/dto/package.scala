package users.http

import io.circe.derivation.Configuration

package object dto:
  given Configuration = Configuration.default

  export io.circe.derivation.ConfiguredCodec

  export users.domain.Protocol.*
