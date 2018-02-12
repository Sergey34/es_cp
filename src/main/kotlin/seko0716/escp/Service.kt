package seko0716.escp

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.web.client.RestTemplate
import java.io.File


@ShellComponent
class Service {
    @Autowired
    lateinit var restTemplate: RestTemplate

    @Value("#{\${server_map}}")
    val nameToUrl: Map<String, String> = HashMap()

    @ShellMethod("""copy from one es server to other. es server port is 9201.
        request is https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html
        sample: cp server1 index_name/_search server1""")
    fun cp(serverFromUrl: String, request: String, serverToUrl: String): String {
        val from = if (serverFromUrl.startsWith("http")) serverFromUrl else "http://$serverFromUrl"
        val to = if (serverToUrl.startsWith("http")) serverFromUrl else "http://$serverToUrl"
        return perform(serverFromUrl = from, request = request, serverToUrl = to)
    }

    @ShellMethod(""""copy from one es server to other.
        request is https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html
        sample: cp_port http://10.106.11.134 9201 test/_search http://10.106.11.134 9201""")
    fun cp_port(serverFromUrl: String, serverFromPort: Int, request: String, serverToUrl: String, serverToPort: Int): String {
        val from = if (serverFromUrl.startsWith("http")) serverFromUrl else "http://$serverFromUrl"
        val to = if (serverToUrl.startsWith("http")) serverFromUrl else "http://$serverToUrl"
        return perform(from, serverFromPort, request, to, serverToPort)
    }

    @ShellMethod(""""copy from one es server to other. es server port is 9201.
        request is https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html
        sample: cp_dev dev1 index_name/_search dev2""")
    fun cp_dev(serverFrom: String, request: String, serverTo: String): String {
        val from: String? = nameToUrl[serverFrom]
        val to: String? = nameToUrl[serverTo]
        if (from == null || to == null) return "not found server by nae"
        return perform(serverFromUrl = from, request = request, serverToUrl = to)
    }

    @ShellMethod(""""copy from one es server to file. es server port is 9201.
        request is https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html
        sample: cp_file dev1 index_name/_search""")
    fun cp_file(serverFrom: String, request: String): String {
        val from: String = nameToUrl[serverFrom] ?: return "not found server by nae"

        val result = restTemplate.getForObject("$from:9201/$request", Result::class.java)
                ?: return "failed request"
        val mapper = jacksonObjectMapper()
        val writeValueAsString = mapper.writeValueAsString(result)

        val file = File("${request.split("/")[0]}.json")
        file.createNewFile()

        file.bufferedWriter().use { out -> out.write(writeValueAsString) }

        return "done for ${result.getData().size}"
    }

    @ShellMethod(""""copy from file to es server. es server port is 9201.
        request is https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html
        sample: cp_from_file fileName dev2""")
    fun cp_from_file(fileName: String,serverToUrl: String): String {
        val to: String = nameToUrl[serverToUrl] ?: return "not found server by nae"
        val mapper = jacksonObjectMapper()
        val result = mapper.readValue<Result>(File(fileName))

        result.getData().forEach {
            restTemplate.put("$to:9201/${it._index}/${it._type}/${it._id}", it._source)
            println("$serverToUrl:$9201/${it._index}/${it._type}/${it._id}")
        }
        return "done for ${result.getData().size}"
    }


    private fun perform(serverFromUrl: String, serverFromPort: Int = 9201, request: String, serverToUrl: String, serverToPort: Int = 9201): String {
        val result = restTemplate.getForObject("$serverFromUrl:$serverFromPort/$request", Result::class.java)
                ?: return "failed request"

        result.getData().forEach {
            restTemplate.put("$serverToUrl:$serverToPort/${it._index}/${it._type}/${it._id}", it._source)
            println("$serverToUrl:$serverToPort/${it._index}/${it._type}/${it._id}")
        }
        return "done for ${result.getData().size}"
    }
}