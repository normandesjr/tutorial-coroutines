package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .bodyList()

        val channel = Channel<List<User>>()
        for (repo in repos) {
            launch {
                val users = service.getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                channel.send(users)
            }
        }

        var allUsers = emptyList<User>()
        repeat(repos.size) {
            val users = channel.receive()
            allUsers = (allUsers + users).aggregate()
            updateResults(allUsers, it == repos.lastIndex)
        }
//
//        val deferreds: List<Deferred<List<User>>> = repos.map { repo ->
//            async {
//                log("starting loading for ${repo.name}")
//                delay(3000)
//                service.getRepoContributors(req.org, repo.name)
//                    .also { logUsers(repo, it) }
//                    .bodyList()
//            }
//        }
//
//        deferreds.awaitAll().flatten().aggregate()

//
//        var allUsers = emptyList<User>()
//        for ((index, repo) in repos.withIndex()) {
//            val users = service.getRepoContributors(req.org, repo.name)
//                .also { logUsers(repo, it) }
//                .bodyList()
//
//            allUsers = (allUsers + users).aggregate()
//            updateResults(allUsers, index == repos.lastIndex)
//        }
    }
}
