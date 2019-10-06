import json

import logging
logger = logging.getLogger(__name__)


from django.http import Http404
from django.db import models


from .models import *

from django.http import HttpResponse, JsonResponse
from django.views.generic import ListView, View, DetailView
from django.forms.models import model_to_dict
from django.views.decorators.http import require_http_methods

# from django.core.paginator import Paginator

from django.urls import reverse
from django import forms


from django.core.validators import RegexValidator

class TokenValidator(RegexValidator):
    def __init__(self):
        super(TokenValidator, self).__init__(regex=r'^[a-zA-Z0-9]+$',
              message='Invalid token format')

# todo: generalize and extract partially to the app module (nickname field!)

class AuthenticationForm(forms.Form):
    device_token = forms.CharField(label='device token', max_length=256, validators=[TokenValidator()])
    timestamp = forms.DateTimeField(label='timestamp', input_formats=['%Y-%m-%dT%H:%M:%S%z']) # 2019-09-04T08:30:00+0300
    # nickname = forms.CharField(label='user\'s nickname', max_length=256)
    signature = forms.CharField(label='signature', max_length=256, validators=[TokenValidator()])


class AuthenticateView(View):
    PAYLOAD_MAX_LENGTH = 512

    def __init__(self, form_cls=AuthenticationForm, *args, **kwargs):
        super(AuthenticateView, self).__init__(*args, **kwargs)
        self.form_cls = form_cls

    def make_form_from_request(self, request):
        data = request.body[:self.PAYLOAD_MAX_LENGTH]
        try:
            deserialized = json.loads(data)
        except json.decoder.JSONDecodeError:
            deserialized = None
        return self.form_cls(deserialized)

    def post(self, request, *args, **kwargs):
        self.form = self.make_form_from_request(request)

        if not self.form.is_valid():
            return self.invalid_response()

        self.cleaned_data = self.form.clean()
        self.device_session = DeviceSession.objects.create_from_token(self.cleaned_data['device_token'])
        self.device_session.save()

        return self.respond_authenticated()

    def respond_authenticated(self):
        return JsonResponse({})

    def invalid_response(self):
        return HttpResponse(status=401)


class SafeView(View):
    def dispatch(self, request, *args, **kwargs):
        auth_token = request.headers.get('X-Client-Auth', None)

        if not check_auth_token(auth_token):
            return HttpResponse(status=401)

        try:
            request.device_session = DeviceSession.objects.get(auth_token=auth_token)
        except DeviceSession.DoesNotExist:
            return HttpResponse(status=401)

        self.mixin_authorized(request)

        return super(SafeView, self).dispatch(request, *args, **kwargs)

    def mixin_authorized(self, request):
        pass
