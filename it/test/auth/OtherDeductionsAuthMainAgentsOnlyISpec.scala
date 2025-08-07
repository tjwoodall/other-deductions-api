/*
 * Copyright 2025 HM Revenue & Customs
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

package auth

import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import shared.auth.AuthMainAgentsOnlyISpec
import shared.models.domain.TaxYear
import shared.services.DownstreamStub

class OtherDeductionsAuthMainAgentsOnlyISpec extends AuthMainAgentsOnlyISpec {

  val callingApiVersion = "1.0"

  val supportingAgentsNotAllowedEndpoint = "retrieve-other-deductions"

  private val taxYear = TaxYear.fromMtd("2021-22")

  val mtdUrl = s"/$nino/${taxYear.asMtd}"

  def sendMtdRequest(request: WSRequest): WSResponse = await(request.get())

  val downstreamUri: String = s"/income-tax/deductions/$nino/${taxYear.asMtd}"

  val maybeDownstreamResponseJson: Option[JsValue] = Some(
    Json.parse(
      """
        |{
        |   "submittedOn": "2019-04-04T01:01:01.000Z",
        |   "seafarers": [{
        |      "customerReference": "SEAFARERS1234",
        |      "amountDeducted": 2543.32,
        |      "nameOfShip": "Blue Bell",
        |      "fromDate": "2019-04-06",
        |      "toDate": "2020-04-05"
        |   }]
        |}
        |""".stripMargin
    )
  )

  override val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.GET

  override val expectedMtdSuccessStatus: Int = OK

}
