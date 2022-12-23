/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.services

import api.models.domain.TaxYear
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.fixtures.RetrieveOtherDeductionsFixtures.responseBodyModel
import v1.mocks.connectors.MockRetrieveOtherDeductionsConnector
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveOtherDeductions.RetrieveOtherDeductionsRequest

import scala.concurrent.Future

class RetrieveOtherDeductionsServiceSpec extends ServiceSpec {

  private val nino    = "AA123456A"
  private val taxYear = "2017-18"

  private val requestData = RetrieveOtherDeductionsRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  trait Test extends MockRetrieveOtherDeductionsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveOtherDeductionsService(
      connector = mockRetrieveOtherDeductionsConnector
    )

  }

  "service" should {
    "return a successful response" when {
      "a successful response is passed through" in new Test {
        MockRetrieveOtherDeductionsConnector
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBodyModel))))

        await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, responseBodyModel))
      }
    }

    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveOtherDeductionsConnector
            .retrieve(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
        ("INVALID_TAX_YEAR", TaxYearFormatError),
        ("INVALID_CORRELATIONID", DownstreamError),
        ("NO_DATA_FOUND", NotFoundError),
        ("SERVER_ERROR", DownstreamError),
        ("SERVICE_UNAVAILABLE", DownstreamError)
      )

      val extraTysErrors = List(
        ("INVALID_CORRELATION_ID", DownstreamError),
        ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

}
