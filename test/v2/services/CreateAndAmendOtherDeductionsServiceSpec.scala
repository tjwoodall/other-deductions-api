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

package v2.services

import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.connectors.MockCreateAndAmendOtherDeductionsConnector
import v2.models.request.createAndAmendOtherDeductions.{CreateAndAmendOtherDeductionsBody, CreateAndAmendOtherDeductionsRequestData, Seafarers}

import scala.concurrent.Future

class CreateAndAmendOtherDeductionsServiceSpec extends ServiceSpec {

  val taxYear = "2021-22"
  val nino    = "AA123456A"

  val body = CreateAndAmendOtherDeductionsBody(
    Some(
      Seq(
        Seafarers(
          Some("myRef"),
          2000.99,
          "Blue Bell",
          "2021-04-06",
          "2022-04-06"
        )))
  )

  private val requestData = CreateAndAmendOtherDeductionsRequestData(Nino(nino), TaxYear.fromMtd(taxYear), body)

  trait Test extends MockCreateAndAmendOtherDeductionsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateAndAmendOtherDeductionsService(
      connector = mockCreateAndAmendOtherDeductionsConnector
    )

  }

  "CreateAndAmendOtherDeductionsService" should {
    "CreateAndAmendOtherDeductions" must {
      "return correct result for a success" in new Test {
        MockCreateAndAmendOtherDeductionsConnector
          .createAndAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.createAndAmend(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockCreateAndAmendOtherDeductionsConnector
            .createAndAmend(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.createAndAmend(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
        ("INVALID_TAX_YEAR", TaxYearFormatError),
        ("INCOME_SOURCE_NOT_FOUND", NotFoundError),
        ("INVALID_PAYLOAD", InternalError),
        ("INVALID_CORRELATIONID", InternalError),
        ("BUSINESS_VALIDATION_RULE_FAILURE", InternalError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )

      val extraTysErrors = List(
        ("INVALID_CORRELATION_ID", InternalError),
        ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

}
