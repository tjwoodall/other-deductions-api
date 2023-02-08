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

package v1.models.response.retrieveOtherDeductions

import api.models.hateoas
import api.models.hateoas.Method
import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v1.fixtures.RetrieveOtherDeductionsFixtures._

class RetrieveOtherDeductionsResponseSpec extends UnitSpec with MockAppConfig {

  val multipleSeafarersRetrieveOtherDeductionsResponse: RetrieveOtherDeductionsResponse = RetrieveOtherDeductionsResponse(
    submittedOn = "2019-04-04T01:01:01Z",
    seafarers = Some(Seq(seafarersModel, seafarersModel))
  )

  val noRefRetrieveOtherDeductionsResponse: RetrieveOtherDeductionsResponse = RetrieveOtherDeductionsResponse(
    "2019-04-04T01:01:01Z",
    Some(Seq(seafarersModel.copy(customerReference = None)))
  )

  val jsonMultipleSeafarers = Json.parse(
    s"""{
      | "submittedOn": "2019-04-04T01:01:01Z",
      | "seafarers": [$seafarersJson, $seafarersJson]
      |}""".stripMargin
  )

  val jsonNoRef = Json.parse("""{
      | "submittedOn": "2019-04-04T01:01:01Z",
      | "seafarers": [{
      |   "amountDeducted": 2000.99,
      |   "nameOfShip": "Blue Bell",
      |   "fromDate": "2018-04-06",
      |   "toDate": "2019-04-06"
      |   }]
      |}""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        responseBodyJson.as[RetrieveOtherDeductionsResponse] shouldBe responseBodyModel
      }
    }
    "passed a JSON with multiple seafarers" should {
      "return a valid model with multiple seafarers" in {
        jsonMultipleSeafarers.as[RetrieveOtherDeductionsResponse] shouldBe multipleSeafarersRetrieveOtherDeductionsResponse
      }
    }
    "passed JSON with no customer reference" should {
      "return a model with no customer reference" in {
        jsonNoRef.as[RetrieveOtherDeductionsResponse] shouldBe noRefRetrieveOtherDeductionsResponse
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(responseBodyModel) shouldBe responseBodyJson
      }
    }
    "passed a model with multiple seafarers" should {
      "return a JSON with multiple seafarers" in {
        Json.toJson(multipleSeafarersRetrieveOtherDeductionsResponse) shouldBe jsonMultipleSeafarers
      }
    }
    "passed a body with no customer reference" should {
      "return a JSON with no customer reference" in {
        Json.toJson(noRefRetrieveOtherDeductionsResponse) shouldBe jsonNoRef
      }
    }
  }

  "LinksFactory" should {
    "produce the correct links" when {
      "called" in {
        val data: RetrieveOtherDeductionsHateoasData = RetrieveOtherDeductionsHateoasData("mynino", "mytaxyear")

        MockAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()

        RetrieveOtherDeductionsResponse.RetrieveOtherLinksFactory.links(mockAppConfig, data) shouldBe Seq(
          hateoas.Link(href = s"/my/context/${data.nino}/${data.taxYear}", method = Method.PUT, rel = "create-and-amend-deductions-other"),
          hateoas.Link(href = s"/my/context/${data.nino}/${data.taxYear}", method = Method.GET, rel = "self"),
          hateoas.Link(href = s"/my/context/${data.nino}/${data.taxYear}", method = Method.DELETE, rel = "delete-deductions-other")
        )
      }
    }
  }

}
