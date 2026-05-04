/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.ui.search

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SearchActivityRepositoryTest {

    @Test
    fun `resolveLocationSearchSource prefers nominatim when locationiq key exists and source is unset`() {
        SearchActivityRepository.resolveLocationSearchSource(
            storedSource = null,
            defaultSource = "openmeteo",
            hasLocationIqKey = true,
        ) shouldBe "nominatim"
    }

    @Test
    fun `resolveLocationSearchSource prefers nominatim when locationiq key exists and source is still default`() {
        SearchActivityRepository.resolveLocationSearchSource(
            storedSource = "openmeteo",
            defaultSource = "openmeteo",
            hasLocationIqKey = true,
        ) shouldBe "nominatim"
    }

    @Test
    fun `resolveLocationSearchSource keeps explicit non default source when locationiq key exists`() {
        SearchActivityRepository.resolveLocationSearchSource(
            storedSource = "geonames",
            defaultSource = "openmeteo",
            hasLocationIqKey = true,
        ) shouldBe "geonames"
    }

    @Test
    fun `resolveLocationSearchSource keeps default source when no locationiq key exists`() {
        SearchActivityRepository.resolveLocationSearchSource(
            storedSource = null,
            defaultSource = "openmeteo",
            hasLocationIqKey = false,
        ) shouldBe "openmeteo"
    }
}