import time
import traceback
import sys

import logging
logger = logging.getLogger(__name__)

from django.db import transaction

from django.core.management.base import BaseCommand, CommandError

from api.models import GooglePlayIAPPurchase, PurchaseStatus
from api.billing import create_google_validator

from django.db.models import Q
from django.db.models.expressions import F


from datetime import timedelta
from django.utils import timezone




class Worker:
    queryset = lambda _: GooglePlayIAPPurchase.objects.select_related('product')


    def __init__(self, handle_rewarded_purchases=True,
                       handle_normal_purchases=True,
                       workers_count=1,
                       worker_num=0,
                       time_delay=0.5,
                       refund_period_days=3):
        self.time_delay = time_delay
        self.handle_normal_purchases = handle_normal_purchases
        self.handle_rewarded_purchases = handle_rewarded_purchases
        self.workers_count = workers_count
        self.worker_num = worker_num
        self.refund_period_days = refund_period_days

        self.validator = create_google_validator()

    def parallelized_queryset(self):
        return self.queryset().annotate(my_rows=(F('pk') + self.worker_num) % self.workers_count).filter(my_rows=True)

    def get_rewarded_purchases(self):
        # q = self.parallelized_queryset() if self.workers_count > 1 else self.queryset()
        q = self.queryset()
        return q.filter(Q(product__is_rewarded_product=True), Q(status=PurchaseStatus.UNKNOWN)).select_for_update()

    def get_normal_purchases(self):
        # q = self.parallelized_queryset() if self.workers_count > 1 else self.queryset()
        q = self.queryset()
        return q.filter(Q(product__is_rewarded_product=False),
                        Q(date_created__gt=self.get_refund_period(), status=PurchaseStatus.PURCHASED) | Q(status=PurchaseStatus.UNKNOWN)).select_for_update()

    def process_rewarded_purchases(self):
        with transaction.atomic():
            for purchase in self.get_rewarded_purchases():
                self.process_purchase(purchase)

    def process_normal_purchases(self):
        with transaction.atomic():
            for purchase in self.get_normal_purchases():
                self.process_purchase(purchase)

    def process_purchase(self, purchase):
        logger.debug(f'Processing IAP # {purchase.pk}:{purchase.order_id}...')
        try:
            self.validator.validate(purchase)
        except Exception as e:
            type_, value_, traceback_ = sys.exc_info()
            ex = '\n'.join(traceback.format_exception(type_, value_, traceback_))
            logger.error(f'{ex}')
        else:
            purchase.save()

    def get_refund_period(self):
        now = timezone.now()
        dt = timedelta(days=self.refund_period_days)
        return now - dt

    def do(self):
        self.process_rewarded_purchases()
        self.process_normal_purchases()

        time.sleep(self.time_delay)


class Command(BaseCommand):
    help = 'Service to validate Google IAPs'

    def add_arguments(self, parser):
        parser.add_argument(
            '--no-normal',
            help='Don\'t handle normal IAP purchases'
        )
        parser.add_argument(
            '--no-rewarded',
            help='Don\'t handle rewarded IAP purchases'
        )
        parser.add_argument(
            '--workers',
            type=int,
            help='How many worker process ran in parallel'
        )
        parser.add_argument(
            '--worker-num',
            type=int,
            help='Which one worker is me?'
        )

    def handle(self, *args, **options):
        worker = Worker()

        while True:
            worker.do()
