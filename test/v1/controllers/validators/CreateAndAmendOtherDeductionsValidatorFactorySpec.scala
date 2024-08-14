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
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.request.createAndAmendOtherDeductions._

class CreateAndAmendOtherDeductionsValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val validRequestBodyJson = Json.parse(
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

  private val validRequestBodyJsonNoRef = Json.parse(
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

  private val validRequestBodyJsonMultiple = Json.parse(
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

  private val parsedValidRequestBody         = validRequestBodyJson.as[CreateAndAmendOtherDeductionsBody]
  private val parsedValidRequestBodyNoRef    = validRequestBodyJsonNoRef.as[CreateAndAmendOtherDeductionsBody]
  private val parsedValidRequestBodyMultiple = validRequestBodyJsonMultiple.as[CreateAndAmendOtherDeductionsBody]

  val validatorFactory = new CreateAndAmendOtherDeductionsValidatorFactory()

  private def validator(nino: String, taxYear: String, body: JsValue) = validatorFactory.validator(nino, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "a valid request is supplied" in {
        val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
          validator(validNino, validTaxYear, validRequestBodyJson).validateAndWrapResult()

        result shouldBe Right(
          CreateAndAmendOtherDeductionsRequestData(parsedNino, parsedTaxYear, parsedValidRequestBody)
        )
      }
      "a valid request is supplied with no customerRef" in {
        val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
          validator(validNino, validTaxYear, validRequestBodyJsonNoRef).validateAndWrapResult()

        result shouldBe Right(
          CreateAndAmendOtherDeductionsRequestData(parsedNino, parsedTaxYear, parsedValidRequestBodyNoRef)
        )
      }
      "a valid request is supplied with multiple objects in the seafarers array" in {
        val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
          validator(validNino, validTaxYear, validRequestBodyJsonMultiple).validateAndWrapResult()

        result shouldBe Right(
          CreateAndAmendOtherDeductionsRequestData(parsedNino, parsedTaxYear, parsedValidRequestBodyMultiple)
        )
      }
    }

    "return path parameter error(s)" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
          validator("AAUH881", validTaxYear, validRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }

      "an invalid taxYear is supplied" in {
        val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
          validator(validNino, "20319", validRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        )
      }

      "an invalid taxYear range is supplied" in {
        val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
          validator(validNino, "2018-20", validRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
        )
      }

      "a taxYear preceeding the minimum is supplied" in {
        val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
          validator(validNino, "2017-18", validRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
        )
      }

      "all path parameters are invalid" in {
        val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
          validator("AANNAA12", "20319", validRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError)))
        )
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "an empty JSON body is submitted" in {
        val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
          validator(validNino, validTaxYear, emptyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError)
        )
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

        val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
          validator(validNino, validTaxYear, badJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, CustomerReferenceFormatError.copy(paths = Some(Seq("/seafarers/0/customerReference"))))
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

        val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
          validator(validNino, validTaxYear, badJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, ValueFormatError.copy(paths = Some(Seq("/seafarers/0/amountDeducted"))))
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

        val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
          validator(validNino, validTaxYear, badJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, NameOfShipFormatError.copy(paths = Some(Seq("/seafarers/0/nameOfShip"))))
        )
      }

      "the fromDate is invalid" when {
        val jsonBody = (fromDate: String) =>
          Json.parse(
            s"""
              |{
              |    "seafarers":[
              |      {
              |      "customerReference": "SEAFARERS1234",
              |      "amountDeducted": 2342.22,
              |      "nameOfShip": "Blue Bell",
              |      "fromDate": "$fromDate",
              |      "toDate":"2018-10-02"
              |      }
              |    ]
              |}
              |""".stripMargin
          )

        "the format of the date is wrong" in {
          val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
            validator(validNino, validTaxYear, jsonBody("17-08-2012")).validateAndWrapResult()

          result shouldBe Left(
            ErrorWrapper(correlationId, DateFormatError.copy(paths = Some(Seq("/seafarers/0/fromDate"))))
          )
        }

        "the date is earlier than the minimum fromDate" in {
          val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
            validator(validNino, validTaxYear, jsonBody("1890-08-12")).validateAndWrapResult()

          result shouldBe Left(
            ErrorWrapper(correlationId, StartDateFormatError)
          )
        }
      }

      "the toDate is invalid" when {
        val jsonBody = (toDate: String) =>
          Json.parse(
            s"""
               |{
               |    "seafarers":[
               |      {
               |      "customerReference": "SEAFARERS1234",
               |      "amountDeducted": 2342.22,
               |      "nameOfShip": "Blue Bell",
               |      "fromDate": "2018-10-02",
               |      "toDate": "$toDate"
               |      }
               |    ]
               |}
               |""".stripMargin
          )

        "the format of the to date is wrong" in {
          val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
            validator(validNino, validTaxYear, jsonBody("2018.10.02")).validateAndWrapResult()

          result shouldBe Left(
            ErrorWrapper(correlationId, DateFormatError.copy(paths = Some(Seq("/seafarers/0/toDate"))))
          )
        }

        "the date is later than the allowed toDate" in {
          val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
            validator(validNino, validTaxYear, jsonBody("2101-08-12")).validateAndWrapResult()

          result shouldBe Left(
            ErrorWrapper(correlationId, EndDateFormatError)
          )
        }

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

        val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
          validator(validNino, validTaxYear, badJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RangeToDateBeforeFromDateError.copy(paths = Some(Seq("/seafarers/0/fromDate", "/seafarers/0/toDate"))))
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

        val result: Either[ErrorWrapper, CreateAndAmendOtherDeductionsRequestData] =
          validator(validNino, validTaxYear, badJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(
              ValueFormatError.copy(paths = Some(Seq("/seafarers/0/amountDeducted", "/seafarers/1/amountDeducted"))),
              NameOfShipFormatError.copy(paths = Some(Seq("/seafarers/0/nameOfShip", "/seafarers/1/nameOfShip"))),
              CustomerReferenceFormatError.copy(paths = Some(Seq("/seafarers/0/customerReference", "/seafarers/1/customerReference"))),
              DateFormatError.copy(paths = Some(Seq("/seafarers/0/fromDate", "/seafarers/0/toDate", "/seafarers/1/fromDate", "/seafarers/1/toDate")))
            ))
          )
        )
      }
    }
  }

}
