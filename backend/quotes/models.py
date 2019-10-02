from django.utils import timezone

from django.db import models

from django.conf import settings
from django.db.models.signals import post_save

from api.models import PurchaseStatus, GooglePlayProduct, AppStoreProduct


class ProfileManager(models.Manager):
    def create(self, *args, **kwargs):
        profile = super(ProfileManager, self).create(*args, **kwargs)
        game_settings = GameBalance.objects.get_actual_game_settings()
        profile.balance = kwargs.get('balance', game_settings.initial_profile_balance)
        return profile

    def get_by_session(self, device_session):
        return Profile.objects.filter(device_sessions__pk__contains=device_session.pk)[0]

    # todo:
    def buy_unlock_category(self, profile, quote_category):
        balance = profile.balance
        price = quote_category.price_to_unlock
        new_balance = balance - price
        profile.balance = new_balance


class Profile(models.Model):
    device_sessions = models.ManyToManyField('api.DeviceSession')
    balance = models.PositiveIntegerField(default=0)
    nickname = models.CharField(max_length=256, default='Пан Инкогнито')

    settings = models.ForeignKey('GameBalance', on_delete=models.CASCADE, default=0)  # todo: default first one GameBalance

    objects = ProfileManager()

    class Meta:
        verbose_name = 'профиль пользователя'
        verbose_name_plural = 'профили пользователей'


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


class Section(models.Model):
    title = models.CharField("Название Раздела", max_length=256)
    topic = models.ForeignKey(Topic, on_delete=models.CASCADE)

    on_complete_achievement = models.ForeignKey(Achievement, on_delete=models.SET_NULL, null=True)


class QuoteCategory(models.Model):
    section = models.ForeignKey(Section, on_delete=models.CASCADE, default=None, null=True)
    title = models.CharField("Название Категории", max_length=256)

    # Events
    is_event = models.BooleanField(default=False)
    event_due_date = models.DateTimeField(default=timezone.now)
    event_title = models.CharField(max_length=256, default='')
    event_description = models.TextField(default='')
    event_win_achievement = models.ForeignKey(Achievement, on_delete=models.SET_NULL, null=True)

    # Locked category
    is_payable = models.BooleanField('Категория платная?', default=False)
    price_to_unlock = models.BigIntegerField('Стоимость открытия категории если она платная', default=0)

    available_to_users = models.ManyToManyField(Profile, verbose_name='Профили пользователей которым доступна категория')


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


class GameBalanceManager(models.Manager):
    def get_actual_game_settings(self):
        try:
            return GameBalance.objects.order_by('-pk')[0]
        except IndexError:
            return self.create(initial_profile_balance=0)


class GameBalance(models.Model):
    initial_profile_balance = models.BigIntegerField(default=0)

    objects = GameBalanceManager()
