package com.prj.data.di

import com.prj.data.repository.AppSettingsRepository
import com.prj.data.repository.FirebaseAuthRepository
import com.prj.data.repository.FirebaseFirestoreRepository
import com.prj.data.repository.ImageRepository
import com.prj.data.repository.JlptTestRepository
import com.prj.data.repository.KanjiClassifier
import com.prj.data.repository.KuromojiTextTokenizer
import com.prj.data.repository.LearningPreferenceRepository
import com.prj.data.repository.LoginStateRepository
import com.prj.data.repository.NotificationRepository
import com.prj.data.repository.TopicVocabularyRepository
import com.prj.data.repository.TranslationRepository
import com.prj.data.repository.TutorialStatusRepository
import com.prj.data.repository.UserStorageRepository
import com.prj.data.repository.WordRepository
import com.prj.domain.repository.IAppSettingsRepository
import com.prj.domain.repository.IAuthRepository
import com.prj.domain.repository.IImageRepository
import com.prj.domain.repository.IStorageRepository
import com.prj.domain.repository.IJlptTestRepository
import com.prj.domain.repository.IKanjiClassifier
import com.prj.domain.repository.ILearningPreferenceRepository
import com.prj.domain.repository.ILoginStateRepository
import com.prj.domain.repository.INotificationRepository
import com.prj.domain.repository.ITopicVocabularyRepository
import com.prj.domain.repository.ITranslationRepository
import com.prj.domain.repository.ITutorialStatusRepository
import com.prj.domain.repository.IUserStorageRepository
import com.prj.domain.repository.IWordRepository
import com.prj.domain.repository.TextTokenizer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindWordRepository(
        impl: WordRepository
    ): IWordRepository

    @Binds
    abstract fun bindTopicVocabularyRepository(
        impl: TopicVocabularyRepository
    ): ITopicVocabularyRepository

    @Binds
    abstract fun bindTranslationRepository(
        impl: TranslationRepository
    ): ITranslationRepository

    @Binds
    abstract fun bindFirebaseAuthRepository(
        impl: FirebaseAuthRepository
    ): IAuthRepository

    @Binds
    abstract fun bindFirebaseFirestoreRepository(
        impl: FirebaseFirestoreRepository
    ): IStorageRepository

    @Binds
    abstract fun bindJlptTestRepository(
        impl: JlptTestRepository
    ): IJlptTestRepository

    @Binds
    abstract fun bindUserStorageRepository(
        impl: UserStorageRepository
    ): IUserStorageRepository

    @Binds
    abstract fun bindLoginStateRepository (
        impl: LoginStateRepository
    ): ILoginStateRepository

    @Binds
    abstract fun bindKanjiClassifier (
        impl: KanjiClassifier
    ): IKanjiClassifier

    @Binds
    @Singleton
    abstract fun bindTextTokenizer(
        impl: KuromojiTextTokenizer
    ): TextTokenizer

    @Binds
    abstract fun bindTutorialStatusRepository(
        impl: TutorialStatusRepository
    ): ITutorialStatusRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(
        impl: ImageRepository
    ): IImageRepository

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(
        impl: AppSettingsRepository
    ): IAppSettingsRepository

    @Binds
    @Singleton
    abstract fun bindLearningPreferenceRepository(
        impl: LearningPreferenceRepository
    ): ILearningPreferenceRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepository
    ): INotificationRepository

}