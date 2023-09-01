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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import api.models.outcomes.ResponseWrapper
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.hateoas.Method.{DELETE, GET, PUT}
import api.services.MockAuditService
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.controllers.validators.MockCreateAndAmendOtherDeductionsValidatorFactory
import v1.mocks.services._
import v1.models.request.createAndAmendOtherDeductions._
import v1.models.response.createAndAmendOtherDeductions.CreateAndAmendOtherDeductionsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAndAmendOtherDeductionsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateAndAmendOtherDeductionsService
    with MockCreateAndAmendOtherDeductionsValidatorFactory
    with MockHateoasFactory
    with MockAuditService
    with MockAppConfig {

  private val taxYear = "2021-22"

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
       |      "fromDate": "2020-08-17",
       |      "toDate":"2020-10-02"
       |    }
       |  ]
       |}
       |""".stripMargin
  )

  private val requestBody = CreateAndAmendOtherDeductionsBody(
    Some(
      Seq(
        Seafarers(
          Some("myRef"),
          2342.22,
          "Blue Bell",
          "2020-08-17",
          "2020-10-02"
        )))
  )

  val responseBody: JsValue = Json.parse(s"""
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

  private val requestData = CreateAndAmendOtherDeductionsRequestData(Nino(nino), TaxYear.fromMtd(taxYear), requestBody)

  "handleRequest" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAndAmendOtherDeductionsService
          .createAndAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAndAmendOtherDeductionsHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = Some(responseBody),
          maybeAuditResponseBody = Some(responseBody)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAndAmendOtherDeductionsService
          .createAndAmend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking {

    val controller = new CreateAndAmendOtherDeductionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateAndAmendOtherDeductionsValidatorFactory,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestBodyJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendOtherDeductions",
        transactionName = "create-amend-other-deductions",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = "1.0",
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = requestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
