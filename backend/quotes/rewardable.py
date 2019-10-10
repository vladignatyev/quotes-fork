from django.db import models

from .events import UserEvents


class RewardableEntity(models.Model):
    class Meta:
        abstract = True

    on_complete_achievement = models.ForeignKey('Achievement', on_delete=models.SET_NULL, null=True, blank=True)
    bonus_reward = models.BigIntegerField(default=0, verbose_name='Бонус монет за прохождение')
    complete_by_users = models.ManyToManyField('Profile', verbose_name='Юзеры которые прошли и должны получить вознаграждение', blank=True,
                                                          related_name="%(app_label)s_%(class)s_complete_by_users",
                                                          related_query_name="%(app_label)s_%(class)s_complete_by_users_objs")

    complete_event_name = UserEvents.COMPLETE
    achievement_event_name = UserEvents.RECEIVED_GENERIC_ACHIEVEMENT
    reward_event_name = UserEvents.RECEIVED_GENERIC_REWARD

    def get_reward(self, profile):
        return self.bonus_reward

    def is_completion_condition_met_by(self, profile):
        return False

    def handle_complete(self, profile):
        self.complete_by_users.add(profile)
        self.save()

        user_events = [UserEvents.new(self.complete_event_name, self.pk)]

        if self.get_reward(profile) > 0:
            user_events += self.process_rewards(profile)

        if self.on_complete_achievement:
            user_events += self.process_achievements(profile)

        return user_events

    def process_achievements(self, profile):
        AchievementReceiving.create(achievement=self.on_complete_achievement,
                                    profile=profile)
        return [UserEvents.new(self.achievement_event_name,
                               self.on_complete_achievement.pk)]

    def process_rewards(self, profile):
        reward = self.get_reward(profile)
        profile.balance = profile.balance + reward
        profile.save()

        return [UserEvents.new(self.reward_event_name, reward)]
