from django.utils import timezone
from django.db import models

from django.utils.translation import gettext_lazy as _
from django.utils.translation import gettext as __


class JobStatus:
    DEFAULT = NEW = 'new'
    IN_PROGRESS = 'inprogress'
    DONE = 'done'
    ERROR = 'error'

    choices = (('new', __('Just created')),
               ('inprogress', __('In progress')),
               ('done', __('Done')),
               ('error', __('Error')))


class LongJobLog(models.Model):
    content = models.TextField(blank=True)
    timestamp = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f'[{self.timestamp}]'


class LongJobQueueItem(models.Model):
    status = models.CharField(max_length=16, choices=JobStatus.choices, default=JobStatus.DEFAULT)

    log = models.ForeignKey('longjob.LongJobLog', on_delete=models.CASCADE, related_name='log_item', null=True, blank=True)

    result = models.TextField(blank=True)  # result explanation

    started_time = models.DateTimeField(blank=True, null=True)
    ended_time = models.DateTimeField(blank=True, null=True)

    worker_id = models.CharField(max_length=256, blank=True)

    class Meta:
        abstract = True

    def done_with_result(self, result_text):
        self.is_hanged = False
        self.result = result_text
        self.status = JobStatus.DONE
        self.ended_time = timezone.now()
        self.save()
        return self

    def start(self, *args, **kwargs):
        self.started_time = timezone.now()
        self.save()
        return self

    def end_with_error(self, error_text):
        self.is_hanged = False
        self.result = error_text
        self.status = JobStatus.ERROR
        self.ended_time = timezone.now()
        self.save()
        return self

    def is_complete(self):
        print(self.ended_time)
        return self.ended_time is not None

    def __str__(self):
        return f'[{self.status}] {self.pk}'
