from django.http import Http404
from django.views.generic import View
from django.template.response import TemplateResponse

from .models import *


class QuoteRankPreview(View):
    template = 'quoterank/top-preview.html'

    def authenticate(self, request):
        if not request.user.is_authenticated:
            raise Http404()

    def get(self, request, *args, **kwargs):
        # self.authenticate(request)

        if getattr(request, 'user_profile', None):
            profile = request.user_profile
            user_top = self.get_user_top(profile)
        else:
            user_top = []

        global_top_10 = self.get_global_top()

        return self.render_response(request, global_top_10, user_top)

    def get_user_top(self, profile):
        return ProfileRank.objects.top_by_profile(profile)

    def get_global_top(self):
        return ProfileRank.objects.global_top(10)

    def render_response(self, request, global_top, user_top):
        return TemplateResponse(request, self.template, {
            'quoterank_global': global_top,
            'user_top': user_top
        })
