/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import config.AppConfig
import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import utils.CurrentTaxYear
import v1.mocks.MockCurrentTaxYear
import v1.models.errors._
import v1.models.request.amendOtherDeductions.AmendOtherDeductionsRawData

class AmendOtherDeductionsValidatorSpec extends UnitSpec {

  private val validNino = "AA123456A"
  private val validTaxYear = "2021-22"
  private val requestBodyJson = Json.parse(
    """
      |{
      |    "seafarers":[
      |      {
      |      "customerReference": "SEAFARERS1234",
      |      "amountDeducted": 2342.22,
      |      "nameOfShip": "Blue Bell",
      |      "fromDate": "2018-08-17",
      |      "toDate":"2018-10-02"
      |      }
      |    ]
      |}
      |""".stripMargin
  )

  private val requestBodyJsonNoRef = Json.parse(
    """
      |{
      |    "seafarers":[
      |      {
      |      "amountDeducted": 2342.22,
      |      "nameOfShip": "Blue Bell",
      |      "fromDate": "2018-08-17",
      |      "toDate":"2018-10-02"
      |      }
      |    ]
      |}
      |""".stripMargin
  )

  private val requestBodyJsonMultiple = Json.parse(
    """
      |{
      |    "seafarers":[
      |      {
      |      "customerReference": "SEAFARERS1234",
      |      "amountDeducted": 2342.22,
      |      "nameOfShip": "Blue Bell",
      |      "fromDate": "2018-08-17",
      |      "toDate":"2018-10-02"
      |      },
      |      {
      |      "customerReference": "SEAFARERS64",
      |      "amountDeducted": 189210.19,
      |      "nameOfShip": "Green Wing",
      |      "fromDate": "2019-01-19",
      |      "toDate":"2019-11-22"
      |      }
      |    ]
      |}
      |""".stripMargin
  )

  private val emptyJson = Json.parse(
    """
      |{}
      |""".stripMargin
  )

  class Test extends MockCurrentTaxYear with MockAppConfig {

      implicit val appConfig: AppConfig = mockAppConfig
      implicit val currentTaxYear: CurrentTaxYear = mockCurrentTaxYear

      MockAppConfig.minimumPermittedTaxYear
        .returns(2022)

    val validator = new AmendOtherDeductionsValidator

