package com.langoa.app.ui.screens.learn

import com.langoa.app.domain.model.LessonReward
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardResultStore @Inject constructor() {
    var lastReward: LessonReward? = null
}
