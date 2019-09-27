from django.db import models

from django.conf import settings


class Profile(models.Model):
    device_sessions = models.ManyToManyField('api.DeviceSession')
    first_login_ipaddr = models.GenericIPAddressField()


class QuoteCategory(models.Model):
    title = models.CharField("Название категории", max_length=256)
    language = models.CharField("Язык категории", max_length=2,
                                choices=settings.QUOTE_LANGUAGES)


class Quote(models.Model):
    list_display = ('text', 'pub_date')

    text = models.CharField("Текст цитаты", max_length=256)
    author = models.CharField("Автор цитаты", max_length=256)

    category = models.ForeignKey(QuoteCategory, on_delete=models.SET_NULL, null=True)
