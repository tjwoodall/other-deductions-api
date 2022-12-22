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

package v1.connectors

import mocks.MockAppConfig
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.createAndAmendOtherDeductions.{CreateAndAmendOtherDeductionsBody, CreateAndAmendOtherDeductionsRequest, Seafarers}

import scala.concurrent.Future

class CreateAndAmendOtherDeductionsConnectorSpec extends ConnectorSpec {

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

  class Test extends MockHttpClient with MockAppConfig {

    val connector: CreateAndAmendOtherDeductionsConnector =
      new CreateAndAmendOtherDeductionsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "connector" must {
    val request = CreateAndAmendOtherDeductionsRequest(Nino(nino), taxYear, body)

    "put a body and return 204 no body" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      implicit val hc: HeaderCarrier                   = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
      val requiredIfsHeadersPut: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

      MockedHttpClient
        .put(
          url = s"$baseUrl/income-tax/deductions/$nino/$taxYear",
          config = dummyHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredIfsHeadersPut,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.createAndAmend(request)) shouldBe outcome
    }
  }

}
