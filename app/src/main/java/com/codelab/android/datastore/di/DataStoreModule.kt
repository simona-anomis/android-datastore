package com.codelab.android.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import com.codelab.android.datastore.UserPreferences
import com.codelab.android.datastore.data.UserPreferencesSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private const val USER_PREFERENCES_NAME = "user_preferences"
private const val DATA_STORE_FILE_NAME = "user_prefs.pb"
private const val SORT_ORDER_KEY = "sort_order"

@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {

    @Singleton
    @Provides
    fun provideProtoDataStore(@ApplicationContext appContext: Context): DataStore<UserPreferences> {
        return DataStoreFactory.create(
            serializer = UserPreferencesSerializer,
            produceFile = { appContext.dataStoreFile(DATA_STORE_FILE_NAME) },
            corruptionHandler = null,
            migrations = listOf(
                SharedPreferencesMigration(
                    appContext,
                    USER_PREFERENCES_NAME
                ) { sharedPrefs: SharedPreferencesView, currentData: UserPreferences ->
                    // Define the mapping from SharedPreferences to UserPreferences
                    if (currentData.sortOrder == UserPreferences.SortOrder.UNSPECIFIED) {
                        currentData.toBuilder().setSortOrder(
                            UserPreferences.SortOrder.valueOf(
                                sharedPrefs.getString(
                                    SORT_ORDER_KEY,
                                    UserPreferences.SortOrder.NONE.name
                                )!!
                            )
                        ).build()
                    } else {
                        currentData
                    }
                }
            ),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )
    }
}