package aomk.kooperate

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.sessions.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.hex
import io.ktor.websocket.webSocket
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(Authentication) {
        form(name = "form_auth") {
            skipWhen { call -> call.sessions.get<UserSession>() != null }
            userParamName = "name"
            passwordParamName = "password"
            challenge = FormAuthChallenge.Redirect { credentials -> "/login" }
            validate { credentials ->
                if (credentials.name == "admin" && credentials.password == "password")
                    UserIdPrincipal(credentials.name)
                else
                    null
            }
        }
    }

    install(Sessions) {
        val secretHashKey = hex("681aa57a3269f5c1968f45236123")

        cookie<UserSession>("UserSession") {
            cookie.path = "/"
            transform(SessionTransportTransformerMessageAuthentication(secretHashKey, "HmacSHA256"))
            SessionStorageMemory()
        }
    }

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(Application::class.java.classLoader, "frontend")
    }

    routing {
        get("/") {
            call.respond(FreeMarkerContent("index.ftl", null, "e"))
        }

        get("/login") {
            call.respond(FreeMarkerContent("index.ftl", null, "e"))
        }

        authenticate("form_auth") {
            post("/login") {
                val principal = call.authentication.principal<UserIdPrincipal>()
                if (principal != null) {
                    call.sessions.set(UserSession(principal.name))
                    call.respondRedirect("/admin")
                } else {
                    call.respondRedirect("/login")
                }
            }
        }

        get("/admin") {
            val userSession: UserSession? = call.sessions.get<UserSession>()

            if (userSession == null) {
                call.respondRedirect("/login")
            } else {
                call.respond("Nice Meme, ${userSession.name}")
            }
        }

        // Static feature. Try to access `/static/ktor_logo.svg`
        static("/static") {
            resources("static")
        }

        static("css") {
            resources("frontend/css")
        }

        install(StatusPages) {
            exception<AuthenticationException> { cause ->
                call.respond(HttpStatusCode.Unauthorized)
            }
            exception<AuthorizationException> { cause ->
                call.respond(HttpStatusCode.Forbidden)
            }

        }

        webSocket("/myws/echo") {
            send(Frame.Text("Hi from server"))
            while (true) {
                val frame = incoming.receive()
                if (frame is Frame.Text) {
                    send(Frame.Text("Client said: " + frame.readText()))
                }
            }
        }
    }
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()

data class UserSession(val name: String)