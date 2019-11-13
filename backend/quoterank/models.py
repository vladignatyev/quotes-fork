from django.db import models


class ProfileRankManager(models.Manager):
    def create_if_not_exist(self, profile):
        o, created = ProfileRank.objects.get_or_create(profile=profile)
        return o



class ProfileRank(models.Model):
    profile = models.OneToOneField(
        'quotes.Profile',
        on_delete=models.CASCADE,
        primary_key=True
    )

    objects = ProfileRankManager()

    def update_by_events(self, occured_events):
        self.save()
        return []
