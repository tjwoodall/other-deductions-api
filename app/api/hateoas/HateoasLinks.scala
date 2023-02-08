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

package api.hateoas

import api.models.hateoas.Link
import api.models.hateoas.Method._
import api.models.hateoas.RelType._
import config.AppConfig

trait HateoasLinks {

  // Domain URIs
  private def otherDeductionsUri(appConfig: AppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/$nino/$taxYear"

  // API resource links
  def createAndAmendOtherDeductions(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = otherDeductionsUri(appConfig, nino, taxYear), method = PUT, rel = CREATE_AND_AMEND_DEDUCTIONS_OTHER)

  def deleteOtherDeductions(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = otherDeductionsUri(appConfig, nino, taxYear), method = DELETE, rel = DELETE_DEDUCTIONS_OTHER)

  def retrieveOtherDeductions(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = otherDeductionsUri(appConfig, nino, taxYear), method = GET, rel = SELF)

}
