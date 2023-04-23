package forex.http.external

package object oneframe {
  type RateClient[F[_]] = oneframe.OneFrameClient[F]
  final val RateHttpClient = oneframe.OneFrameHttpClient
}
