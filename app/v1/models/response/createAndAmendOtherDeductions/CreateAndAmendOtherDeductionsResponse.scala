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

package v1.models.response.createAndAmendOtherDeductions

import shared.config.SharedAppConfig
import shared.hateoas.{HateoasData, HateoasLinksFactory, Link}
import v1.HateoasLinks

object CreateAndAmendOtherDeductionsResponse extends HateoasLinks {

  implicit object CreateAndAmendOtherLinksFactory extends HateoasLinksFactory[Unit, CreateAndAmendOtherDeductionsHateoasData] {

    override def links(appConfig: SharedAppConfig, data: CreateAndAmendOtherDeductionsHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAndAmendOtherDeductions(appConfig, nino, taxYear),
        retrieveOtherDeductions(appConfig, nino, taxYear),
        deleteOtherDeductions(appConfig, nino, taxYear)
      )
    }

  }

}

case class CreateAndAmendOtherDeductionsHateoasData(nino: String, taxYear: String) extends HateoasData
