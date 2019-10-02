from django.utils import timezone

from django.db import models

from django.conf import settings
from django.db.models.signals import post_save
from django.dispatch import receiver

from api.models import PurchaseStatus, GooglePlayProduct, AppStoreProduct


class ProfileManager(models.Manager):
    def create(self, *args, **kwargs):
        profile = super(ProfileManager, self).create(*args, **kwargs)
        profile.balance = kwargs.get('balance', settings.QUOTES_INITIAL_PROFILE_BALANCE)
        return profile

    def get_by_session(self, device_session):
        return Profile.objects.filter(device_sessions__pk__contains=device_session.pk)[0]


class Profile(models.Model):
    device_sessions = models.ManyToManyField('api.DeviceSession')
    balance = models.PositiveIntegerField(default=0)
    nickname = models.CharField(max_length=256, default='Пан Инкогнито')

    objects = ProfileManager()

    class Meta:
        verbose_name = 'профиль пользователя'
        verbose_name_plural = 'профили пользователей'


# todo: replace when auth get done
@receiver(post_save, sender='api.DeviceSession')
def create_profile_on_new_device_session(sender, instance, created, **kwargs):
    if not created:
        return
    session = instance
    profile = Profile.objects.create()
    profile.device_sessions.set([session,])
    profile.save()

@receiver(post_save, sender='api.GooglePlayIAPPurchase')
def recharge_profile_on_purchase(sender, instance, created, **kwargs):
    if created:
        return
    if instance.status != PurchaseStatus.VALID:
        return
    purchase = instance
    session = purchase.device_session
    # app_product = Product.objects.filter(google_play_product__pk=purchase.product.pk)[0]
    app_product = Product.objects.get_by_store_product(purchase.product)

    profile_to_recharge = Profile.objects.get_by_session(session)
    profile_to_recharge.balance = profile_to_recharge.balance + app_product.balance_recharge
    profile_to_recharge.save()


def _truncate(text, length=50, suffix='...'):
    '''
    >>> _truncate('Карл у Клары украл коралы', length=10, suffix='˚˚˚')
    >>> Карл у Кла˚˚˚
    '''
    return f'{text}'[:length] + (suffix if len(text) > length else '')


class QuoteAuthor(models.Model):
    name = models.CharField("Автор цитаты", max_length=256)

    def __str__(self):
        return _truncate(self.name)

    class Meta:
        verbose_name = 'автор цитат'
        verbose_name_plural = 'авторы цитат'


class Achievement(models.Model):
    icon_id = models.CharField("Имя иконки в приложении", max_length=256)
    title = models.CharField(max_length=256)
    received_text = models.TextField(default='')
    description_text = models.TextField(default='')


class Topic(models.Model):
    title = models.CharField("Название Темы", max_length=256)

    on_complete_achievement = models.ForeignKey(Achievement, on_delete=models.SET_NULL, null=True)


class QuoteCategory(models.Model):
    topic = models.ForeignKey(Topic, on_delete=models.CASCADE, default=None, null=True)
    title = models.CharField("Название Раздела", max_length=256)

    available_by_default = models.BooleanField(default=False)
    available_to_users = models.ManyToManyField(Profile)

    # language = models.ForeignKey(QuoteLanguage, "Язык категории")
    # Events
    is_event = models.BooleanField(default=False)
    event_due_date = models.DateTimeField(default=timezone.now)
    event_title = models.CharField(max_length=256, default='')
    event_description = models.TextField(default='')
    event_win_achievement = models.ForeignKey(Achievement, on_delete=models.SET_NULL, null=True)

    def __str__(self):
        return _truncate(f'{self.topic.title} > {self.title}')

    class Meta:
        verbose_name = 'категория цитат'
        verbose_name_plural = 'категории цитат'


class Quote(models.Model):
    text = models.CharField("Текст цитаты", max_length=256)
    author = models.ForeignKey(QuoteAuthor, on_delete=models.SET_NULL, null=True)
    category = models.ForeignKey(QuoteCategory, on_delete=models.SET_NULL, null=True)

    def __str__(self):
        return _truncate(self.text)

    class Meta:
        verbose_name = 'цитата'
        verbose_name_plural = 'цитаты'


class ProductManager(models.Manager):
    def get_by_store_product(self, store_product):
        if type(store_product) is GooglePlayProduct:
            return Product.objects.get(google_play_product=store_product)
        elif type(store_product) is AppStoreProduct:
            return Product.objects.get(app_store_product=store_product)
        else:
            raise Error('Unknown product type.')


class Product(models.Model):
    admin_title = models.CharField("Название для админки", max_length=256)
    balance_recharge = models.IntegerField("Сумма пополнения баланса", default=1)

    google_play_product = models.ForeignKey('api.GooglePlayProduct', on_delete=models.SET_NULL, null=True, blank=True)
    # app_store_product = models.ForeignKey('api.AppStoreProduct', on_delete=models.SET_NULL, null=True, blank=True)

    objects = ProductManager()

    class Meta:
        verbose_name = 'IAP продукт'
        verbose_name_plural = 'продукты'
