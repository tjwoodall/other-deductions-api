/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.controllers.validators

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{DateRange, DateRangeResolving, ResolveJsonObject, ResolveNino, ResolveParsedNumber, ResolveTaxYear}
import api.models.domain.TaxYear
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import play.api.libs.json.JsValue
import v1.models.request.createAndAmendOtherDeductions.{CreateAndAmendOtherDeductionsBody, CreateAndAmendOtherDeductionsRequestData, Seafarers}

import javax.inject.Singleton

@Singleton
class CreateAndAmendOtherDeductionsValidatorFactory {

  private val customerRefRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  private val valid = Valid(())

  private val resolveJson = new ResolveJsonObject[CreateAndAmendOtherDeductionsBody]()

  def validator(nino: String, taxYear: String, body: JsValue): Validator[CreateAndAmendOtherDeductionsRequestData] =
    new Validator[CreateAndAmendOtherDeductionsRequestData] {

      def validate: Validated[Seq[MtdError], CreateAndAmendOtherDeductionsRequestData] =
        (
          ResolveNino(nino),
          ResolveTaxYear(TaxYear.minimumTaxYear.year, taxYear, None, None),
          resolveJson(body)
        ).mapN(CreateAndAmendOtherDeductionsRequestData) andThen validateBodyFieldFormat

    }

  private def validateBodyFieldFormat(
      parsed: CreateAndAmendOtherDeductionsRequestData): Validated[Seq[MtdError], CreateAndAmendOtherDeductionsRequestData] = {

    parsed.body.seafarers match {
      case None => Valid(parsed)
      case Some(seafarers) =>
        seafarers.zipWithIndex
          .traverse_ { case (item, index) => validateSeafarers(item, index) }
          .map(_ => parsed)
    }

  }

  private def validateSeafarers(seafarers: Seafarers, arrayIndex: Int): Validated[Seq[MtdError], Unit] = {
    import seafarers._

    (
      validateCustomerReference(customerReference, s"/seafarers/$arrayIndex/customerReference"),
      validateAmountDeducted(amountDeducted, s"/seafarers/$arrayIndex/amountDeducted"),
      validateNameOfShip(nameOfShip, s"/seafarers/$arrayIndex/nameOfShip"),
      validateDates(fromDate, toDate, arrayIndex)
    ).tupled
      .andThen { case (_, _, _, _) => valid }
  }

  private def validateDates(fromDate: String, toDate: String, arrayIndex: Int): Validated[Seq[MtdError], Unit] = {
    val fromPath = s"/seafarers/$arrayIndex/fromDate"
    val toPath   = s"/seafarers/$arrayIndex/toDate"

    object ResolveToFromDateRange extends DateRangeResolving {
      override protected val startDateFormatError: MtdError = DateFormatError.withPath(fromPath)
      override protected val endDateFormatError: MtdError   = DateFormatError.withPath(toPath)

      def apply(value: (String, String), maybeError: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], DateRange] = {
        resolve(value, maybeError, path)
      }

    }

    ResolveToFromDateRange((fromDate, toDate), Some(RangeToDateBeforeFromDateError.withPaths(List(fromPath, toPath))), None).map(_ => ())
  }

  private def validateCustomerReference(customerReference: Option[String], path: String): Validated[Seq[MtdError], Unit] =
    customerReference match {
      case None => valid
      case Some(ref: String) =>
        if (customerRefRegex.matches(ref))
          valid
        else
          Invalid(List(CustomerReferenceFormatError.withPath(path)))
    }

  private val resolveAmountDeducted = ResolveParsedNumber()

  private def validateAmountDeducted(value: BigDecimal, path: String): Validated[Seq[MtdError], Unit] = {
    resolveAmountDeducted(value, path = Some(path)).map(_ => ())
  }

  private def validateNameOfShip(field: String, path: String): Validated[Seq[MtdError], Unit] = {
    if (field.length <= 105)
      valid
    else
      Invalid(List(NameOfShipFormatError.withPath(path)))
  }

}
