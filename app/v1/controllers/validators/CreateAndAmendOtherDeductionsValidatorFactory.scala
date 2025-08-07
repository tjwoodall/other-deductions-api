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

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import common.controllers.validators.resolvers.ResolveDateRange
import common.errors.{CustomerReferenceFormatError, DateFormatError, NameOfShipFormatError, RangeToDateBeforeFromDateError}
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers._
import shared.models.errors.MtdError
import v1.models.request.createAndAmendOtherDeductions.{CreateAndAmendOtherDeductionsBody, CreateAndAmendOtherDeductionsRequestData, Seafarers}

import javax.inject.Singleton

@Singleton
class CreateAndAmendOtherDeductionsValidatorFactory {

  private val customerRefRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  private val valid = Valid(())

  private val resolveJson = new ResolveJsonObject[CreateAndAmendOtherDeductionsBody]()

  private val minYear: Int = 1900
  private val maxYear: Int = 2100

  private val resolveTaxYear = ResolveTaxYearMinimum(minimumPermittedTaxYear)

  def validator(nino: String, taxYear: String, body: JsValue): Validator[CreateAndAmendOtherDeductionsRequestData] =
    new Validator[CreateAndAmendOtherDeductionsRequestData] {

      def validate: Validated[Seq[MtdError], CreateAndAmendOtherDeductionsRequestData] =
        (
          ResolveNino(nino),
          resolveTaxYear(taxYear),
          resolveJson(body)
        ).mapN(CreateAndAmendOtherDeductionsRequestData.apply).andThen(validateBodyFieldFormat)

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
      validateDateRange(fromDate, toDate, arrayIndex)
    ).tupled
      .andThen { case (_, _, _, _) => valid }
  }

  private def validateDateRange(fromDate: String, toDate: String, arrayIndex: Int): Validated[Seq[MtdError], Unit] = {
    val fromPath = s"/seafarers/$arrayIndex/fromDate"
    val toPath   = s"/seafarers/$arrayIndex/toDate"

    (
      ResolveIsoDate(fromDate, DateFormatError.withPath(fromPath)),
      ResolveIsoDate(toDate, DateFormatError.withPath(toPath))
    ).tupled
      .andThen { case (fromDate, toDate) =>
        ResolveDateRange.validateRange(fromDate, toDate, RangeToDateBeforeFromDateError.withPaths(List(fromPath, toPath)))
      }
      .andThen(dateRange => ResolveDateRange.validateMaxAndMinDate(minYear, maxYear, dateRange).map(_ => ()))

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
    resolveAmountDeducted(value, path = path).map(_ => ())
  }

  private def validateNameOfShip(field: String, path: String): Validated[Seq[MtdError], Unit] = {
    if (field.length <= 105)
      valid
    else
      Invalid(List(NameOfShipFormatError.withPath(path)))
  }

}