    "running a validation" should {
      "return no errors" when {
        "a valid request is supplied" in {
          validator.validate(AmendOtherDeductionsRawData(validNino, validTaxYear, requestBodyJson)) shouldBe Nil
        }
        "a valid request is supplied with no customerRef" in {
          validator.validate(AmendOtherDeductionsRawData(validNino, validTaxYear, requestBodyJsonNoRef)) shouldBe Nil
        }
        "a valid request is supplied with multiple objects in the seafarers array" in {
          validator.validate(AmendOtherDeductionsRawData(validNino, validTaxYear, requestBodyJsonMultiple)) shouldBe Nil
        }
      }

      "return a path parameter error" when {
        "an invalid nino is supplied" in {
          validator.validate(AmendOtherDeductionsRawData("AAUH881", validTaxYear, requestBodyJson)) shouldBe List(NinoFormatError)
        }
        "an invalid taxYear is supplied" in {
          validator.validate(AmendOtherDeductionsRawData(validNino, "20319", requestBodyJson)) shouldBe List(TaxYearFormatError)
        }
        "a taxYear with too long of a range is supplied" in {
          validator.validate(AmendOtherDeductionsRawData(validNino, "2018-20", requestBodyJson)) shouldBe List(RuleTaxYearRangeInvalidError)
        }
        "all path parameters are invalid" in {
          validator.validate(AmendOtherDeductionsRawData("AANNAA12", "20319", requestBodyJson)) shouldBe List(NinoFormatError, TaxYearFormatError)
        }
      }

      "return RuleIncorrectOrEmptyBodyError" when {
        "an empty JSON body is submitted" in {
          validator.validate(AmendOtherDeductionsRawData(validNino, validTaxYear, emptyJson)) shouldBe Nil
        }
      }

      "return request body field errors" when {
        "the customer reference is invalid" in {
          val badJson = Json.parse(
            """
              |{
              |    "seafarers":[
              |      {
              |      "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
              |      "amountDeducted": 2342.22,
              |      "nameOfShip": "Blue Bell",
              |      "fromDate": "2018-08-17",
              |      "toDate":"2018-10-02"
              |      }
              |    ]
              |}
              |""".stripMargin
          )

          validator.validate(AmendOtherDeductionsRawData(validNino, validTaxYear, badJson)) shouldBe List(
            CustomerReferenceFormatError.copy(paths = Some(Seq("/seafarers/0/customerReference")))
          )
        }
        "the amountDeduction is invalid" in {
          val badJson = Json.parse(
            """
              |{
              |    "seafarers":[
              |      {
              |      "customerReference": "SEAFARERS1234",
              |      "amountDeducted": 999999999999.99,
              |      "nameOfShip": "Blue Bell",
              |      "fromDate": "2018-08-17",
              |      "toDate":"2018-10-02"
              |      }
              |    ]
              |}
              |""".stripMargin
          )

          validator.validate(AmendOtherDeductionsRawData(validNino, validTaxYear, badJson)) shouldBe List(
            ValueFormatError.copy(paths = Some(Seq("/seafarers/0/amountDeducted")))
          )

        }
        "the nameOfShip is invalid" in {
          val badJson = Json.parse(
            """
              |{
              |    "seafarers":[
              |      {
              |      "customerReference": "SEAFARERS1234",
              |      "amountDeducted": 2342.22,
              |      "nameOfShip": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
              |      "fromDate": "2018-08-17",
              |      "toDate":"2018-10-02"
              |      }
              |    ]
              |}
              |""".stripMargin
          )

          validator.validate(AmendOtherDeductionsRawData(validNino, validTaxYear, badJson)) shouldBe List(
            NameOfShipFormatError.copy(paths = Some(Seq("/seafarers/0/nameOfShip")))
          )

        }
        "the fromDate is invalid" in {
          val badJson = Json.parse(
            """
              |{
              |    "seafarers":[
              |      {
              |      "customerReference": "SEAFARERS1234",
              |      "amountDeducted": 2342.22,
              |      "nameOfShip": "Blue Bell",
              |      "fromDate": "17-08-2012",
              |      "toDate":"2018-10-02"
              |      }
              |    ]
              |}
              |""".stripMargin
          )

          validator.validate(AmendOtherDeductionsRawData(validNino, validTaxYear, badJson)) shouldBe List(
            DateFormatError.copy(paths = Some(Seq("/seafarers/0/fromDate")))
          )

        }
        "the toDate is invalid" in {
          val badJson = Json.parse(
            """
              |{
              |    "seafarers":[
              |      {
              |      "customerReference": "SEAFARERS1234",
              |      "amountDeducted": 2342.22,
              |      "nameOfShip": "Blue Bell",
              |      "fromDate": "2018-08-17",
              |      "toDate":"2018.10.02"
              |      }
              |    ]
              |}
              |""".stripMargin
          )

          validator.validate(AmendOtherDeductionsRawData(validNino, validTaxYear, badJson)) shouldBe List(
            DateFormatError.copy(paths = Some(Seq("/seafarers/0/toDate")))
          )

        }
        "the toDate is before fromDate" in {
          val badJson = Json.parse(
            """
              |{
              |    "seafarers":[
              |      {
              |      "customerReference": "SEAFARERS1234",
              |      "amountDeducted": 2342.22,
              |      "nameOfShip": "Blue Bell",
              |      "fromDate": "2018-10-17",
              |      "toDate":"2018-08-17"
              |      }
              |    ]
              |}
              |""".stripMargin
          )

          validator.validate(AmendOtherDeductionsRawData(validNino, validTaxYear, badJson)) shouldBe List(
            RangeToDateBeforeFromDateError.copy(paths = Some(Seq("/seafarers/0/fromDate", "/seafarers/0/toDate")))
          )

        }
      }

      "return all types of field errors" when {
        "the provided data violates all errors" in {
          val badJson = Json.parse(
            """
              |{
              |    "seafarers":[
              |      {
              |      "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
              |      "amountDeducted": 999999999999.99,
              |      "nameOfShip": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
              |      "fromDate": "17-08-2012",
              |      "toDate":"2018.10.02"
              |      },
              |      {
              |      "customerReference": "AAAAAAAAAABBBBBBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
              |      "amountDeducted": 999999999999.99,
              |      "nameOfShip": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
              |      "fromDate": "17-08-2012",
              |      "toDate":"2018.10.02"
              |      }
              |    ]
              |}
              |""".stripMargin
          )

          validator.validate(AmendOtherDeductionsRawData(validNino, validTaxYear, badJson)) shouldBe List(
            NameOfShipFormatError.copy(paths = Some(Seq("/seafarers/0/nameOfShip", "/seafarers/1/nameOfShip"))),
            CustomerReferenceFormatError.copy(paths = Some(Seq("/seafarers/0/customerReference", "/seafarers/1/customerReference"))),
            ValueFormatError.copy(paths = Some(Seq("/seafarers/0/amountDeducted", "/seafarers/1/amountDeducted"))),
            DateFormatError.copy(paths = Some(Seq("/seafarers/0/fromDate", "/seafarers/0/toDate", "/seafarers/1/fromDate", "/seafarers/1/toDate")))
          )
        }
      }
    }
  }
}
