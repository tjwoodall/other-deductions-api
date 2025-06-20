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

package v1.fixtures

import play.api.libs.json.{JsObject, JsValue, Json}
import shared.models.domain.Timestamp
import v1.models.response.retrieveOtherDeductions.{RetrieveOtherDeductionsResponse, Seafarers}

object RetrieveOtherDeductionsFixtures {

  val seafarersModel: Seafarers = Seafarers(
    customerReference = Some("myRef"),
    amountDeducted = 2000.99,
    nameOfShip = "Blue Bell",
    fromDate = "2018-04-06",
    toDate = "2019-04-06"
  )

  val seafarersJson: JsValue = Json.parse(
    s"""
       |{
       |  "customerReference": "myRef",
       |  "amountDeducted": 2000.99,
       |  "nameOfShip": "Blue Bell",
       |  "fromDate": "2018-04-06",
       |  "toDate": "2019-04-06"
       |}
       |""".stripMargin
  )

  val responseBodyModel: RetrieveOtherDeductionsResponse = RetrieveOtherDeductionsResponse(
    submittedOn = Timestamp("2019-04-04T01:01:01.000Z"),
    seafarers = Some(Seq(seafarersModel))
  )

  val responseBodyJson: JsValue = Json.parse(
    s"""{
       |  "submittedOn": "2019-04-04T01:01:01.000Z",
       |  "seafarers": [$seafarersJson]
       |}""".stripMargin
  )

  def responseWithHateoasLinks(taxYear: String): JsValue = responseBodyJson.as[JsObject] ++ hateoasLinks(taxYear).as[JsObject]

  private def hateoasLinks(taxYear: String): JsValue = Json.parse(
    s"""
       |{
       |  "links":[
       |    {
       |       "href":"/individuals/deductions/other/AA123456A/$taxYear",
       |       "method":"PUT",
       |       "rel":"create-and-amend-deductions-other"
       |    },
       |    {
       |       "href":"/individuals/deductions/other/AA123456A/$taxYear",
       |       "method":"GET",
       |       "rel":"self"
       |    },
       |    {
       |       "href":"/individuals/deductions/other/AA123456A/$taxYear",
       |       "method":"DELETE",
       |       "rel":"delete-deductions-other"
       |    }
       |  ]
       |}
       |""".stripMargin
  )

}
