package net.tobysullivan.google.geolocation

import play.api.Play
import play.api.Play.current
import play.api.libs.ws._
import play.api.cache.Cache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object GoogleGeolocationClient {
  val apiKey = Play.configuration.getString("google.publicApiKey").getOrElse(throw new Exception("Failed to load config"))
  val apiUrl = "https://maps.googleapis.com/maps/api/geocode/json"

  def getLatLong(address: String): Future[Option[GeoPoint]] = {
    val addressHash = play.api.libs.Codecs.md5(address.getBytes)
    val cacheKey = "geolocation."+addressHash

    // Check cache
    val cached = Cache.getAs[GeoPoint](cacheKey)

    cached.map(point => Future(Some(point))).getOrElse {
      val holder: WSRequestHolder = WS.url(apiUrl)
        .withQueryString(
          "address" -> address,
          "key" -> apiKey
        )
      holder.get().map { response =>
        val oPoint = for (
          geo <- (response.json \ "results" \\ "geometry").headOption;
          loc = geo \ "location";
          lat <- (loc \ "lat").asOpt[Float];
          long <- (loc \ "lng").asOpt[Float]
        ) yield GeoPoint(lat, long)

        // Add to cache
        oPoint.map { point =>
          Cache.set(cacheKey, point)
          point
        }
      }
    }
  }
}
