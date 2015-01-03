package models.contact

import play.api.libs.json.{Json, Writes}

object Address {
  implicit val addressWrites: Writes[Address] = new Writes[Address] {
    def writes(address: Address) = Json.obj(
      "street" -> address.street.map(_.street),
      "city" -> address.city.map(_.city),
      "postalCode" -> address.postCode.map(_.postCode),
      "province" -> address.province.map(_.province),
      "country" -> address.country.map(_.country)
    )
  }
}

case class Address(
                    street: Option[AddressStreet],
                    city: Option[City],
                    province: Option[Province],
                    postCode: Option[PostalCode],
                    country: Option[Country]
                    )

case class AddressStreet(street: String) extends AnyVal
case class City(city: String) extends AnyVal
case class Province(province: String) extends AnyVal
case class PostalCode(postCode: String) extends AnyVal
case class Country(country: String) extends AnyVal
