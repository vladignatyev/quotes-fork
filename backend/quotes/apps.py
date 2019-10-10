from django.apps import AppConfig

from django.db.models.signals import post_save


class QuotesConfig(AppConfig):
    name = 'quotes'

    verbose_name = 'Quotes Guessing App'

    def ready(self):
        from .signals import create_profile_on_new_device_session, recharge_profile_on_purchase, unlock_category_on_purchase
        from .models import clean_unlock_cache, clean_content_cache

        post_save.connect(create_profile_on_new_device_session, sender='api.DeviceSession')

        post_save.connect(recharge_profile_on_purchase, sender='api.GooglePlayIAPPurchase')
        post_save.connect(unlock_category_on_purchase, sender='api.GooglePlayIAPPurchase')

        post_save.connect(clean_unlock_cache, sender='quotes.CategoryUnlockPurchase')

        post_save.connect(clean_content_cache, sender='quotes.Topic')
        post_save.connect(clean_content_cache, sender='quotes.Section')
        post_save.connect(clean_content_cache, sender='quotes.QuoteCategory')
        post_save.connect(clean_content_cache, sender='quotes.Quote')
