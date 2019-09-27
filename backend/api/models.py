from django.conf import settings
from django.db import models


class DeviceSession(models.Model):
    token = models.CharField("Токен идентификатор сессии", max_length=256)
    timestamp = models.DateTimeField(auto_now_add=True)


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
