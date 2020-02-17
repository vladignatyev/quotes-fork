# import re
import json

from django.conf import settings

import firebase_admin
from firebase_admin import messaging, credentials



class FirebaseApp:
    def __init__(self, *args, **kwargs):
        """

        https://console.firebase.google.com/u/0/project/quotepuzzle-904ce/settings/serviceaccounts/adminsdk

        """
        cred = credentials.Certificate(settings.GOOGLE_APPLICATION_CREDENTIALS)
        self.app = firebase_admin.initialize_app(cred)


class FirebaseMessagingApp(FirebaseApp):
    def __init__(self, *args, **kwargs):
        super(FirebaseMessagingApp, self).__init__(*args, **kwargs)

    def _build_notification_from_model(self, push_notification):
        if not push_notification.title and not push_notification.body:
            return None

        notification = messaging.Notification(
            title=push_notification.title,
            body=push_notification.body,
            image=push_notification.image_url or None
        )

        return notification

    def build_message_from_model(self, push_notification_model, push_subscription_model):
        if push_notification_model.data is not None:
            # todo: check that all values are strings
            data_payload = json.loads(push_notification_model.data)
        else:
            data_payload = None

        notification = self._build_notification_from_model(push_notification_model)

        message_params = {}
        message_params['data'] = data_payload
        message_params['notification'] = notification

        if push_subscription_model:
            if push_subscription_model.token:
                message_params['token'] = push_subscription_model.token
            elif push_subscription_model.condition:
                message_params['condition'] = push_notification_model.condition
        else:
            message_params['topic'] = push_notification_model.topic

        message = messaging.Message(**message_params)
        #     topic=push_notification_model.topic or None,
        #     condition=push_notification_model.condition or None,
        #     data=data_payload,
        #     token=push_subscription_model.token,
        #     notification=notification
        # )
        return message

    # def build_multicast_message_from_subscriptions(self, push_notification_model, push_subscription_models):
    #     tokens = [o.token for o in push_subscription_models]
    #
    #     if str(push_notification_model.data) != '':
    #         # todo: check that all values are strings
    #         data_payload = json.loads(push_notification_model.data)
    #     else:
    #         data_payload = None
    #
    #     notification = self._build_notification_from_model(push_notification_model)
    #
    #     message = messaging.MulticastMessage(tokens,
    #                                          data=data_payload,
    #                                          notification=notification)
    #     return message

    # todo: wrap required error handlers
    def send_multicast_message(self, message, dry_run=False):
        return messaging.send_multicast(message, dry_run, app=self.app)

    # todo: wrap required error handlers
    def send_messages(self, messages, dry_run=False):
        return messaging.send_all(messages, dry_run, app=self.app)

    # todo: wrap required error handlers
    def send_message(self, message, dry_run=False):
        return messaging.send(message, dry_run, app=self.app)






#
# class FCMError:
#     class InvalidResponseStatus(Exception): pass
#     class ConnectionTimeout(Exception): pass
#     class OtherError(Exception): pass
#
#
# class FCMService:
#     SCOPES = ['https://www.googleapis.com/auth/firebase.messaging']
#
#     def __init__(self, project_id, service_account_file):
#         assert re.match(r'^[a-zA-Z0-9]+-[a-zA-Z0-9]+$', project_id) is not None
#
#         self.project_id = project_id
#         # default_app = firebase_admin.initialize_app()
#
#         credentials = ServiceAccountCredentials.from_json_keyfile_name(service_account_file, self.SCOPES)
#         access_token_info = credentials.get_access_token()
#
#         self.access_token = access_token_info.access_token
#
#     def get_url(self):
#         return f'https://fcm.googleapis.com/v1/projects/{self.project_id}/messages:send'
#
#     def build_request(self, push_subscription, push_notification):
#         url = self.get_url()
#
#         headers = {
#             'Authorization': f'Bearer {self.access_token}',
#             'Content-Type': 'application/json; UTF-8'
#         }
#
#         body = {
#             'message': {}
#         }
#
#         if push_notification.topic:
#             body['message']['topic'] = str(push_notification.topic)
#
#         pass

#
# def build_fcm_request(push_subscription, push_notification, firebase_server_key,
#                       fcm_url='https://fcm.googleapis.com/fcm/send'):
#     url = fcm_url
#
#     headers = {
#         'Content-Type': 'application/json',
#         'Authorization': f'key={firebase_server_key}'
#     }
#
#     body = {
#         'to': push_subscription.token,
#         'notification': push_notification.as_dict()
#     }
#
#     return url, headers, body  # fcm_request_tuple
#
#
# def send_with_requests_session(session,
#                        url, headers, body,
#                        verify_ssl_certs=False,
#                        proxies=None,
#                        timeout=5):
#     from requests import Request
#     from requests.exceptions import ConnectTimeout
#
#     request = Request('POST', url, data=json.dumps(body), headers=headers)
#     prepared_request = request.prepare()
#
#     try:
#         response = session.send(prepared_request,
#             stream=False,
#             verify=verify_ssl_certs,
#             proxies=proxies,
#             timeout=timeout
#         )
#     except ConnectTimeout as e:
#         raise FCMError.ConnectionTimeout(e)
#     else:
#         status = response.status_code
#         content = response.content
#
#         if response.status_code >= 400:
#             raise FCMError.InvalidResponseStatus(url, headers, body, status, content)
#
#         # {"multicast_id":6461723160575566048,"success":0,"failure":1,"canonical_ids":0,"results":[{"error":"InvalidRegistration"}]}
#
#         response_obj = json.loads(response.content)
#         if response_obj['success'] == 1:
#             return  # well done.
#         elif response_obj['success'] == 1:
#
#         print(response.status_code)
#         print(response.content)
