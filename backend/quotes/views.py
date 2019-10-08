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

from api.views import AuthenticationForm, AuthenticateView, SafeView


def json_response(res_dict):
    return JsonResponse(res_dict, json_dumps_params={'indent': 4, 'ensure_ascii': False})


class BaseView(SafeView):
    def mixin_authorized(self, request):
        request.user_profile = Profile.objects.get_by_session(request.device_session)


class RestDetailView(View):
    pass # todo: extract base class for all serializable detail views

class RestListView(View):
    pass # todo: extract base class for all serializable list views



class SectionDetail(View):
    def get(self, request, *args, **kwargs):
        section = Section.objects.get(pk=kwargs['pk'])
        return json_response(topic)


class TopicDetail(BaseView):
    def get(self, request, *args, **kwargs):
        topic = Topic.objects.get_flattened(pk=kwargs['pk'], current_user=self.request.user_profile)
        return json_response(topic)


class TopicList(BaseView):
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


class LevelsList(BaseView):
    def get(self, request, *args, **kwargs):
        category_pk = kwargs.get('category_pk', None)

        levels = list(get_levels(category_pk=category_pk,
                                 profile=self.request.user_profile))

        if levels is None:
            return HttpResponse(status=404)

        res_dict = {
            "objects": levels,
            "meta": {}
        }
        return json_response(res_dict)


class AchievementList(BaseView):
    fields = ('icon', 'title', 'received_text', 'description_text')

    def flattened(self, iterable):
        result = [ None ] * len(iterable)

        for i, o in enumerate(iterable):
            result[i] = model_to_dict(o.achievement, fields=self.fields)
            result[i]['received_at'] = o.received_at.strftime('%Y-%m-%dT%H:%M:%S%z')
        return result

    def get(self, request, *args, **kwargs):
        achievements = self.flattened(AchievementReceiving.objects.filter(profile=self.request.user_profile))

        res_dict = {
            "objects": achievements,
            "meta": {}
        }

        return json_response(res_dict)


class QuotesAuthenticationForm(AuthenticationForm):
    nickname = forms.CharField(label='user\'s nickname', max_length=256, required=False)


class QuotesAuthenticateView(AuthenticateView):
    def __init__(self, *args, **kwargs):
        super(QuotesAuthenticateView, self).__init__(form_cls=QuotesAuthenticationForm, *args, **kwargs)

    def respond_authenticated(self):
        profile = Profile.objects.get_by_session(self.device_session)
        profile.nickname = self.cleaned_data['nickname']
        profile.settings = GameBalance.objects.get_actual_game_settings()
        profile.save()

        return JsonResponse({
            'auth_token': self.device_session.auth_token
        })
