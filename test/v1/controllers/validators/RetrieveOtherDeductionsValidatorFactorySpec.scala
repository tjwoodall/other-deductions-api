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

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import config.MockAppConfig
import support.UnitSpec
import v1.models.request.retrieveOtherDeductions.RetrieveOtherDeductionsRequestData

class RetrieveOtherDeductionsValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val validatorFactory = new RetrieveOtherDeductionsValidatorFactory()

  private def validator(nino: String, taxYear: String) = validatorFactory.validator(nino, taxYear)

  "validator" should {
    "return the parsed domain object" when {
      "a valid request is made" in {
        val result: Either[ErrorWrapper, RetrieveOtherDeductionsRequestData] = validator(validNino, validTaxYear).validateAndWrapResult()
        result shouldBe Right(
          RetrieveOtherDeductionsRequestData(parsedNino, parsedTaxYear)
        )
      }
    }

    "return NinoFormatError" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, RetrieveOtherDeductionsRequestData] = validator("A12344A", validTaxYear).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }
    }

    "return TaxYearFormatError" when {
      "an invalid tax year is supplied" in {
        val result: Either[ErrorWrapper, RetrieveOtherDeductionsRequestData] = validator(validNino, "201831").validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        )
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "a taxYear preceeding the minimum is supplied" in {
        val result: Either[ErrorWrapper, RetrieveOtherDeductionsRequestData] = validator(validNino, "2018-19").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
        )
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "the tax year range exceeds 1" in {
        val result: Either[ErrorWrapper, RetrieveOtherDeductionsRequestData] = validator(validNino, "2019-21").validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
        )
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        val result: Either[ErrorWrapper, RetrieveOtherDeductionsRequestData] = validator("A12344A", "20178").validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError)))
        )
      }
    }
  }

}
