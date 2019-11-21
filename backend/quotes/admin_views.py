import uuid
import json

from django.db import models
from django.apps import apps
from django.http import Http404, HttpResponse, JsonResponse
from django.views.generic import ListView, View, DetailView
from django.forms.models import model_to_dict
from django.views.decorators.http import require_http_methods
from django.urls import reverse
from django import forms
from django.template.response import TemplateResponse

from .models import *


class QuotePreview(View):
    template = 'admin/quote-preview.html'

    def get(self, request, *args, **kwargs):
        if not request.user.is_authenticated:
            raise Http404()

        quote_pk = kwargs.get('quote_pk', None)
        if quote_pk is None:
            raise Http404()
        try:
            quote = Quote.objects.get(pk=quote_pk)
            return TemplateResponse(request, self.template, {'quote': quote})
        except Quote.DoesNotExist:
            raise Http404()


class QuotesBulkPreview(View):
    template = 'admin/bulk-levels-preview.html'

    def get(self, request, *args, **kwargs):
        # if not request.user.is_authenticated:
        #     raise Http404()

        # try:
        quotes = Quote.objects.all()
        return TemplateResponse(request, self.template, {'levels': quotes})
        # except Quote.DoesNotExist:
        #     raise Http404()
