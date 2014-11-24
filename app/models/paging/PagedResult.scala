package models.paging

case class PagedResult[T](result: Seq[T], pageNumber: PageNumber, resultsPerPage: ResultsPerPage, totalResults: TotalResults)

case class PageNumber(page: Int) extends AnyVal
case class ResultsPerPage(resultsPerPage: Int) extends AnyVal
case class TotalResults(totalResults: Int) extends AnyVal
