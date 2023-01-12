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

package v1.controllers

import api.models.domain.TaxYear
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.RetrieveOtherDeductionsFixtures.responseBodyModel
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveOtherDeductionsRequestParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveOtherDeductionsService}
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.hateoas.Method.GET
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveOtherDeductions.{RetrieveOtherDeductionsRawData, RetrieveOtherDeductionsRequest}
import v1.models.response.retrieveOtherDeductions.RetrieveOtherDeductionsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveOtherDeductionsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveOtherDeductionsService
    with MockRetrieveOtherDeductionsRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new RetrieveOtherDeductionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveOtherDeductionsRequestParser,
      service = mockRetrieveOtherDeductionsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  private val nino          = "AA123456A"
  private val taxYear       = "2019-20"
  private val correlationId = "X-123"

  private val rawData     = RetrieveOtherDeductionsRawData(nino, taxYear)
  private val requestData = RetrieveOtherDeductionsRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  private val testHateoasLink = Link(href = s"/individuals/deductions/other/{nino}/{taxYear}", method = GET, rel = "self")

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockRetrieveOtherDeductionsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveOtherDeductionsService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBodyModel))))

        MockHateoasFactory
          .wrap(responseBodyModel, RetrieveOtherDeductionsHateoasData(nino, taxYear))
          .returns(HateoasWrapper(responseBodyModel, Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveOtherDeductionsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }
      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveOtherDeductionsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockRetrieveOtherDeductionsService
              .retrieve(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val errors = List(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        val extraTysErrors = List(
          (RuleTaxYearNotSupportedError, BAD_REQUEST)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
