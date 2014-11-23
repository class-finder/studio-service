package models.contact

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
