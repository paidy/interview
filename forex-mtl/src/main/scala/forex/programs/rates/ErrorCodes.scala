package forex.programs.rates

object ErrorCodes {
  final val cachePopulationFailed: String = "FX_CacheNotPopulated"
  final val cacheFetchFailed: String = "FX_CacheFetchFailed"
  final val cacheInitFailed: String = "FX_CachecInitializationFailed"
  final val cacheRefreshFailed: String = "FX_CacheRefreshFailed"
  final val fxRateLookUpFailed: String = "FX_RatesFetchFailed"
  final val internalError: String = "FX_InternalError"
}
