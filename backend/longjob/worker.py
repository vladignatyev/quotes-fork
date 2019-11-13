import os
import uuid
import time
import traceback
import sys


from django.utils import timezone
from django.db import transaction

from django.core.management.base import BaseCommand, CommandError

from .models import LongJobLog, JobStatus



class LogWriter:
    def __init__(self, log_model_cls=LongJobLog):
        self.entries = []
        self.log_model_cls = log_model_cls

    def log(self, message, format='[%(time)s] %(message)s', *args, **kwargs):
        ts = timezone.now()
        entry = format % {'message': message, 'time': ts}
        self.entries += [entry]

    def save(self):
        return self.log_model_cls.objects.create(content='\r\n'.join(self.entries))


class Worker:
    class ProgrammingError(Exception): pass
    class NotImplementedError(Exception): pass


    def __init__(self, *args, **kwargs):
        self.job_model_cls = kwargs.get('job_model_cls', None)
        self.log_model_cls = kwargs.get('log_model_cls', LongJobLog)
        self.stdout = kwargs['stdout']
        self.time_delay = kwargs.get('time_delay', 3)

    def query_jobs(self):
        '''
        Return QuerySet containing all jobs those have to be done by this worker.
        By default we query all items of `job_model_cls`.
        '''
        return self.job_model_cls.objects.filter(status=JobStatus.NEW)

    def get_worker_id(self):
        '''
        Worker ID is an unique ID used as
        '''
        return uuid.uuid4()

    def do(self):
        timenow = timezone.now()
        self.worker_id = worker_id = self.get_worker_id()

        # here we acquire lock for jobs to be done
        with transaction.atomic(): # not sure if it's required, cause "UPDATE .. WHERE" SQL query is atomic itself
            count = self.query_jobs().update(started_time=timenow,
                                             status=JobStatus.IN_PROGRESS,
                                             worker_id=worker_id)
        if count == 0:
            self.stdout.write(f'Nothing to do. Worker id: {worker_id}')
            if self.time_delay > 0:
                time.sleep(self.time_delay)
            return

        self.stdout.write(f'New jobs found: {count}. Processing with worker id: {worker_id}')

        my_jobs = self.job_model_cls.objects.filter(worker_id=self.worker_id)

        self.iter_jobs(my_jobs.all())

    def iter_jobs(self, my_jobs):
        for job in my_jobs.all():
            self.iter_job_loop(job)

    def iter_job_loop(self, job):
        l = LogWriter()
        try:
            job.start()
            self.do_job_with_logger(job, l)
        except Exception as e:
            type_, value_, traceback_ = sys.exc_info()
            ex = '\n'.join(traceback.format_exception(type_, value_, traceback_))
            job.end_with_error(f'Exception raised:\n\n{ex}')
            l.log(f'{ex}')
        finally:
            job.log = l.save()
            job.save()

        if not job.is_complete():
            job_name = type(job).__name__
            raise self.ProgrammingError(f'Neither `done_with_result` nor `end_with_error` has been called while executing the job `{job_name}` pk={job.pk}')

    def do_job_with_logger(self, job, log):
        '''
        Worker method to be implemented by user.
        '''
        worker_name = type(self).__name__
        raise self.NotImplementedError(f'You should implement your own implementation of `do_job_with_logger` for `{worker_name}`')



class LongJobBaseCommand(BaseCommand):
    help = 'Get queued jobs and process them all at once'

    job_model_cls = None
    worker_cls = None
    log_model_cls = LongJobLog

    time_delay = 3

    def add_arguments(self, parser):
        pass

    def handle(self, *args, **options):
        # command_name = type(self).__name__

        # if self.job_model_cls is None:
        #     raise Worker.NotImplementedError(f'You should provide proper `job_model_cls` for command `{command_name}`')
        # if self.log_model_cls is None:
        #     raise Worker.NotImplementedError(f'You should provide proper `log_model_cls` for command `{command_name}`')
        # if self.worker_cls is None:
        #     raise Worker.NotImplementedError(f'You should provide proper `worker_cls` for command `{command_name}`')

        worker = self.worker_cls(stdout=self.stdout,
                                 job_model_cls=self.job_model_cls,
                                 log_model_cls=self.log_model_cls,
                                 time_delay=self.time_delay)

        while True:
            worker.do()
