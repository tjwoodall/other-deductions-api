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
import v1.mocks.connectors.MockCreateAndAmendOtherDeductionsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.createAndAmendOtherDeductions.{CreateAndAmendOtherDeductionsBody, CreateAndAmendOtherDeductionsRequest, Seafarers}

import scala.concurrent.Future

class CreateAndAmendOtherDeductionsServiceSpec extends ServiceSpec {

  val taxYear = "2018-04-06"
  val nino    = "AA123456A"

  val body = CreateAndAmendOtherDeductionsBody(
    Some(
      Seq(
        Seafarers(
          Some("myRef"),
          2000.99,
          "Blue Bell",
          "2018-04-06",
          "2019-04-06"
        )))
  )

  private val requestData = CreateAndAmendOtherDeductionsRequest(Nino(nino), taxYear, body)

  trait Test extends MockCreateAndAmendOtherDeductionsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateAndAmendOtherDeductionsService(
      connector = mockCreateAndAmendOtherDeductionsConnector
    )

  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockCreateAndAmendOtherDeductionsConnector
          .createAndAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.createAndAmend(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"a $ifsErrorCode error is returned from the service" in new Test {

          MockCreateAndAmendOtherDeductionsConnector
            .createAndAmend(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

          await(service.createAndAmend(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "SERVER_ERROR"              -> DownstreamError,
        "SERVICE_UNAVAILABLE"       -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

}
