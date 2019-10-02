from django.apps import AppConfig

from django.db.models.signals import post_save


class QuotesConfig(AppConfig):
    name = 'quotes'

    verbose_name = 'Quotes Guessing App'

    def ready(self):
        from .signals import create_profile_on_new_device_session, recharge_profile_on_purchase
        
        post_save.connect(create_profile_on_new_device_session, sender='api.DeviceSession')
        post_save.connect(recharge_profile_on_purchase, sender='api.GooglePlayIAPPurchase')
