from django.conf import settings
from django.apps import AppConfig


class ApiConfig(AppConfig):
    name = 'api'
    verbose_name = 'Mobile API v' + str(settings.API_VERSION)
