from django.apps import apps
from django.db import models

from django.forms.models import model_to_dict


from api.models import DeviceSession, GooglePlayProduct, AppStoreProduct


class TopicManager(models.Manager):
    topic_fields = ['title']
    section_fields = ['title', 'id', 'on_complete_achievement']
    category_fields = ['title', 'id', 'on_complete_achievement', 'icon',
                       'is_payable', 'price_to_unlock',
                       'is_event', 'event_due_date', 'event_title', 'event_icon',
                       'event_description', 'event_win_achievement']


class BaseProductManager(models.Manager):
     def get_queryset(self):
        return super().get_queryset().select_related('google_play_product') #filter(author='Roald Dahl')
    # def get_by_store_product(self, store_product):
    #     if type(store_product) is GooglePlayProduct:
    #         return apps.get_model('quotes.BalanceRechargeProduct').objects.get(google_play_product=store_product)
    #     elif type(store_product) is AppStoreProduct:
    #         return apps.get_model('quotes.BalanceRechargeProduct').objects.get(app_store_product=store_product)
    #     else:
    #         raise Error('Unknown product type.')


class GameBalanceManager(models.Manager):
    def get_actual(self):
        # TODO: implement LRU Cache in a manner it has been done in `api` module
        try:
            return self.model.objects.latest('pk')
        except self.model.DoesNotExist:
            game_settings = self.create(initial_profile_balance=0)
            return game_settings


class ProfileManager(models.Manager):
    def create(self, *args, **kwargs):
        profile = super(ProfileManager, self).create(*args, **kwargs)

        game_settings = apps.get_model('quotes.GameBalance').objects.get_actual()

        profile.balance = kwargs.get('balance', game_settings.initial_profile_balance)
        profile.nickname = kwargs.get('nickname', '')

        return profile

    def get_by_session(self, device_session):
        return self.get(device_sessions__pk__contains=device_session.pk)

    def get_by_token(self, device_session_token):
        return self.get(device_sessions__token__contains=device_session_token)

    def get_by_auth_token(self, auth_token):
        session = DeviceSession.objects.get(auth_token=auth_token)
        return self.get_by_session(session)
