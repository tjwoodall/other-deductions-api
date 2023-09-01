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
import api.hateoas.Method.{DELETE, GET, PUT}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.mvc.Result
import v1.controllers.validators.MockRetrieveOtherDeductionsValidatorFactory
import v1.fixtures.RetrieveOtherDeductionsFixtures._
import v1.mocks.services.MockRetrieveOtherDeductionsService
import v1.models.request.retrieveOtherDeductions.RetrieveOtherDeductionsRequestData
import v1.models.response.retrieveOtherDeductions.RetrieveOtherDeductionsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveOtherDeductionsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveOtherDeductionsService
    with MockRetrieveOtherDeductionsValidatorFactory
    with MockHateoasFactory
    with MockAppConfig {

  private val taxYear     = "2019-20"
  private val requestData = RetrieveOtherDeductionsRequestData(Nino(nino), TaxYear.fromMtd(taxYear))

  private val testHateoasLink = Seq(
    Link(
      href = s"/individuals/deductions/other/AA123456A/$taxYear",
      method = PUT,
      rel = "create-and-amend-deductions-other"
    ),
    Link(
      href = s"/individuals/deductions/other/AA123456A/$taxYear",
      method = GET,
      rel = "self"
    ),
    Link(
      href = s"/individuals/deductions/other/AA123456A/$taxYear",
      method = DELETE,
      rel = "delete-deductions-other"
    )
  )

  private val responseJson = responseWithHateoasLinks(taxYear)

  "handleRequest" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveOtherDeductionsService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBodyModel))))

        MockHateoasFactory
          .wrap(responseBodyModel, RetrieveOtherDeductionsHateoasData(nino, taxYear))
          .returns(HateoasWrapper(responseBodyModel, testHateoasLink))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(responseJson)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveOtherDeductionsService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveOtherDeductionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveOtherDeductionsValidatorFactory,
      service = mockRetrieveOtherDeductionsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakeGetRequest)
  }

}
