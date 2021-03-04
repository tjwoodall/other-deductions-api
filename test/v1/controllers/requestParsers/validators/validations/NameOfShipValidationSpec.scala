/*
 * Copyright 2021 HM Revenue & Customs
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
import v1.models.errors.NameOfShipFormatError

class NameOfShipValidationSpec extends UnitSpec {

  val validName = "Blue Bell"
  val invalidName = "Ships. Of Blue Red Yellow Green Orange Purple Violet Indigo Brown Black White Beige Cream Grey Gold Silver"
  val path = "/seafarers/3/nameOfShip"

  "validate" should {
    "return no errors" when {
      "a valid name is supplied" in {
        val validationResult = NameOfShipValidation.validate(validName, path)

        validationResult.isEmpty shouldBe true
      }
    }
    "return a name of ship format error" when {
      "a name that exceeds 105 characters" in {
        val validationResult = NameOfShipValidation.validate(invalidName, path)

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe NameOfShipFormatError.copy(paths = Some(Seq(path)))
      }
    }
  }
}
