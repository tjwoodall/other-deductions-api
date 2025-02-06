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

package v2

import shared.config.SharedAppConfig
import shared.hateoas.Link
import shared.hateoas.Method.{DELETE, GET, PUT}

trait HateoasLinks {

  private object RelType {
    val SELF                              = "self"
    val CREATE_AND_AMEND_DEDUCTIONS_OTHER = "create-and-amend-deductions-other"
    val DELETE_DEDUCTIONS_OTHER           = "delete-deductions-other"
  }

  // API resource links
  def createAndAmendOtherDeductions(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = otherDeductionsUri(appConfig, nino, taxYear), method = PUT, rel = RelType.CREATE_AND_AMEND_DEDUCTIONS_OTHER)

  // Domain URIs
  private def otherDeductionsUri(appConfig: SharedAppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/$nino/$taxYear"

  def deleteOtherDeductions(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = otherDeductionsUri(appConfig, nino, taxYear), method = DELETE, rel = RelType.DELETE_DEDUCTIONS_OTHER)

  def retrieveOtherDeductions(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(href = otherDeductionsUri(appConfig, nino, taxYear), method = GET, rel = RelType.SELF)

}
