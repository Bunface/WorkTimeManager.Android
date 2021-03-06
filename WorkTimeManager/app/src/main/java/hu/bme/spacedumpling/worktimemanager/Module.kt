package hu.bme.spacedumpling.worktimemanager.presentation.modules

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import hu.bme.spacedumpling.worktimemanager.android.baseUrl
import hu.bme.spacedumpling.worktimemanager.android.sharedData
import hu.bme.spacedumpling.worktimemanager.domain.api.APIService
import hu.bme.spacedumpling.worktimemanager.domain.api.NetworkDatasource
import hu.bme.spacedumpling.worktimemanager.domain.api.NetworkDatasourceImpl
import hu.bme.spacedumpling.worktimemanager.logic.login.CallInterceptor
import hu.bme.spacedumpling.worktimemanager.logic.login.LogoutHandler
import hu.bme.spacedumpling.worktimemanager.logic.login.LogoutHandlerImpl
import hu.bme.spacedumpling.worktimemanager.logic.models.Project
import hu.bme.spacedumpling.worktimemanager.logic.models.SimpleStatistic
import hu.bme.spacedumpling.worktimemanager.logic.repository.appsettings.AppSettingsModel
import hu.bme.spacedumpling.worktimemanager.logic.repository.appsettings.AppSettingsRepository
import hu.bme.spacedumpling.worktimemanager.logic.repository.appsettings.AppSettingsRepositoryImpl
import hu.bme.spacedumpling.worktimemanager.logic.repository.home.HomeRepository
import hu.bme.spacedumpling.worktimemanager.logic.repository.home.HomeRepositoryImpl
import hu.bme.spacedumpling.worktimemanager.logic.repository.home.HomeRepositoryModel
import hu.bme.spacedumpling.worktimemanager.logic.repository.projects.ProjectsRepository
import hu.bme.spacedumpling.worktimemanager.logic.repository.projects.ProjectsRepositoryImpl
import hu.bme.spacedumpling.worktimemanager.logic.repository.statistics.StatisticsRepository
import hu.bme.spacedumpling.worktimemanager.logic.repository.statistics.StatisticsRepositoryImpl
import hu.bme.spacedumpling.worktimemanager.presentation.page.home.HomeViewModel
import hu.bme.spacedumpling.worktimemanager.presentation.page.projects.ProjectDetailsViewModel
import hu.bme.spacedumpling.worktimemanager.presentation.page.projects.ProjectViewModel
import hu.bme.spacedumpling.worktimemanager.presentation.page.statistics.StatisticsViewModel
import hu.bme.spacedumpling.worktimemanager.util.CustomDateAdapter
import hu.uni.corvinus.my.app.data.datasources.base.DataSource
import hu.uni.corvinus.my.app.data.datasources.base.createDataSourceForListBasedObjects
import hu.uni.corvinus.my.app.data.datasources.base.createDataSourceForNonListBasedObjects
import okhttp3.OkHttpClient
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

enum class DatasourceTypes{
    PROJECTS,
    HOME,
    STATISTICS,
    APP_SETTINGS
}

val workTimeMangerModule = module{
//NETWORK


    factory<Moshi> {
        Moshi.Builder().apply {
            add(KotlinJsonAdapterFactory())
            add(CustomDateAdapter())
        }
            .build()
    }
    factory { MoshiConverterFactory.create(get()) }

    single {
        CallInterceptor(
            appSettingsRepository = get()
        )
    }

    factory {
        val interceptorFactory: CallInterceptor= get()
        OkHttpClient.Builder()
            .addInterceptor(interceptorFactory.createHeaderChangingInterceptor())
            .addInterceptor(interceptorFactory.createHeaderCatcherInterceptor())
            .build()
    }

    single{
        Retrofit.Builder()
            .client(get()).apply {
                addConverterFactory(get<MoshiConverterFactory>())
            }
            .baseUrl(baseUrl)
            .build()
    }

    factory {
        val retrofit = get<Retrofit>()
        retrofit.create(APIService::class.java)
    }

    single<NetworkDatasource> {
        NetworkDatasourceImpl(api = get())
    }

//LOCAL DATA
    factory {
        get<Context>().getSharedPreferences(
            sharedData,
            Context.MODE_PRIVATE
        )
    }

    single<DataSource<List<Project>>>(named(DatasourceTypes.PROJECTS.name)) {
        createDataSourceForListBasedObjects(
            defaultValue = null,
            typeForHandlingLists = Types.newParameterizedType(
                List::class.java,
                Project::class.java
            ),
            sharedPreferences = get(),
            moshi = get(),
            TAG = DatasourceTypes.PROJECTS.name
        )
    }

    single<DataSource<HomeRepositoryModel>>(named(DatasourceTypes.HOME.name)) {
       createDataSourceForNonListBasedObjects(
           defaultValue = null,
           sharedPreferences = get(),
           moshi = get(),
           TAG = DatasourceTypes.HOME.name
       )
    }

    single<DataSource<List<SimpleStatistic>>>(named(DatasourceTypes.STATISTICS.name)) {
        createDataSourceForListBasedObjects(
            defaultValue = null,
            typeForHandlingLists = Types.newParameterizedType(
                List::class.java,
                SimpleStatistic::class.java
            ),
            sharedPreferences = get(),
            moshi = get(),
            TAG = DatasourceTypes.STATISTICS.name
        )
    }

    single<DataSource<AppSettingsModel>>(named(DatasourceTypes.APP_SETTINGS.name)) {
        createDataSourceForNonListBasedObjects(
            defaultValue = null,
            sharedPreferences = get(),
            moshi = get(),
            TAG = DatasourceTypes.APP_SETTINGS.name
        )
    }

//REPOSITORY
    single<ProjectsRepository>{
        ProjectsRepositoryImpl(
            networkSource = get(),
            localDataSource = get(named(DatasourceTypes.PROJECTS.name)),
            appSettingsRepository = get()
        )
    }
    single<HomeRepository>{
        HomeRepositoryImpl(
            networkSource = get(),
            localDataSource = get(named(DatasourceTypes.HOME.name)),
            appSettingsRepository = get()
        )
    }

    single<StatisticsRepository>{
        StatisticsRepositoryImpl(
            networkSource = get(),
            localDataSource = get(named(DatasourceTypes.STATISTICS.name)),
            appSettingsRepository = get()
        )
    }

    single<AppSettingsRepository>{
        AppSettingsRepositoryImpl(
            localDataSource = get(named(DatasourceTypes.APP_SETTINGS.name))
        )
    }

    single<LogoutHandler>{
        LogoutHandlerImpl(
            appSettingsRepository = get(),
            homeRepository = get(),
            projectsRepository = get(),
            statisticsRepository = get()
        )
    }


//PRESENTATION
    viewModel{
        ProjectViewModel(
            projectsRepository = get(),
            appSettingsRepository = get()
        )
    }
    viewModel{ (projectId: Int) ->
        ProjectDetailsViewModel(
            projectId = projectId,
            projectsRepository = get(),
            appSettingsRepository = get()
        )
    }
    viewModel{
        HomeViewModel(
            homeRepository = get(),
            logoutHandler = get(),
            appSettingsRepository = get()
        )
    }
    viewModel{
        StatisticsViewModel(
            statisticsRepository = get(),
            appSettingsRepository = get()
        )
    }
}