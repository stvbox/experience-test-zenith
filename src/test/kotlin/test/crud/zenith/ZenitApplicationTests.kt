package test.crud.zenith

import com.querydsl.core.BooleanBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.LinkRelation
import org.springframework.hateoas.RepresentationModel
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.test.context.ActiveProfiles
import java.util.*


@AutoConfigureTestEntityManager
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ZenithApplicationTests {
    @Autowired
    protected var restTemplate: TestRestTemplate? = null

    @Autowired
    protected var executorsRepository: Executors? = null

    @Autowired
    protected var managersRepository: Managers? = null

    @Autowired
    protected var projectsRepository: Projects? = null

    @LocalServerPort
    protected var port = 0

    @BeforeAll
    fun setup() {
        projectsRepository?.deleteAll()
        executorsRepository?.deleteAll()
        managersRepository?.deleteAll()
    }

    @Test
    fun test() {
        restTemplate?.restTemplate?.requestFactory = HttpComponentsClientHttpRequestFactory()

        val HOST_URL = "http://localhost:${port}";
        val MANAGERS_ENDPOINT = "${HOST_URL}/managers/"
        val EXECUTORS_ENDPOINT = "${HOST_URL}/executors/"
        val PROJECTS_ENDPOINT = "${HOST_URL}/projects/"

        // Создание исполнителя ////////////////////////////////////////////////////////////////////////////////////////
        val ivanPatronymic = UUID.randomUUID().toString();
        var body = mapOf("name" to "Иван", "surname" to "Иванов", "patronymic" to ivanPatronymic)
        var resultEx = restTemplate?.postForEntity(EXECUTORS_ENDPOINT, body, EmployeeMdl::class.java)
        // Исполнитель создан
        Assertions.assertEquals(HttpStatus.CREATED, resultEx?.statusCode)

        var booleanBuilder = BooleanBuilder()
        booleanBuilder.and(QExecutor.executor.patronymic.eq(ivanPatronymic))
        var employeeCnt = executorsRepository?.findAll(booleanBuilder)?.count()
        // Исполнитель найден (БЭК)
        Assertions.assertTrue(employeeCnt != null && employeeCnt > 0)

        // Создание менеджера //////////////////////////////////////////////////////////////////////////////////////////
        val dimaPatronymic = UUID.randomUUID().toString();
        body = mapOf("name" to "Дмитрий", "surname" to "Дмитриев", "patronymic" to dimaPatronymic)
        var resultMn = restTemplate?.postForEntity(MANAGERS_ENDPOINT, body, EmployeeMdl::class.java)
        // Менеджер создан
        Assertions.assertEquals(HttpStatus.CREATED, resultMn?.statusCode)

        booleanBuilder = BooleanBuilder()
        booleanBuilder.and(QManager.manager.patronymic.eq(dimaPatronymic))
        employeeCnt = managersRepository?.findAll(booleanBuilder)?.count()
        // Менеджер найден (БЭК)
        Assertions.assertTrue(employeeCnt != null && employeeCnt > 0)

        // Создание проекта ////////////////////////////////////////////////////////////////////////////////////////////
        var projectName = "Разработка проектной документации для тестового задания"
        body = mapOf("name" to projectName)
        var resultPr = restTemplate?.postForEntity(PROJECTS_ENDPOINT, body, ProjectMdl::class.java)
        // Проект создан
        Assertions.assertEquals(HttpStatus.CREATED, resultPr?.statusCode)

        booleanBuilder = BooleanBuilder()
        booleanBuilder.and(QProject.project.name.eq(projectName))
        var projectCnt = projectsRepository?.findAll(booleanBuilder)?.count()
        // Проект найден (БЭК)
        Assertions.assertTrue(projectCnt != null && projectCnt > 0)

        // Назначение сотрудников ////////////////////////////////////////////////////////////
        var projectSelfLink = resultPr?.body?.links?.getLink(LinkRelation.of("self"))
        var executorSelfLink = resultEx?.body?.links?.getLink(LinkRelation.of("self"))
        var managerSelfLink = resultMn?.body?.links?.getLink(LinkRelation.of("self"))
        var executorUrl = executorSelfLink?.get()?.href;
        var managerUrl = managerSelfLink?.get()?.href;
        var projectUrl = projectSelfLink?.get()?.href;

        val requestHeaders = HttpHeaders()
        requestHeaders.add("Content-type", "text/uri-list")

        // Назначение исполнителя
        var httpEntity: HttpEntity<String> = HttpEntity(executorUrl, requestHeaders)
        restTemplate?.exchange("${projectUrl}/executors", HttpMethod.PUT, httpEntity, String::class.java)

        // Назначение менеджера
        httpEntity = HttpEntity(managerUrl, requestHeaders)
        restTemplate?.exchange("${projectUrl}/managers", HttpMethod.PUT, httpEntity, String::class.java)

        // Изменение любого атрибута проекта, ранее изменен состав участников
        var patchHeaders = HttpHeaders();
        patchHeaders.add("Content-type", "application/hal+json");
        var bodyStatus = mapOf("status" to ProjectStatuses.GOES)
        var bodyStatusEntity = HttpEntity(bodyStatus, patchHeaders)
        var resultPr1 = restTemplate?.exchange(projectUrl, HttpMethod.PATCH, bodyStatusEntity, ProjectMdl::class.java)

        // Статус изменился
        Assertions.assertEquals(resultPr1?.body?.status, ProjectStatuses.GOES)

        // Получение списка проектов
        var projects = restTemplate?.getForEntity(PROJECTS_ENDPOINT, ProjectMdlCollection::class.java)
        // Получение списка проектов
        Assertions.assertTrue(projects?.body?.content?.size != null && projects.body?.content?.size == 1)

        // Получение исполнителей проекта
        var executors = restTemplate?.getForEntity("${projectUrl}/executors", EmployeeMdlCollection::class.java)
        Assertions.assertTrue(executors?.body?.content?.size != null && executors.body?.content?.size == 1)

        // Получение проектов исполнителя
        projects = restTemplate?.getForEntity("${executorUrl}/projects", ProjectMdlCollection::class.java)
        Assertions.assertTrue(projects?.body?.content?.size != null && projects.body?.content?.size == 1)

        // Удаление менеджера
        restTemplate?.delete(managerUrl)
        // Удаление проекта
        restTemplate?.delete(projectUrl)
        // Удаление исполнителя
        restTemplate?.delete(executorUrl)


        // Проверка удаления проекта
        var projectsItems = projectsRepository?.findAll();
        Assertions.assertTrue(projectsItems != null && projectsItems?.count() == 0)
        // Проверка удаления исполнителя
        var executorsItems = executorsRepository?.findAll();
        Assertions.assertTrue(executorsItems?.count() != null && executorsItems?.count() == 0)
        // Проверка удаления менеджера
        var managersItems = managersRepository?.findAll();
        Assertions.assertTrue(managersItems?.count() != null && managersItems?.count() == 0)

    }

    open class EmployeeMdl : RepresentationModel<EmployeeMdl>() {
        open var id: Long? = null
        open var name: String? = null
        open var surname: String? = null
        open var patronymic: String? = null
        open var projects: Set<Project>? = null
    }

    class EmployeeMdlCollection : CollectionModel<EmployeeMdl>()

    open class ProjectMdl : RepresentationModel<ProjectMdl>() {
        var id: Long? = null
        open var name: String? = null
        var status: ProjectStatuses? = null
        var startDate: Long? = null
        var finishDate: Long? = null
        open var managers: Set<Manager>? = null
        open var executors: Set<Executor>? = null
    }

    class ProjectMdlCollection : CollectionModel<ProjectMdl>()

}