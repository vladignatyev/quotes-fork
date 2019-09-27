import re

from django.conf import settings
from django.db import models



class DeviceSession(models.Model):
    token = models.CharField("Токен идентификатор сессии", max_length=256)
    timestamp = models.DateTimeField(auto_now_add=True)

    @classmethod
    def is_valid_token(cls, token, valid_chars=r'^[a-z|A-Z|0-9]{32}$'):
        # todo:
        is_empty = token == ''
        is_match_rules = re.match(valid_chars, token) is not None

        return not is_empty and is_match_rules


    @classmethod
    def create_from_token(cls, token):
        if not cls.is_valid_token(token):
            return
        session = DeviceSession.objects.create(token=token)
        session.save()
        return session

    def __str__(self):
        return f'{self.token}'


class PushSubscription(models.Model):
    device_session = models.ForeignKey(DeviceSession, on_delete=models.CASCADE)
    token = models.CharField(max_length=256)


class GooglePlayIAPSubscription(models.Model):
    pass


class GooglePlayIAPPurchase(models.Model):
    pass


class AppStoreIAPSubscription(models.Model):
    pass


class AppStoreIAPPurchase(models.Model):
    pass


class Credentials(models.Model):
    google_play_bundle_id = models.CharField("Play Market Bundle ID", max_length=256)
    google_play_api_key = models.CharField("Google Play API Key", max_length=256)

    appstore_bundle_id = models.CharField("AppStore Bundle ID", max_length=256)

    class Meta:
        verbose_name = 'Credentials'
        verbose_name_plural = 'Credentials'
