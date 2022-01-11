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
import v1.models.errors.DateFormatError

class DateValidationSpec extends UnitSpec {

  val validDate = "2019-02-18"
  val invalidDate = "01-10-2039"
  val path  = "/seafarers/2/fromDate"

  "validate" should {
    "return no errors" when {
      "a valid date is supplied" in {
        val validationResult = DateValidation.validate(validDate, path)
        print(validationResult)
        validationResult.isEmpty shouldBe true
      }
    }
    "return a date format error" when {
      "an invalid date is supplied" in {
        val validationResult = DateValidation.validate(invalidDate, path)

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe DateFormatError.copy(paths = Some(Seq(path)))
      }
    }
  }

}
