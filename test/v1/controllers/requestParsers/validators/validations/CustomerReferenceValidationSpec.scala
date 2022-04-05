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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.CustomerReferenceFormatError

class CustomerReferenceValidationSpec extends UnitSpec {

  val validCustomerRef         = "SEAFARERS1234"
  val invalidCustomerRefMax    = "JJJAHHSUUSNNNAMMSMMSMSIIAAALLLAOOo982031813jJJAMMMAAmmmAAAPPPAOAOAOAJJJSSSAJNDUDNUNDUDNKI9B"
  val invalidCustomerRefFormat = "+=@,."
  val path                     = "/seafarers/0/customerReference"

  "validate" should {
    "return no errors" when {
      "a valid customer reference is supplied" in {
        val validationResult = CustomerReferenceValidation.validateOptional(Some(validCustomerRef), path)

        validationResult.isEmpty shouldBe true
      }
      "no customer reference is supplied" in {
        val validationResult = CustomerReferenceValidation.validateOptional(None, path)

        validationResult.isEmpty shouldBe true
      }
    }
    "return a format error" when {
      "the customer reference exceeds 90 characters" in {
        val validationResult = CustomerReferenceValidation.validateOptional(Some(invalidCustomerRefMax), path)

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe CustomerReferenceFormatError.copy(paths = Some(Seq(path)))
      }
      "the customer reference has invalid characters" in {
        val validationResult = CustomerReferenceValidation.validateOptional(Some(invalidCustomerRefFormat), path)

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe CustomerReferenceFormatError.copy(paths = Some(Seq(path)))
      }
    }
  }

}
