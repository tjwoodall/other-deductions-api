/*
 * Copyright 2020 HM Revenue & Customs
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
import v1.models.errors.ValueFormatError

class AmountValidationSpec extends UnitSpec {

  val validAmount = 2000.99
  val invalidAmountMin = -10
  val invalidAmountMax = 100000000000.00
  val invalidAmountDecimals = 42102.39142
  val path = "/seafarers/1/amountDeducted"

  "validate" should {
    "return no errors" when {
      "a valid name is supplied" in {
        val validationResult = AmountValidation.validate(validAmount, path)

        validationResult.isEmpty shouldBe true
      }
    }
    "return an amount format error" when {
      "The amount exceeds the max amount" in {
        val validationResult = AmountValidation.validate(invalidAmountMax, path)

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe ValueFormatError.copy(paths = Some(Seq(path)))
      }
      "The amount has more than 2 decimal points" in {
        val validationResult = AmountValidation.validate(invalidAmountDecimals, path)

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe ValueFormatError.copy(paths = Some(Seq(path)))
      }
      "The amount is less than the minimum amount" in {
        val validationResult = AmountValidation.validate(invalidAmountMin, path)

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe ValueFormatError.copy(paths = Some(Seq(path)))
      }
    }
  }

}
