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

from api.views import AuthenticationForm, AuthenticateView


def json_response(res_dict):
    return JsonResponse(res_dict, json_dumps_params={'indent': 4, 'ensure_ascii': False})



class RestDetailView(View):
    pass # todo: extract base class for all serializable detail views

class RestListView(View):
    pass # todo: extract base class for all serializable list views



class SectionDetail(View):
    def get(self, request, *args, **kwargs):
        section = Section.objects.get(pk=kwargs['pk'])
        return json_response(topic)


class TopicDetail(View):
    def get(self, request, *args, **kwargs):
        topic = Topic.objects.get_flattened(pk=kwargs['pk'])
        return json_response(topic)


class TopicList(View):
    queryset = Topic.objects.filter(hidden=False)
    ordering = '-pk'

    def get(self, request, *args, **kwargs):
        objects = [ model_to_dict(o) for o in self.queryset.order_by(self.ordering) ]
        for o in objects:
            o['uri'] = reverse('topic-detail', kwargs={'pk': o['id']})


        res_dict = {
            "objects": objects,
            "meta": {
                # "limit": self.paginate_by,
                # # "next": null,
                # "offset": 0,
                # # "previous": null,
                # "total_count": p.count,
                # "num_pages": p.num_pages
            }
        }

        return json_response(res_dict)


class AchievementList(View):
    def get(self, request, *args, **kwargs):

        res_dict = {
            "objects": achievements,
            "meta": {}
        }

        return json_response(res_dict)


class QuotesAuthenticationForm(AuthenticationForm):
    nickname = forms.CharField(label='user\'s nickname', max_length=256)


class QuotesAuthenticateView(AuthenticateView):
    def __init__(self, *args, **kwargs):
        super(QuotesAuthenticateView, self).__init__(form_cls=QuotesAuthenticationForm, *args, **kwargs)
