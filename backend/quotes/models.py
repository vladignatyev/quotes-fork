from django.db import models

from django.conf import settings
from django.db.models.signals import post_save
from django.dispatch import receiver

# from ..api.models import DeviceSession


class Profile(models.Model):
    device_sessions = models.ManyToManyField('api.DeviceSession')

    class Meta:
        verbose_name = 'профиль пользователя'
        verbose_name_plural = 'профили пользователей'

@receiver(post_save, sender='api.DeviceSession')
def create_profile_on_new_device_session(sender, instance, created, **kwargs):
    if not created:
        return
    session = instance
    profile = Profile.objects.create()
    profile.device_sessions.set([session,])
    profile.save()


def _truncate(text, length=50, suffix='...'):
    '''
    >>> _truncate('Карл у Клары украл коралы', length=10, suffix='˚˚˚')
    >>> Карл у Кла˚˚˚
    '''
    return f'{text}'[:length] + (suffix if len(text) > length else '')


class QuoteAuthor(models.Model):
    name = models.CharField("Автор цитаты", max_length=256)

    def __str__(self):
        return _truncate(self.author)

    class Meta:
        verbose_name = 'автор цитат'
        verbose_name_plural = 'авторы цитат'


class QuoteCategory(models.Model):
    title = models.CharField("Название категории", max_length=256)
    language = models.CharField("Язык категории", max_length=2,
                                choices=settings.QUOTE_LANGUAGES)

    def __str__(self):
        return _truncate(self.title)

    class Meta:
        verbose_name = 'категория цитат'
        verbose_name_plural = 'категории цитат'


class Quote(models.Model):
    text = models.CharField("Текст цитаты", max_length=256)
    author = models.ForeignKey(QuoteAuthor, on_delete=models.SET_NULL, null=True, blank=True)
    category = models.ForeignKey(QuoteCategory, on_delete=models.SET_NULL, null=True, blank=True)

    def __str__(self):
        return _truncate(self.text)

    class Meta:
        verbose_name = 'цитата'
        verbose_name_plural = 'цитаты'
