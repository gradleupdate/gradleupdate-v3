package org.hidetake.gradleupdate.app

import org.hidetake.gradleupdate.domain.*
import org.springframework.stereotype.Service

@Service
class GradleUpdateService(
    private val repositoryRepository: RepositoryRepository,
    private val gradleWrapperRepository: GradleWrapperRepository,
    private val pullRequestRepository: PullRequestRepository
) {
    private val LATEST_GRADLE_WRAPPER = RepositoryPath("int128", "latest-gradle-wrapper")

    fun getRepositoryMetadata(repositoryPath: RepositoryPath) =
        repositoryRepository.getByName(repositoryPath)

    fun getGradleWrapperVersionStatus(repositoryPath: RepositoryPath): GradleWrapperVersionStatus? =
        gradleWrapperRepository.findVersion(repositoryPath)?.let { target ->
            gradleWrapperRepository.findVersion(LATEST_GRADLE_WRAPPER)?.let { latest ->
                GradleWrapperVersionStatus(target, latest)
            }
        }

    fun findPullRequestForUpdate(repositoryPath: RepositoryPath): PullRequestForUpdate? =
        gradleWrapperRepository.findVersion(LATEST_GRADLE_WRAPPER)?.let { latest ->
            pullRequestRepository.find(repositoryPath, latest)
        }

    fun createPullRequestForLatestGradleWrapper(repositoryPath: RepositoryPath) =
        gradleWrapperRepository.findVersion(repositoryPath)?.let { target ->
            gradleWrapperRepository.findVersion(LATEST_GRADLE_WRAPPER)?.let { latest ->
                val status = GradleWrapperVersionStatus(target, latest)
                when {
                    status.upToDate -> TODO()
                    else -> {
                        val files = gradleWrapperRepository.findFiles(LATEST_GRADLE_WRAPPER)
                        pullRequestRepository.createOrUpdate(repositoryPath, latest, files)
                    }
                }
            }
        }
}
