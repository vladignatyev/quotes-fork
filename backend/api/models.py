import re

from django.conf import settings
from django.db import models



class DeviceSession(models.Model):
    token = models.CharField("Токен идентификатор сессии", max_length=256)
    timestamp = models.DateTimeField(auto_now_add=True)

    @classmethod
    def is_valid_token(cls, token, valid_chars=r'^[a-z|A-Z|0-9]{32}$'):
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


class PurchaseTypes:
    DEFAULT = 'purchase'

    choices = (('video', 'Reward Video'),
               ('purchase', 'Purchase'))


class PurchaseStatus:
    DEFAULT = UNKNOWN = 'unknown'
    VALID = 'valid'

    choices = (('unknown', 'Unknown'),
               ('valid', 'Valid'),
               ('invalid', 'Invalid'))


class Purchase(models.Model):
    type = models.CharField(choices=PurchaseTypes.choices, default=PurchaseTypes.DEFAULT, max_length=16)
    status = models.CharField(choices=PurchaseStatus.choices, default=PurchaseStatus.DEFAULT, max_length=16)
    device_session = models.ForeignKey(DeviceSession, on_delete=models.CASCADE, null=True)

    class Meta:
        abstract = True

    def validate(self):
        pass

    def is_valid(self):
        return self.status == PurchaseStatus.VALID


class GooglePlayProduct(models.Model):
    pass


class AppStoreProduct(models.Model):
    pass


class GooglePlayIAPPurchase(Purchase):
    product = models.ForeignKey(GooglePlayProduct, on_delete=models.SET_NULL, null=True, blank=True)


class AppStoreIAPSubscription(models.Model):
    pass


class AppStoreIAPPurchase(Purchase):
    product = models.ForeignKey(AppStoreProduct, on_delete=models.SET_NULL, null=True, blank=True)


class Credentials(models.Model):
    google_play_bundle_id = models.CharField("Play Market Bundle ID", max_length=256)
    google_play_api_key = models.CharField("Google Play API Key", max_length=256)

    appstore_bundle_id = models.CharField("AppStore Bundle ID", max_length=256)

    class Meta:
        verbose_name = 'Credentials'
        verbose_name_plural = 'Credentials'
