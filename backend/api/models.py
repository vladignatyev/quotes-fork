import re
import hashlib
from functools import lru_cache

from django.conf import settings
from django.db import models

from django.db.models.signals import post_save


from .crypto import generate_secret


class DeviceSessionManager(models.Manager):
    def is_valid_token(self, token, valid_chars=r'^[a-z|A-Z|0-9]{32}$'):
        is_empty = token == ''
        is_match_rules = re.match(valid_chars, token) is not None

        return not is_empty and is_match_rules


    def create_from_token(self, token):
        if not self.is_valid_token(token):
            return
            
        session = DeviceSession.objects.create(token=token)
        session.save()
        return session


class DeviceSession(models.Model):
    token = models.CharField("Токен идентификатор сессии", max_length=256, unique=True)
    timestamp = models.DateTimeField(auto_now_add=True)

    objects = DeviceSessionManager()

    def __str__(self):
        return f'{self.token}'


class PushSubscription(models.Model):
    device_session = models.ForeignKey(DeviceSession, on_delete=models.CASCADE)
    token = models.CharField(max_length=256)


class GooglePlayIAPSubscription(models.Model):
    pass


class AppStoreIAPSubscription(models.Model):
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
    type = models.CharField('Type: reward video or purchase', choices=PurchaseTypes.choices, default=PurchaseTypes.DEFAULT, max_length=16)
    status = models.CharField('Validation status', choices=PurchaseStatus.choices, default=PurchaseStatus.DEFAULT, max_length=16)
    device_session = models.ForeignKey(DeviceSession, on_delete=models.CASCADE, null=True)

    class Meta:
        abstract = True

    def validate(self):
        pass

    def is_valid(self):
        return self.status == PurchaseStatus.VALID


class GooglePlayProduct(models.Model):
    # according to: https://developer.android.com/google/play/billing/billing_library_overview
    sku = models.CharField('IAP SKU (Product ID)', max_length=256, blank=True)

    def __str__(self):
        return f'{self.sku}'


class AppStoreProduct(models.Model):
    pass


class GooglePlayIAPPurchase(Purchase):
    product = models.ForeignKey(GooglePlayProduct, on_delete=models.SET_NULL, null=True, blank=True)

    # according to: https://developer.android.com/google/play/billing/billing_overview
    purchase_token = models.CharField(max_length=256, blank=True)
    order_id = models.CharField(max_length=256, blank=True)

    date_created = models.DateTimeField(auto_now_add=True)
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


class Credentials(models.Model):
    google_play_bundle_id = models.CharField("Play Market Bundle ID", default='', max_length=256)
    google_play_api_key = models.CharField("Google Play API Key", default='', max_length=256)
    # appstore_bundle_id = models.CharField("AppStore Bundle ID", default='', max_length=256)

    date_added = models.DateTimeField(auto_now_add=True)

    server_secret = models.CharField("Server-only secret", max_length=256, default=generate_secret)
    shared_secret = models.CharField("Shared secret", max_length=256, default=generate_secret)

    objects = CredentialsManager()

    class Meta:
        verbose_name = 'Credentials'
        verbose_name_plural = 'Credentials'


@lru_cache(maxsize=1)
def get_shared_secret():
    credentials = Credentials.objects.get()
    return str(credentials.shared_secret)

@lru_cache(maxsize=1)
def get_server_secret():
    credentials = Credentials.objects.get()
    return str(credentials.server_secret)

def clean_tokens_lru_cache(*args, **kwargs):
    get_shared_secret.cache_clear()
    get_server_secret.cache_clear()

post_save.connect(clean_tokens_lru_cache, sender='api.Credentials')


def generate_signature(device_token, timestamp):
    shared_secret = get_shared_secret()

    h = hashlib.sha256()
    h.update(shared_secret.encode('utf-8'))
    masked_shared_secret = h.hexdigest()

    sequence = f'{device_token}|{timestamp}'

    h2 = hashlib.sha256()
    h2.update(masked_shared_secret.encode('utf-8'))
    h2.update(sequence.encode('utf-8'))

    signature = h2.hexdigest()
    return signature


def check_signature(device_token, timestamp, signature):
    return generate_signature(device_token, timestamp) == signature
