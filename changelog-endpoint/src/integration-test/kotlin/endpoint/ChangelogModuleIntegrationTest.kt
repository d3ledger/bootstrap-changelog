/*
 * Copyright Soramitsu Co., Ltd. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package endpoint

import environments.ChangelogModuleIntegrationTestEnvironment
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import jp.co.soramitsu.bootstrap.changelog.ChangelogInterface
import jp.co.soramitsu.bootstrap.changelog.endpoint.changelogModule
import jp.co.soramitsu.bootstrap.changelog.service.changelogHistoryStorageAccountId
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.math.absoluteValue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChangelogModuleIntegrationTest {

    private val changelogEnvironment = ChangelogModuleIntegrationTestEnvironment()

    @AfterAll
    fun tearDown() {
        changelogEnvironment.close()
    }

    /**
     * @given script with Iroha account creation logic
     * @when script is passed to changelog service twice
     * @then account is created, second changelog attempt didn't make any effect
     */
    @Test
    fun testIdempotent() = withTestApplication({
        changelogModule(changelogEnvironment.changelogExecutor)
    }) {
        val random = Random()
        val firstRandomAccountName = random.nextInt().absoluteValue.toString()
        val randomSchema = random.nextInt().absoluteValue.toString()
        var request = changelogEnvironment.createChangelogScriptRequest(
            firstRandomAccountName,
            randomSchema
        )
        with(handleRequest(HttpMethod.Post, "/changelog/changelogScript") {
            setBody(changelogEnvironment.gson.toJson(request))
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
        //Second changelog attempt
        val secondRandomAccountName = random.nextInt().absoluteValue.toString()
        //Same schema, new account
        request = changelogEnvironment.createChangelogScriptRequest(
            secondRandomAccountName,
            randomSchema
        )
        with(handleRequest(HttpMethod.Post, "/changelog/changelogScript") {
            setBody(changelogEnvironment.gson.toJson(request))
        }) {
            assertEquals(HttpStatusCode.NotModified, response.status())
        }
        //Account was not created because of old schema
        assertFalse(changelogEnvironment.accountExists("$secondRandomAccountName@d3"))
    }

    /**
     * @given script with Iroha account creation logic
     * @when script is passed to changelog service
     * @then account is created
     */
    @Test
    fun testExecuteScript() = withTestApplication({
        changelogModule(changelogEnvironment.changelogExecutor)
    }) {
        val random = Random()
        val randomAccountName = random.nextInt().absoluteValue.toString()
        val randomSchema = random.nextInt().absoluteValue.toString()
        val request = changelogEnvironment.createChangelogScriptRequest(
            randomAccountName,
            randomSchema
        )
        with(handleRequest(HttpMethod.Post, "/changelog/changelogScript") {
            setBody(changelogEnvironment.gson.toJson(request))
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
        val changelogReducedTxHash = JSONObject(
            changelogEnvironment.queryAPI.getAccountDetails(
                changelogHistoryStorageAccountId,
                ChangelogInterface.superuserAccountId,
                randomSchema
            )
        ).getJSONObject(ChangelogInterface.superuserAccountId).get(randomSchema).toString()
        assertNotNull(changelogReducedTxHash)

        val account = changelogEnvironment.queryAPI.getAccount("$randomAccountName@d3").account
        assertEquals(
            "{\"superuser@bootstrap\": {\"test_key\": \"test_value\"}}",
            account.jsonData
        )
    }
}
