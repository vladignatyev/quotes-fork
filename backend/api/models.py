import uuid
import re

from functools import lru_cache

from django.conf import settings
from django.db import models

from django.db.models.signals import post_save

from .crypto import *


from longjob.models import LongJobQueueItem


class DeviceSessionManager(models.Manager):
    def is_valid_token(self, token, valid_chars=r'^[a-z|A-Z|0-9]+$'):
        is_empty = token == ''
        is_match_rules = re.match(valid_chars, token) is not None

        return not is_empty and is_match_rules


    def create_from_token(self, token):
        if not self.is_valid_token(token):
            return

        session = self.create(token=token, auth_token=generate_auth_token())
        return session


class DeviceSession(models.Model):
    token = models.CharField("Токен устройства", max_length=256, unique=True)
    auth_token = models.CharField("Токен идентификатор сессии", max_length=256, unique=True, default='')
    timestamp = models.DateTimeField(auto_now_add=True)

    objects = DeviceSessionManager()

    def __str__(self):
        # if self.timestamp:
        #     return f'{self.token} @ {self.timestamp:%Y-%m-%d %H:%M:%S}'
        # else:
        return f'{self.token}'# @ {self.timestamp}'


class PushSubscription(models.Model):
    device_session = models.ForeignKey(DeviceSession, on_delete=models.CASCADE)
    token = models.CharField(max_length=256)


class PushNotificationQueueItem(LongJobQueueItem):
    # todo: switch to many to many for using FCM batch send
    push_subscription = models.ForeignKey(PushSubscription, on_delete=models.CASCADE, null=True)

    title = models.CharField(max_length=256, blank=True, default='')
    body = models.TextField(blank=True, default='')
    image_url = models.CharField(max_length=256, blank=True, default='')

    data = models.TextField(blank=True, default='')
    topic = models.CharField(max_length=256, blank=True, null=True, default='')
    condition = models.CharField(max_length=256, blank=True, null=True, default='')

    is_broadcast = models.BooleanField(default=False, blank=True)



class GooglePlayIAPSubscription(models.Model):
    pass


class AppStoreIAPSubscription(models.Model):
    pass


class PurchaseTypes:
    DEFAULT = PURCHASE = 'purchase'
    VIDEO = 'video'

    choices = (('video', 'Reward Video'),
               ('purchase', 'Purchase'))


class PurchaseStatus:
    DEFAULT = UNKNOWN = 'unknown'
    INVALID = 'invalid'

    PURCHASED = 'purchased'
    CANCELLED = 'cancelled'

    choices = (('unknown', 'Unknown'),
               ('invalid', 'Invalid'),
               ('purchased', 'Purchased'),
               ('cancelled', 'Cancelled'))


class Purchase(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    type = models.CharField('Type: reward video or purchase', choices=PurchaseTypes.choices, default=PurchaseTypes.DEFAULT, max_length=16)
    previous_status = models.CharField('Validation status before validation occured', choices=PurchaseStatus.choices, default=PurchaseStatus.DEFAULT, max_length=16)
    status = models.CharField('Validation status', choices=PurchaseStatus.choices, default=PurchaseStatus.DEFAULT, max_length=16)
    device_session = models.ForeignKey(DeviceSession,
        on_delete=models.CASCADE, null=True,
        related_name="%(app_label)s_%(class)s_related",
        related_query_name="%(app_label)s_%(class)ss",
    )

    class Meta:
        abstract = True


class GooglePlayProduct(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    # according to: https://developer.android.com/google/play/billing/billing_library_overview
    sku = models.CharField('IAP SKU (Product ID)', max_length=256, blank=True)
    is_rewarded_product = models.BooleanField(default=False, blank=True)

    def __str__(self):
        return f'{self.sku}'


class AppStoreProduct(models.Model):
    pass


class GooglePlayIAPPurchase(Purchase):
    product = models.ForeignKey(GooglePlayProduct, on_delete=models.SET_NULL, null=True, blank=True)

    # according to: https://developer.android.com/google/play/billing/billing_overview
    purchase_token = models.CharField(max_length=256, blank=True)

    # should be unique, see https://developer.android.com/google/play/billing/billing_best_practices#validating-purchase-server
    order_id = models.CharField(max_length=256, blank=True, unique=True)

    date_created = models.DateTimeField(auto_now_add=True, blank=True)
    date_updated = models.DateTimeField(auto_now=True)


class AppStoreIAPPurchase(Purchase):
    product = models.ForeignKey(AppStoreProduct, on_delete=models.SET_NULL, null=True, blank=True)


class CredentialsManager(models.Manager):
    def get(self):
        try:
            return self.latest('date_added')
        except self.model.DoesNotExist:
            instance = self.create()
            instance.save()
            return instance

def generate_secret(secret_key=None):
    return crypto_generate_secret(settings.SECRET_KEY)

class Credentials(models.Model):
    google_play_bundle_id = models.CharField("Play Market Bundle ID", default='', max_length=256)
    # google_play_api_key = models.CharField("Google Play API Key", default='', max_length=256)
    # appstore_bundle_id = models.CharField("AppStore Bundle ID", default='', max_length=256)

    date_added = models.DateTimeField(auto_now_add=True)

    server_secret = models.CharField("Server-only secret", max_length=256, default=generate_secret)
    shared_secret = models.CharField("Shared secret", max_length=256, default=generate_secret)

    objects = CredentialsManager()

    class Meta:
        verbose_name = 'Credentials'
        verbose_name_plural = 'Credentials'

    def __str__(self):
        return f'{self.date_added:%Y-%m-%d %H:%M:%S}: {self.google_play_bundle_id}'



def get_shared_secret():
    credentials = Credentials.objects.get()
    return str(credentials.shared_secret)

def get_server_secret():
    credentials = Credentials.objects.get()
    return str(credentials.server_secret)


# def clean_tokens_lru_cache(*args, **kwargs):
#     get_shared_secret.cache_clear()
#     get_server_secret.cache_clear()
#
# post_save.connect(clean_tokens_lru_cache, sender='api.Credentials')

def generate_signature(device_token, timestamp):
    shared_secret = get_shared_secret()
    return crypto_generate_signature(device_token, timestamp, shared_secret)


def check_signature(device_token, timestamp, signature):
    shared_secret = get_shared_secret()
    return crypto_check_signature(device_token, timestamp, signature, shared_secret)


def check_auth_token(auth_token):
    server_secret = get_server_secret()
    return crypto_check_auth_token(auth_token, server_secret)


def generate_auth_token():
    return crypto_generate_auth_token(settings.SECRET_KEY, get_server_secret())


def sign_auth_token(token):
    return crypto_sign_auth_token(token, get_server_secret())
