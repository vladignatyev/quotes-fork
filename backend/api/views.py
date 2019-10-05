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

import json


from django.core.validators import RegexValidator

class TokenValidator(RegexValidator):
    def __init__(self):
        super(TokenValidator, self).__init__(regex=r'^[a-zA-Z0-9]+$',
              message='Invalid token format')


class AuthenticationForm(forms.Form):
    device_token = forms.CharField(label='device token', max_length=256, validators=[TokenValidator()])
    timestamp = forms.DateTimeField(label='timestamp', input_formats=['%Y-%m-%dT%H:%M:%S%z']) # 2019-09-04T08:30:00+0300
    nickname = forms.CharField(label='user\'s nickname', max_length=256)
    signature = forms.CharField(label='signature', max_length=256, validators=[TokenValidator()])

    @classmethod
    def from_request(cls, request):
        data = request.POST.get('data', '{}')
        deserialized = json.loads(data)
        return cls(deserialized)

    def clean(self):
        cleaned_data = super().clean()
        # cleaned_data['']

class AuthenticateView(View):
    def post(self, request, *args, **kwargs):
        # print(request.POST['data'])
        # print(**kwargs)
        # print(request.headers)

        form = AuthenticationForm.from_request(request)# ()
        if form.is_valid():
            Credentials.objects.get()
            # todo:
            # authenticate
            return HttpResponse(status=400)
        else:
            return HttpResponse(status=401)
