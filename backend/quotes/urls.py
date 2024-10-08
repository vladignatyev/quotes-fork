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
from .admin_views import *

urlpatterns = [
    # Authorization backend
    path('auth/', QuotesAuthenticateView.as_view(), name='quote-auth'),

    # Topic's sections and categories list along with user progress and lock status
    path('topic/<int:pk>/', TopicDetailView.as_view(), name='topic-detail'),

    # Topics list
    path('topic/list/', TopicListView.as_view(), name='topic-list'),

    # Levels list in category
    path('levels/category/<int:category_pk>/', LevelsListView.as_view(), name='levels-list'),

    # Level complete postback
    path('level/<int:level_pk>/complete', LevelCompleteView.as_view(), name='level-complete'),

    # Unlock for coins
    path('category/<int:category_pk>/unlock', CategoryUnlockView.as_view(), name='category-unlock'),

    # User profile
    path('profile/', ProfileView.as_view(), name='profile-view'),
    path('profile/update/', ProfileUpdateView.as_view(), name='profile-update-view'),

    # List of user achievements
    path('achievements/', AchievementListView.as_view(), name='achievements-list'),

    # List of all existing achievements
    path('achievements/all/', AllAchievementListView.as_view(), name='achievements-list-all'),

    # Google Play purchases
    path('purchase/play/', GenericPurchaseView.as_view(), name='purchase-play-view'),

    path('admob/ssv/', AdmobPurchaseVerificationView.as_view(), name='purchase-admob-ssv'),

    path('purchase/play/status/<uuid:purchase_id>/', PurchaseStatusView.as_view(), name='purchase-status-view'),
    path('purchase/play/products/', PurchaseableProductsListView.as_view(), name='purchase-products-list'),

    path('coin/products/', CoinProductsList.as_view(), name='coin-products-list'),
    path('coin/consume/', CoinProductConsumeView.as_view(), name='coin-product-consume'),

    path('quote/preview/<int:quote_pk>/', QuotePreview.as_view(), name='quote-preview'),

    path('quoterank/globaltop/preview/', QuoteRankHtmlPreview.as_view(), name='quoterank-globaltop-preview'),

    path('quoterank/', QuoteRankTop.as_view(), name='quoterank-top'),

    # Push notifications
    path('notifications/subscribe/', PushNotificationSubscriptionView.as_view(), name='notifications-subscribe')
]
