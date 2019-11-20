import time
import traceback
import sys

import logging
logger = logging.getLogger(__name__)

from django.db import transaction

from django.core.management.base import BaseCommand, CommandError

from api.models import GooglePlayIAPPurchase, PurchaseStatus
from api.billing import create_google_validator


class Worker:
    time_delay = 3

    def __init__(self):
        self.validator = create_google_validator()

    def do(self):
        with transaction.atomic():
            purchases = GooglePlayIAPPurchase.objects.select_for_update().filter(status=PurchaseStatus.DEFAULT)

            for purchase in purchases:
                logger.debug(f'Processing IAP # {purchase.pk}:{purchase.order_id}...')
                try:
                    self.validator.validate(purchase)
                except Exception as e:
                    type_, value_, traceback_ = sys.exc_info()
                    ex = '\n'.join(traceback.format_exception(type_, value_, traceback_))
                    logger.error(f'{ex}')
                else:
                    purchase.save()

        time.sleep(self.time_delay)


class Command(BaseCommand):
    help = 'Service to validate Google IAPs'

    def add_arguments(self, parser):
        pass

    def handle(self, *args, **options):
        worker = Worker()

        while True:
            worker.do()
