package dawchat

import dawchat.domain.channel.ChannelDomainConfig
import dawchat.domain.user.Sha256TokenEncoder
import dawchat.domain.user.UsersDomainConfig
import dawchat.http.AuthInterceptor
import dawchat.http.AuthUserResolver
import dawchat.repojdbi.configureWithAppRequirements
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import kotlin.time.Duration.Companion.hours


@SpringBootApplication
@ComponentScan
class ChatApp{

    @Bean
    fun jdbi() = Jdbi.create(
        PGSimpleDataSource().apply {
            setURL(Environment.getDbUrl())
        },
    ).configureWithAppRequirements()


    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()

    @Bean
    fun clock() = Clock.System

    @Bean
    fun usersDomainConfig() = UsersDomainConfig(
        usernameMaxLen = 32,
        usernameMinLen = 4,
        passwordMinLen = 4,
        tokenLen = 256 / 8,
        tokenMaxAge = 24.hours,
        tokenMaxAgeIdle = 1.hours,
        maxTokensPerUser = 3,
    )

    @Bean
    fun channelDomainConfig() = ChannelDomainConfig(
        //TODO one single config?
        channelNameMaxLen = 32,
        channelNameMinLen = 4,
    )

    /*
    @Bean
    fun registerObjectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        val module = SimpleModule("MyInstantSerializer")
        module.addSerializer(ChannelType::class.java, ChannelTypeSerializer())
        module.addDeserializer(ChannelType::class.java, ChannelTypeDeserializer())
        mapper.registerModule(module)

        return mapper
    }
     */



}


@Configuration
class PipelineConfigure(
    val authenticationInterceptor: AuthInterceptor,
    val authenticatedUserArgumentResolver: AuthUserResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**").allowedOrigins("http://localhost:5000").allowCredentials(true)
        //registry.addMapping("/**").allowedMethods("*").allowCredentials(true)
    }

    /*
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(ToChannelTypeConverter())
        registry.addConverter(FromChannelTypeConverter())
    }
     */

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedUserArgumentResolver)
    }

    /*
    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>?>) {

        val builder = Jackson2ObjectMapperBuilder()
        builder.serializerByType(ChannelType::class.java, ChannelTypeSerializer())
        builder.deserializerByType(ChannelType::class.java, ChannelTypeDeserializer())
        builder.serializerByType(UserPermissions::class.java, UserPermsSerializer())
        builder.deserializerByType(UserPermissions::class.java, UserPermsDeserializer())
        val mapper = builder.build<ObjectMapper>()
        val converter1 = MappingJackson2HttpMessageConverter(mapper)
        converters.add(MappingJackson2HttpMessageConverter(mapper))

        val perms  = UserPermissions.entries.toTypedArray()
        val test = mapper.writeValueAsString(ChannelType.Public)
        val test2  = mapper.writeValueAsString(perms)

        /*
        val perms2: Array<UserPermissions> = mapper.readValue(
            "[\"read\", \"write\"]", mapper.typeFactory.constructCollectionType(
                MutableList::class.java,
                Array<UserPermissions>::class.java
            )
        )
         */


        super.configureMessageConverters(converters)


        /*
        val objectMapper = ObjectMapper()
        val module = SimpleModule()
        module.addSerializer(ChannelType::class.java, ChannelTypeSerializer())
        module.addDeserializer(ChannelType::class.java, ChannelTypeDeserializer())
        objectMapper.registerModule(module)
        val converter = MappingJackson2HttpMessageConverter(objectMapper)

         */


    }

     */



}



fun main() {
    runApplication<ChatApp>()
}