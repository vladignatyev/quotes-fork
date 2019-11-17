from django.apps import AppConfig

from django.db.models.signals import post_save


class QuoterankConfig(AppConfig):
    name = 'quoterank'
    verbose_name = 'Quotes Ranking'

    def ready(self):
        from .models import create_ranking_for_profile

        post_save.connect(create_ranking_for_profile, sender='quotes.Profile')
