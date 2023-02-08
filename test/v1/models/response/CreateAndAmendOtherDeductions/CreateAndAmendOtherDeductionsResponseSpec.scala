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

package v1.models.response.CreateAndAmendOtherDeductions

import api.models.hateoas
import api.models.hateoas.Method
import mocks.MockAppConfig
import support.UnitSpec
import v1.models.response.createAndAmendOtherDeductions.{CreateAndAmendOtherDeductionsHateoasData, CreateAndAmendOtherDeductionsResponse}

class CreateAndAmendOtherDeductionsResponseSpec extends UnitSpec with MockAppConfig {

  "LinksFactory" should {
    "produce the correct links" when {
      "called" in {
        val data: CreateAndAmendOtherDeductionsHateoasData = CreateAndAmendOtherDeductionsHateoasData("mynino", "mytaxyear")

        MockAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()

        CreateAndAmendOtherDeductionsResponse.CreateAndAmendOtherLinksFactory.links(mockAppConfig, data) shouldBe Seq(
          hateoas.Link(href = s"/my/context/${data.nino}/${data.taxYear}", method = Method.PUT, rel = "create-and-amend-deductions-other"),
          hateoas.Link(href = s"/my/context/${data.nino}/${data.taxYear}", method = Method.GET, rel = "self"),
          hateoas.Link(href = s"/my/context/${data.nino}/${data.taxYear}", method = Method.DELETE, rel = "delete-deductions-other")
        )
      }
    }
  }

}
