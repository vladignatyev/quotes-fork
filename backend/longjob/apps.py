import os

from django.apps import AppConfig
from django.core.checks import Error, register
from django.apps import apps

from django.core.management import find_commands, load_command_class


class LongjobConfig(AppConfig):
    name = 'longjob'


@register()
def check_workers_config(app_configs, **kwargs):
    from .models import LongJobQueueItem

    errors = []

    app_configs = apps.get_app_configs()
    longjob_apps = []

    for app_config in app_configs:
        for model in app_config.get_models():
            if LongJobQueueItem in model.__bases__ and app_config not in longjob_apps:
                longjob_apps += [app_config]

    for app_config in longjob_apps:
        commands_names = find_commands(os.path.join(app_config.path, 'management'))
        for c in commands_names:
            cmd_obj = load_command_class(app_config.name, c)
            if type(super(type(cmd_obj))) == 'LongJobBaseCommand':
                if cmd_obj.job_model_cls is None:
                    errors += [(
                        Error(
                            f'`job_model_cls` is not set for management command `{c}`',
                            hint=f'You should provide proper `job_model_cls` for command `{c}`.',
                            obj=cmd_obj,
                            id='longjob.E001',
                        )
                    )]
                if cmd_obj.log_model_cls is None:
                    errors += [(
                        Error(
                            f'`log_model_cls` is not set for management command `{c}`',
                            hint=f'You should provide proper `log_model_cls` for command `{c}`.',
                            obj=cmd_obj,
                            id='longjob.E002',
                        )
                    )]
                if cmd_obj.worker_cls is None:
                    errors += [(
                        Error(
                            f'`worker_cls` is not set for management command `{c}`',
                            hint=f'You should provide proper `worker_cls` for command `{c}`.',
                            obj=cmd_obj,
                            id='longjob.E003',
                        )
                    )]

    return errors
