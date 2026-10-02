package com.langoa.app.di

import com.langoa.app.data.repository.AuthRepositoryImpl
import com.langoa.app.data.repository.CivilizationRepositoryImpl
import com.langoa.app.data.repository.LearningRepositoryImpl
import com.langoa.app.data.repository.SubscriptionRepositoryImpl
import com.langoa.app.domain.repository.AuthRepository
import com.langoa.app.domain.repository.CivilizationRepository
import com.langoa.app.domain.repository.LearningRepository
import com.langoa.app.domain.repository.SubscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLearningRepository(impl: LearningRepositoryImpl): LearningRepository

    @Binds
    @Singleton
    abstract fun bindCivilizationRepository(impl: CivilizationRepositoryImpl): CivilizationRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: SubscriptionRepositoryImpl): SubscriptionRepository
}
