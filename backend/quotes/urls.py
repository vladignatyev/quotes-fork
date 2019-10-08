"""backend URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/2.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""

from django.urls import include, path
from django.conf import settings

from .views import *

urlpatterns = [
    path('auth/', QuotesAuthenticateView.as_view(), name='quote-auth'),

    path('topic/<int:pk>/', TopicDetail.as_view(), name='topic-detail'),
    path('topic/list/', TopicList.as_view(), name='topic-list'),

    path('levels/category/<int:category_pk>/', LevelsList.as_view(), name='levels-list'),

    path('section/<int:pk>/', SectionDetail.as_view(), name='section-detail'),

    path('achievements/', AchievementList.as_view(), name='achievements-list'),
]
