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

import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import v1.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockDeleteOtherDeductionsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.deleteOtherDeductions.DeleteOtherDeductionsRequest

import scala.concurrent.Future

class DeleteOtherDeductionsServiceSpec extends ServiceSpec {

  private val nino    = "AA123456A"
  private val taxYear = "2017-18"

  private val request = DeleteOtherDeductionsRequest(Nino(nino), taxYear)

  trait Test extends MockDeleteOtherDeductionsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new DeleteOtherDeductionsService(
      DeleteOtherDeductionsConnector = mockDeleteOtherDeductionsConnector
    )

  }

  "service" when {
    "service call successsful" must {
      "return mapped result" in new Test {
        MockDeleteOtherDeductionsConnector
          .delete(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.delete(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(ifsErrorCode: String, error: MtdError): Unit =
          s"a $ifsErrorCode error is returned from the service" in new Test {

            MockDeleteOtherDeductionsConnector
              .delete(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

            await(service.delete(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("NO_DATA_FOUND", NotFoundError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
