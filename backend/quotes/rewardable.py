from django.apps import apps
from django.db import models

from .events import UserEvents


class RewardableEntity(models.Model):
    class Meta:
        abstract = True

    on_complete_achievement = models.ForeignKey('Achievement', verbose_name='Достижение выдаваемое при прохождении', on_delete=models.SET_NULL, null=True, blank=True)
    bonus_reward = models.BigIntegerField(default=0, verbose_name='Бонус монет за прохождение')
    complete_by_users = models.ManyToManyField('Profile', verbose_name='Юзеры которые прошли и должны получить вознаграждение', blank=True,
                                                          related_name="%(app_label)s_%(class)s_complete_by_users",
                                                          related_query_name="%(app_label)s_%(class)s_complete_by_users_objs")

    complete_event_name = UserEvents.COMPLETE
    achievement_event_name = UserEvents.RECEIVED_GENERIC_ACHIEVEMENT
    reward_event_name = UserEvents.RECEIVED_GENERIC_REWARD

    def get_reward(self, profile):
        return self.bonus_reward

    def handle_complete(self, profile, save_profile=True):
        self.complete_by_users.add(profile)
        # self.save()

        user_events = [UserEvents.new(self.complete_event_name, self.pk)]

        if self.get_reward(profile) > 0:
            user_events += self.process_rewards(profile, save_profile=save_profile)

        if self.on_complete_achievement:
            user_events += self.process_achievements(profile)

        return user_events

    def process_achievements(self, profile):
        AchievementReceiving = apps.get_model('quotes.AchievementReceiving')
        ar = AchievementReceiving.objects.create(achievement=self.on_complete_achievement,
                                            profile=profile)
        ar.save()                                    
        return [UserEvents.new(self.achievement_event_name,
                               self.on_complete_achievement.pk)]

    def process_rewards(self, profile, save_profile=True):
        """Updates user balance given reward

        save_profile -- hook for deferred save of Profile to avoid cascade
                        repeating saves on completing last quote in last
                        category in last section of topic
        """
        reward = self.get_reward(profile)
        profile.balance = profile.balance + reward

        if save_profile:
            profile.save()

        return [UserEvents.new(self.reward_event_name, reward)]
