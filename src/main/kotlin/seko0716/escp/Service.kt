package seko0716.escp

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.web.client.RestTemplate

@ShellComponent
class Service {
    @Autowired
    lateinit var restTemplate: RestTemplate

    @ShellMethod("Add two integers together.")
    fun perfoam(serverFromUrl: String, serverFromPort: Int, request: String): Int {
        val result = restTemplate.getForObject("$serverFromUrl:$serverFromPort/$request", Result::class.java)

        val hit = result.getData()[0]
//        restTemplate.put("$serverFromUrl:$serverFromPort/${hit._index}/${hit._type}",)

        println(result)
        return 5 + 5
    }
}