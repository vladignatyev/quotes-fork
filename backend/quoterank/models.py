from django.db import models
from quotes.events import UserEvents

from .core import calculate_quote_rank_v1_0

from django.utils import timezone


class ProfileRankManager(models.Manager):
    pass
    # def create_if_not_exist(self, profile):
    #     o, created = ProfileRank.objects.get_or_create(profile=profile)
    #     return o


def create_ranking_for_profile(sender, instance, created, **kwargs):
    if not created:
        return
    pr = ProfileRank.objects.create(profile=instance)
    pr.save()


class ProfileRank(models.Model):
    profile = models.OneToOneField(
        'quotes.Profile',
        on_delete=models.CASCADE,
        primary_key=True
    )

    last_update = models.DateTimeField(auto_now=True)#, default=timezone.now)
    joined_at = models.DateTimeField(auto_now_add=True)#, default=timezone.now)  # todo: signal on profile first save!

    total_quote_reward = models.PositiveIntegerField(default=0)
    total_category_reward = models.PositiveIntegerField(default=0)
    total_section_reward = models.PositiveIntegerField(default=0)
    total_topic_reward = models.PositiveIntegerField(default=0)

    total_quote_achievements_received = models.PositiveIntegerField(default=0)
    total_category_achievements_received = models.PositiveIntegerField(default=0)
    total_section_achievements_received = models.PositiveIntegerField(default=0)
    total_topic_achievements_received = models.PositiveIntegerField(default=0)

    rank_cached = models.PositiveIntegerField(default=0)

    rank_change_since_last_update = models.BigIntegerField(default=0)
    position_change_since_last_update = models.BigIntegerField(default=0)


    objects = ProfileRankManager()

    def update_by_events(self, occured_events):
        events = []
        rank_before = self.rank_cached

        for name, value in occured_events:
            if name == UserEvents.RECEIVED_PER_LEVEL_REWARD:
                self.total_quote_reward += value
            if name == UserEvents.RECEIVED_PER_CATEGORY_REWARD:
                self.total_category_reward += value
            if name == UserEvents.RECEIVED_PER_SECTION_REWARD:
                self.total_section_reward += value
            if name == UserEvents.RECEIVED_PER_TOPIC_REWARD:
                self.total_topic_reward += value

            if name == UserEvents.RECEIVED_LEVEL_ACHIEVEMENT:
                self.total_quote_achievements_received += 1
            if name == UserEvents.RECEIVED_CATEGORY_ACHIEVEMENT:
                self.total_category_achievements_received += 1
            if name == UserEvents.RECEIVED_SECTION_ACHIEVEMENT:
                self.total_section_achievements_received += 1
            if name == UserEvents.RECEIVED_TOPIC_ACHIEVEMENT:
                self.total_topic_achievements_received += 1

        new_rank = int(calculate_quote_rank_v1_0(self))

        rank_diff = new_rank - self.rank_cached

        position_before = self.get_position_in_overall_top_by_rank(self.rank_cached)
        new_position = self.get_position_in_overall_top_by_rank(new_rank)

        position_diff = new_position - position_before

        events += [(UserEvents.QUOTERANK_POSITION_IN_TOP_CHANGED, position_diff)]
        if int(rank_diff) > 0:
            events += [(UserEvents.QUOTERANK_RANK_CHANGED, int(rank_diff))]

        self.rank_cached = new_rank
        self.rank_change_since_last_update = rank_diff
        self.position_change_since_last_update = position_diff
        self.save()

        return events

    def refresh_changes(self):
        self.rank_change_since_last_update = 0
        self.position_change_since_last_update = 0
        self.save()

    def get_position_in_overall_top_by_rank(self, rank):
        return ProfileRank.objects.filter(rank_cached__gt=rank).count() + 1

    @classmethod
    def get_top(self, profile_infocus, top_up=5, top_down=5):
        pr = ProfileRank.objects.select_related('profile').get(profile=profile_infocus)
        return list(ProfileRank.objects.select_related('profile').filter(rank_cached__gt=pr.rank_cached).order_by('-rank_cached')[:top_up]) + \
        [ pr ] + \
        list(ProfileRank.objects.select_related('profile').filter(rank_cached__lt=pr.rank_cached).order_by('-rank_cached')[:top_down])
