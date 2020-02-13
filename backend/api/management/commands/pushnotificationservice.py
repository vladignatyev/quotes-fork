from django.conf import settings

from longjob.worker import LongJobBaseCommand, Worker

from api.models import PushNotificationQueueItem, PushSubscription
from api.notifications import FirebaseMessagingApp


class PushSendingWorker(Worker):
    def __init__(self, *args, **kwargs):
        super(PushSendingWorker, self).__init__(self, *args, **kwargs)

        self.fcmapp = FirebaseMessagingApp()

    def do_job_with_logger(self, job, logger):
        push_notification_model = job

        if push_notification_model.is_broadcast:
            subscriptions = PushSubscription.objects.all()
            log_items = []
            for subscription in subscriptions:
                message = self.fcmapp.build_message_from_model(push_notification_model, subscription)
                result = self.fcmapp.send_message(message, dry_run=(settings.DEBUG or settings.TEST))
                log_items.append(f'Sent to token {subscription.token} with result {result}')

            job.done_with_result(result_text=f'Message ID: {result}')

            # message = self.fcmapp.build_multicast_message_from_subscriptions(push_notification_model, subscriptions)
            # result = self.fcmapp.send_multicast_message(message, dry_run=(settings.DEBUG or settings.TEST))
            # job.done_with_result(result_text=f'Total: {len(result.responses)} Success: {result.success_count} Failures: {result.failure_count}')

        else:
            push_subscription = push_notification_model.push_subscription
            message = self.fcmapp.build_message_from_model(push_notification_model, push_subscription)
            result = self.fcmapp.send_message(message, dry_run=(settings.DEBUG or settings.TEST))
            job.done_with_result(result_text=f'Message ID: {result}')




class Command(LongJobBaseCommand):
    help = 'Service that sends enqueued Push Notifications using Google FCM API'

    worker_cls = PushSendingWorker
    job_model_cls = PushNotificationQueueItem
