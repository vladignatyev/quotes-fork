from django.conf import settings
from django.apps import AppConfig
from django.core.checks import Error, Warning, register


class ApiConfig(AppConfig):
    name = 'api'
    verbose_name = 'Mobile API v' + str(settings.API_VERSION)


@register()
def check_settings(app_configs, **kwargs):
    errors = []

    if getattr(settings, 'API_VERSION', None) is None:
        errors += [(
            Error(
                'To work properly Mobile API requires API_VERSION setting.',
                hint='Provide API_VERSION in your Django `settings.py`',
                obj=None,
                id='api.E001',
            )
        )]

    if getattr(settings, 'GOOGLE_APPLICATION_CREDENTIALS', None) is None:
        errors += [(
            Warning(
                'Missed GOOGLE_APPLICATION_CREDENTIALS in your `settings.py`',
                hint='If you are using Push Notifications feature for Android apps, please provide GOOGLE_APPLICATION_CREDENTIALS path to Service Account credentials in your Django `settings.py`',
                obj=None,
                id='api.W001',
            )
        )]


    if getattr(settings, 'GOOGLE_IAP_CREDENTIALS', None) is None:
        errors += [(
            Warning(
                'Missed GOOGLE_IAP_CREDENTIALS in your `settings.py`',
                hint='If you are using IAP Validation feature for Android apps, please provide GOOGLE_IAP_CREDENTIALS path to Service Account credentials in your Django `settings.py`',
                obj=None,
                id='api.W002',
            )
        )]

    if getattr(settings, 'GOOGLE_PLAY_BUNDLE_ID', None) is None:
        errors += [(
            Warning(
                'Missed GOOGLE_PLAY_BUNDLE_ID in your `settings.py`',
                hint='If you are using IAP Validation feature for Android apps, please provide GOOGLE_PLAY_BUNDLE_ID path to Service Account credentials in your Django `settings.py`',
                obj=None,
                id='api.W003',
            )
        )]

    if getattr(settings, 'SECRET_KEY', None) is None:
        errors += [(
            Error(
                'Missed SECRET_KEY in your `settings.py`',
                hint='Please provide SECRET_KEY in your Django `settings.py`. It should be long and random, because cryptographical functions rely on it\'s quality.',
                obj=None,
                id='api.E002',
            )
        )]

    return errors
