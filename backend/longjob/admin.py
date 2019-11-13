from django.contrib import admin

from .models import LongJobLog, JobStatus


def relaunch_job(modeladmin, request, queryset):
    for job in queryset:
        job.status = JobStatus.NEW
        job.ended_time = None
        job.started_time = None
        job.save()
relaunch_job.short_description = 'Relaunch selected jobs'


@admin.register(LongJobLog)
class LongJobLogAdmin(admin.ModelAdmin):
    list_display = ['timestamp',]
    readonly_fields = ['timestamp', 'content']
    date_hierarchy = 'timestamp'


class LongJobAdmin(admin.ModelAdmin):
    list_display = ['status', 'worker_id']
    actions = [relaunch_job]
