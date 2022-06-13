package test.crud.zenith

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.CrossOrigin
import javax.persistence.*
import javax.validation.constraints.NotNull

@SpringBootApplication
class ZenithApplication

fun main(args: Array<String>) {
    runApplication<ZenithApplication>(*args)
}

@CrossOrigin(origins = ["*"], allowCredentials = "true")
interface CorsConfig

@Repository
interface Projects : PagingAndSortingRepository<Project, Long>, QuerydslPredicateExecutor<Project> {

}

@Repository
interface Managers : PagingAndSortingRepository<Manager, Long>, QuerydslPredicateExecutor<Manager> {

}

@Repository
interface Executors : PagingAndSortingRepository<Executor, Long>, QuerydslPredicateExecutor<Executor> {

}

@Entity
@Table(name = "PROJECTS")
class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false, updatable = false)
    open var id: Long? = null

    @NotNull
    @Column(columnDefinition = "VARCHAR", length = 1024, unique = true)
    open var name: String = "Наименование проекта"

    open var status: ProjectStatuses? = ProjectStatuses.WAIT

    open var startDate: Long? = null
    open var finishDate: Long? = null

    @ManyToMany(
        fetch = FetchType.EAGER,
        cascade = [CascadeType.DETACH]
    )
    @JoinTable(
        name = "project_manager",
        joinColumns = [JoinColumn(name = "project_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "manager_id", referencedColumnName = "id")]
    )
    open var managers: Set<Manager>? = null

    @ManyToMany(
        fetch = FetchType.EAGER,
        cascade = [CascadeType.DETACH]
    )
    @JoinTable(
        name = "project_executor",
        joinColumns = [JoinColumn(name = "project_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "executor_id", referencedColumnName = "id")]
    )
    open var executors: Set<Executor>? = null
}

@Entity
@Table(name = "EMPLOYEES")
open class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false, updatable = false)
    open var id: Long? = null

    @NotNull
    @Column(columnDefinition = "VARCHAR", length = 100)
    open var name: String? = null

    @NotNull
    @Column(columnDefinition = "VARCHAR", length = 100)
    open var surname: String? = null

    @NotNull
    @Column(columnDefinition = "VARCHAR", length = 100)
    open var patronymic: String? = null
}

@Entity
class Manager : Employee() {
    @ManyToMany(
        //mappedBy = "managers",
        fetch = FetchType.EAGER,
        cascade = [CascadeType.DETACH]
    )
    @JoinTable(
        name = "project_manager",
        joinColumns = [JoinColumn(name = "manager_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "project_id", referencedColumnName = "id")]
    )
    var projects: Set<Project>? = null
}

@Entity
class Executor : Employee() {
    @ManyToMany(
        //mappedBy = "executors",
        fetch = FetchType.EAGER,
        cascade = [CascadeType.DETACH]
    )
    @JoinTable(
        name = "project_executor",
        joinColumns = [JoinColumn(name = "executor_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "project_id", referencedColumnName = "id")]
    )
    var projects: Set<Project>? = null
}

enum class ProjectStatuses(var code: String) {
    WAIT("WAIT"),
    GOES("GOES"),
    ENDED("ENDED");
}