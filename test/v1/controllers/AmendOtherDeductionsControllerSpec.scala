/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockAmendOtherDeductionsRequestParser
import v1.mocks.services.{MockAmendOtherDeductionsService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.audit.{AuditError, AuditEvent, AuditResponse, DeductionsAuditDetail}
import v1.models.errors._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendOtherDeductions.{AmendOtherDeductionsBody, AmendOtherDeductionsRawData, AmendOtherDeductionsRequest, Seafarers}
import v1.models.response.AmendOtherDeductionsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendOtherDeductionsControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendOtherDeductionsService
    with MockAmendOtherDeductionsRequestParser
    with MockHateoasFactory
    with MockAuditService {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new AmendOtherDeductionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendOtherDeductionsRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  private val nino = "AA123456A"
  private val taxYear = "2019-20"
  private val correlationId = "X-123"
  private val testHateoasLinks = Seq(
    Link(href = s"/individuals/deductions/other/$nino/$taxYear", method = PUT, rel = "amend-deductions-other"),
    Link(href = s"/individuals/deductions/other/$nino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/deductions/other/$nino/$taxYear", method = DELETE, rel = "delete-deductions-other")
  )

  private val requestBodyJson = Json.parse(
    """|
       |{
       |  "seafarers":[
       |    {
       |      "customerReference": "myRef",
       |      "amountDeducted": 2342.22,
       |      "nameOfShip": "Blue Bell",
       |      "fromDate": "2018-08-17",
       |      "toDate":"2018-10-02"
       |    }
       |  ]
       |}
       |""".stripMargin
  )

  private val requestBody = AmendOtherDeductionsBody(
    Some(Seq(Seafarers(
      Some("myRef"),
      2342.22,
      "Blue Bell",
      "2018-08-17",
      "2018-10-02"
    )))
  )

  val responseBody: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/deductions/other/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"amend-deductions-other"
       |      },
       |      {
       |         "href":"/individuals/deductions/other/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      },
       |      {
       |         "href":"/individuals/deductions/other/$nino/$taxYear",
       |         "method":"DELETE",
       |         "rel":"delete-deductions-other"
       |      }
       |   ]
       |}
       |""".stripMargin)

  def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[DeductionsAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendOtherDeductions",
      transactionName = "create-amend-other-deductions",
      detail = DeductionsAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("nino" -> nino, "taxYear" -> taxYear),
        requestBody = requestBody,
        `X-CorrelationId` = correlationId,
        auditResponse = auditResponse
      )
    )

  private val rawData = AmendOtherDeductionsRawData(nino, taxYear, requestBodyJson)
  private val requestData = AmendOtherDeductionsRequest(Nino(nino), taxYear, requestBody)

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockAmendOtherDeductionsRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendOtherDeductionsService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendOtherDeductionsHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestBodyJson))
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(responseBody))
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(requestBodyJson))).once
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendOtherDeductionsRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse, Some(requestBodyJson))).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (ValueFormatError.copy(paths = Some(Seq(
            "seafarers/0/amountDeducted",
            "seafarers/1/amountDeducted"))), BAD_REQUEST),
          (NameOfShipFormatError.copy(paths = Some(Seq(
            "seafarers/0/nameOfShip",
            "seafarers/1/nameOfShip"))), BAD_REQUEST),
          (CustomerReferenceFormatError.copy(paths = Some(Seq(
            "seafarers/0/customerReference",
            "seafarers/1/customerReference"))), BAD_REQUEST),
          (DateFormatError.copy(paths = Some(Seq(
            "seafarers/0/fromDate",
            "seafarers/0/toDate",
            "seafarers/1/fromDate",
            "seafarers/1/toDate"))), BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (RangeToDateBeforeFromDateError.copy(paths = Some(Seq(
            "seafarers/0/fromDate",
            "seafarers/0/toDate",
            "seafarers/1/fromDate",
            "seafarers/1/toDate"))), BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendOtherDeductionsRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockAmendOtherDeductionsService
              .amend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse, Some(requestBodyJson))).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR),
          (TaxYearFormatError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}