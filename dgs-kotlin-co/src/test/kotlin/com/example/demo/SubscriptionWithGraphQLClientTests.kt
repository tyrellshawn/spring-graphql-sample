package com.example.demo

import com.example.demo.gql.types.Comment
import com.example.demo.gql.types.Post
import com.netflix.graphql.dgs.DgsQueryExecutor
import com.netflix.graphql.dgs.client.DefaultGraphQLClient
import com.netflix.graphql.dgs.client.HttpResponse
import com.netflix.graphql.dgs.client.MonoRequestExecutor
import graphql.ExecutionResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubscriptionWithGraphQLClientTests {
    private val log = LoggerFactory.getLogger(SubscriptionWithGraphQLClientTests::class.java)

    @LocalServerPort
    var port: Int = 8080

    lateinit var webClient: WebClient

    lateinit var client: DefaultGraphQLClient

    @Autowired
    lateinit var dgsQueryExecutor: DgsQueryExecutor

    @BeforeEach
    fun setup() {
        webClient = WebClient.create("http://localhost:$port")
        client = DefaultGraphQLClient("http://localhost:$port")
    }

    @Test
    fun `sign in and create a post and comment`() {
        val requestExecutor = MonoRequestExecutor { _, headers, body ->
            webClient.post().uri("/graphql")
                .headers {
                    headers.forEach { (t, u) -> it[t] = u }
                }
                .bodyValue(body)
                .retrieve()
                .toEntity(String::class.java)
                .map { HttpResponse(it.statusCodeValue, it.body) }
        }

        val requestExecutorWithAuth = MonoRequestExecutor { _, headers, body ->
            webClient.post().uri("/graphql")
                .headers {
                    it.setBasicAuth("user", "password")
                    headers.forEach { (t, u) -> it[t] = u }
                }
                .bodyValue(body)
                .retrieve()
                .toEntity(String::class.java)
                .map { HttpResponse(it.statusCodeValue, it.body) }
        }

        val createPostQuery =
            "mutation createPost(\$input: CreatePostInput!){ createPost(createPostInput:\$input) {id, title} }"
        val createPostQueryVariables = mapOf(
            "input" to mapOf(
                "title" to "test title",
                "content" to "test content"
            )
        )
        val createPostResult =
            this.client.reactiveExecuteQuery(createPostQuery, createPostQueryVariables, requestExecutor)
                .map { it.extractValueAsObject("createPost", Post::class.java) }
                .block(Duration.ofSeconds(5L))
        val postId = createPostResult?.id
        assertThat(postId).isNotNull
        assertThat(createPostResult?.title).isEqualTo("test title")

        // get post by id.
        val postByIdQuery = "query postById(\$id: String!){postById(postId:\$id){ id title }}"
        val postByIdVariables = mapOf<String, Any>(
            "id" to postId as Any
        )


        this.client.reactiveExecuteQuery(postByIdQuery, postByIdVariables, requestExecutor)
            .map { it.extractValueAsObject("postById", Post::class.java) }
            .`as` { StepVerifier.create(it) }
            .consumeNextWith { assertThat(it!!.title).isEqualTo("test title") }
            .verifyComplete()

        // Simply use `DgsQueryExecutor` to subscribe to commentAdded.
        // TODO: authentication is disabled.
        // The `DgsQueryExecutor` does not work in a web layer.
        val executionResult =
            dgsQueryExecutor.execute("subscription onCommentAdded{ commentAdded{ id postId  content}}")
        val publisher = executionResult.getData<Publisher<ExecutionResult>>()

        val verifier = StepVerifier.create(publisher)
            .consumeNextWith {
                log.debug("comment@1: {}", it)
                assertThat(
                    (it.getData<Map<String, Map<String, Any>>>()["commentAdded"]
                        ?.get("content") as String)
                ).isEqualTo("comment1")
            }
            .consumeNextWith {
                log.debug("comment@2: {}", it)
                assertThat(
                    (it.getData<Map<String, Map<String, Any>>>()["commentAdded"]
                        ?.get("content") as String)
                ).isEqualTo("comment2")
            }
            .thenCancel()
            .verifyLater()//delay to verify

        val commentQuery =
            "mutation addComment(\$input: CommentInput!) { addComment(commentInput:\$input) { id postId content}}"
        val comment1Variables = mapOf(
            "input" to mapOf(
                "postId" to postId,
                "content" to "comment1"
            )
        )
        val comment2Variables = mapOf(
            "input" to mapOf(
                "postId" to postId,
                "content" to "comment2"
            )
        )

        this.client.reactiveExecuteQuery(commentQuery, comment1Variables, requestExecutorWithAuth)
            .map { it.extractValueAsObject("addComment", Comment::class.java) }
            .`as` { StepVerifier.create(it) }
            .consumeNextWith { assertThat(it.content).isEqualTo("comment1") }
            .verifyComplete()

        this.client.reactiveExecuteQuery(commentQuery, comment2Variables, requestExecutorWithAuth)
            .map { it.extractValueAsObject("addComment", Comment::class.java) }
            .`as` { StepVerifier.create(it) }
            .consumeNextWith { assertThat(it.content).isEqualTo("comment2") }
            .verifyComplete()

        // verify now.
        verifier.verify()
    }
}