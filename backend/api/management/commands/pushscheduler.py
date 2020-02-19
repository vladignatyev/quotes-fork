import time
import traceback
import sys

import logging
logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)

from django.core.management.base import BaseCommand, CommandError

from api.models import PushMessage, PushNotificationQueueItem

from datetime import datetime



class Command(BaseCommand):
    help = 'Pushes sending scheduler'


    def handle(self, *args, **options):
        while True:
            self.loop()
            time.sleep(30)

    def loop(self):
        now = datetime.now()
        hour, minute, weekday = now.hour, now.minute, now.isoweekday()

        field_name = (0, 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday')[weekday]

        logger.debug(f'Batch @ {field_name}, {hour}:{minute}')
        filter = {f'{field_name}': f'{hour}:{minute}'}

        messages = list(PushMessage.objects.filter(**filter))
        logger.debug(f'There is {len(messages)} scheduled messages to send at the moment.')

        if len(messages) == 0:
            return

        for message in messages:
            logger.debug(f'Pushing the message to the queue: {message.title} | {message.body}')

            queueitem = message.to_queue_item()
            meta = f'{field_name}, {hour}:{minute}'
            already_enqueued = PushNotificationQueueItem.objects.filter(meta=meta).count()
            if already_enqueued:
                logger.debug(f'Message has been already enqueued, so skipping.')
            else:
                queueitem.meta = meta
                queueitem.save()
                logger.debug(f'Enqueued.')
