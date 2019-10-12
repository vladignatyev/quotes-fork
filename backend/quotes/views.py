from django.db import models
from django.apps import apps
from django.http import Http404, HttpResponse, JsonResponse
from django.views.generic import ListView, View, DetailView
from django.forms.models import model_to_dict
from django.views.decorators.http import require_http_methods
from django.urls import reverse
from django import forms

from .models import *


from api.views import AuthenticationForm, AuthenticateView, SafeView


from .utils import json_response

class BaseView(SafeView):
    def mixin_authorized(self, request):
        request.user_profile = Profile.objects.get_by_session(request.device_session)


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

        try:
            query_result = get_levels(category_pk=category_pk,
                                      profile=self.request.user_profile)
        except QuoteCategory.DoesNotExist:
            return HttpResponse(status=404)

        if query_result is None:
            return HttpResponse(status=402)

        levels = list(query_result)

        if levels is None:
            return HttpResponse(status=404)

        res_dict = {
            "objects": levels,
            "meta": {}
        }
        return json_response(res_dict)


class LevelCompleteView(BaseView):
    def post(self, request, *args, **kwargs):
        level_pk = kwargs.get('level_pk', None)

        try:
            quote = Quote.objects.get(pk=level_pk)
            events = quote.mark_complete(profile=self.request.user_profile)

            res_dict = {
                "objects": events,
                "meta": {}
            }

            return json_response(res_dict)
        except Quote.NoAccess:
            return HttpResponse(status=402)
        except Quote.DoesNotExist:
            return HttpResponse(status=404)


class ProfileView(BaseView):
    fields = ('id', 'balance', 'nickname', 'settings__initial_profile_balance',
              'settings__reward_per_level_completion', 'settings__reward_per_doubleup')

    def get(self, request, *args, **kwargs):
        flat_profile = model_to_dict(self.request.user_profile, fields=self.fields)
        res_dict = {
            "objects": [flat_profile],
            "meta": {}
        }
        return json_response(res_dict)


class PurchaseCoinsView(BaseView):
    def post(self, request, *args, **kwargs):
        pass


class PurchaseUnlockView(BaseView):
    def post(self, request, *args, **kwargs):
        pass

class PurchaseStatusView(BaseView):
    # todo: move to API
    def get(self, request, *args, **kwargs):
        purchase_id = kwargs['purchase_id']
        Purchase = apps.get_model('api.GooglePlayIAPPurchase')

        try:
            purchase = Purchase.objects.get(id=purchase_id)
            res_dict = {
                "status": purchase.status
            }
            return json_response(res_dict)
        except Purchase.DoesNotExist:
            pass

        raise Http404()

class CategoryUnlockView(BaseView):
    def post(self, request, *args, **kwargs):
        category = QuoteCategory.objects.get(pk=kwargs['category_pk'])

        try:
            unlock = CategoryUnlockPurchase.objects.create(type=CategoryUnlockTypes.UNLOCK_FOR_COINS,
                                                  profile=self.request.user_profile,
                                                  category_to_unlock=category)
            unlock.do_unlock()
        except CategoryUnlockPurchase.InsufficientFunds:
            return HttpResponse(status=402)
        except CategoryUnlockPurchase.AlreadyAvailable:
            pass

        return HttpResponse(status=200)

class AchievementList(BaseView):
    fields = ('icon', 'title', 'received_text', 'description_text')

    def flattened(self, iterable):
        result = [ None ] * len(iterable)

        for i, o in enumerate(iterable):
            result[i] = model_to_dict(o.achievement, fields=self.fields)
            result[i]['received_at'] = o.received_at.strftime('%Y-%m-%dT%H:%M:%S%z')
        return result

    def filter(self):
        return AchievementReceiving.objects.filter(profile=self.request.user_profile)

    def get(self, request, *args, **kwargs):
        achievements = self.flattened(self.filter())

        res_dict = {
            "objects": achievements,
            "meta": {}
        }

        return json_response(res_dict)


class AllAchievementList(BaseView):
    fields = ('icon', 'title', 'received_text', 'description_text')

    def flattened(self, iterable):
        result = [ None ] * len(iterable)

        for i, o in enumerate(iterable):
            result[i] = model_to_dict(o, fields=self.fields)
        return result

    def get(self, request, *args, **kwargs):
        achievements = self.flattened(Achievement.objects.all())

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

    def add_nickname_to_profile(self):
        profile = Profile.objects.get_by_session(self.device_session)
        profile.nickname = self.cleaned_data['nickname']
        profile.save()

    def respond_authenticated(self):
        self.add_nickname_to_profile()

        return JsonResponse({
            'auth_token': self.device_session.auth_token
        })
