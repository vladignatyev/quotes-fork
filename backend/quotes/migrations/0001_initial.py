# Generated by Django 2.2.5 on 2019-10-12 16:48

from django.db import migrations, models
import django.db.models.deletion
import django.utils.timezone
import uuid


class Migration(migrations.Migration):

    initial = True

    dependencies = [
        ('api', '0001_initial'),
    ]

    operations = [
        migrations.CreateModel(
            name='Achievement',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('icon', models.CharField(max_length=256, verbose_name='Имя иконки в приложении')),
                ('title', models.CharField(max_length=256)),
                ('received_text', models.TextField(default='')),
                ('description_text', models.TextField(default='')),
            ],
            options={
                'verbose_name': 'достижение',
                'verbose_name_plural': 'достижения',
            },
        ),
        migrations.CreateModel(
            name='CategoryUnlockPurchase',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('type', models.CharField(choices=[('null_unlock', '!! не использовать !!'), ('unlock_for_coins', 'Анлок за игровые монеты'), ('unlock_by_purchase', 'Анлок за покупку')], default='null_unlock', max_length=32)),
                ('date_created', models.DateTimeField(auto_now_add=True)),
            ],
            options={
                'verbose_name': 'Покупка доступа к категории',
                'verbose_name_plural': 'Покупки доступов к категориям',
            },
        ),
        migrations.CreateModel(
            name='GameBalance',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('initial_profile_balance', models.BigIntegerField(default=0)),
                ('reward_per_level_completion', models.BigIntegerField(default=5)),
                ('reward_per_doubleup', models.BigIntegerField(default=5)),
            ],
            options={
                'verbose_name': 'игровой баланс',
                'verbose_name_plural': 'игровой баланс',
            },
        ),
        migrations.CreateModel(
            name='Profile',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('balance', models.PositiveIntegerField(default=0)),
                ('nickname', models.CharField(default='Пан Инкогнито', max_length=256)),
                ('device_sessions', models.ManyToManyField(to='api.DeviceSession')),
                ('settings', models.ForeignKey(default=None, null=True, on_delete=django.db.models.deletion.CASCADE, to='quotes.GameBalance')),
            ],
            options={
                'verbose_name': 'профиль пользователя',
                'verbose_name_plural': 'профили пользователей',
            },
        ),
        migrations.CreateModel(
            name='QuoteAuthor',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=256, verbose_name='Автор цитаты')),
            ],
            options={
                'verbose_name': 'автор цитат',
                'verbose_name_plural': 'авторы цитат',
            },
        ),
        migrations.CreateModel(
            name='Topic',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('bonus_reward', models.BigIntegerField(default=0, verbose_name='Бонус монет за прохождение')),
                ('title', models.CharField(max_length=256, verbose_name='Название Темы')),
                ('hidden', models.BooleanField(default=False)),
                ('complete_by_users', models.ManyToManyField(blank=True, related_name='quotes_topic_complete_by_users', related_query_name='quotes_topic_complete_by_users_objs', to='quotes.Profile', verbose_name='Юзеры которые прошли и должны получить вознаграждение')),
                ('on_complete_achievement', models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='quotes.Achievement')),
            ],
            options={
                'verbose_name': 'тема',
                'verbose_name_plural': 'темы',
            },
        ),
        migrations.CreateModel(
            name='Section',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('bonus_reward', models.BigIntegerField(default=0, verbose_name='Бонус монет за прохождение')),
                ('title', models.CharField(max_length=256, verbose_name='Название Раздела')),
                ('complete_by_users', models.ManyToManyField(blank=True, related_name='quotes_section_complete_by_users', related_query_name='quotes_section_complete_by_users_objs', to='quotes.Profile', verbose_name='Юзеры которые прошли и должны получить вознаграждение')),
                ('on_complete_achievement', models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='quotes.Achievement')),
                ('topic', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='quotes.Topic')),
            ],
            options={
                'verbose_name': 'раздел',
                'verbose_name_plural': 'разделы',
            },
        ),
        migrations.CreateModel(
            name='QuoteCategory',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('bonus_reward', models.BigIntegerField(default=0, verbose_name='Бонус монет за прохождение')),
                ('title', models.CharField(max_length=256, verbose_name='Название Категории')),
                ('icon', models.CharField(blank=True, default='', max_length=256)),
                ('is_event', models.BooleanField(default=False)),
                ('event_due_date', models.DateTimeField(blank=True, default=django.utils.timezone.now)),
                ('event_title', models.CharField(blank=True, default='', max_length=256)),
                ('event_icon', models.CharField(blank=True, default='', max_length=256)),
                ('event_description', models.TextField(blank=True, default='')),
                ('is_payable', models.BooleanField(default=False, verbose_name='Категория платная?')),
                ('price_to_unlock', models.BigIntegerField(blank=True, default=0, verbose_name='Стоимость открытия категории если она платная')),
                ('available_to_users', models.ManyToManyField(blank=True, related_name='availability_to_profile', through='quotes.CategoryUnlockPurchase', to='quotes.Profile', verbose_name='Профили пользователей которым доступна категория')),
                ('complete_by_users', models.ManyToManyField(blank=True, related_name='quotes_quotecategory_complete_by_users', related_query_name='quotes_quotecategory_complete_by_users_objs', to='quotes.Profile', verbose_name='Юзеры которые прошли и должны получить вознаграждение')),
                ('event_win_achievement', models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, related_name='on_event_complete_achievement', to='quotes.Achievement')),
                ('on_complete_achievement', models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='quotes.Achievement')),
                ('section', models.ForeignKey(default=None, null=True, on_delete=django.db.models.deletion.CASCADE, to='quotes.Section')),
            ],
            options={
                'verbose_name': 'категория',
                'verbose_name_plural': 'категории',
            },
        ),
        migrations.CreateModel(
            name='Quote',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('bonus_reward', models.BigIntegerField(default=0, verbose_name='Бонус монет за прохождение')),
                ('text', models.CharField(max_length=256, verbose_name='Текст цитаты')),
                ('order_in_category', models.BigIntegerField(blank=True, default=0, verbose_name='Порядковый номер уровня в категории')),
                ('author', models.ForeignKey(null=True, on_delete=django.db.models.deletion.SET_NULL, to='quotes.QuoteAuthor')),
                ('category', models.ForeignKey(null=True, on_delete=django.db.models.deletion.SET_NULL, to='quotes.QuoteCategory')),
                ('complete_by_users', models.ManyToManyField(blank=True, related_name='quotes_quote_complete_by_users', related_query_name='quotes_quote_complete_by_users_objs', to='quotes.Profile', verbose_name='Юзеры которые прошли и должны получить вознаграждение')),
                ('on_complete_achievement', models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='quotes.Achievement')),
            ],
            options={
                'verbose_name': 'цитата',
                'verbose_name_plural': 'цитаты',
            },
        ),
        migrations.AddField(
            model_name='categoryunlockpurchase',
            name='category_to_unlock',
            field=models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='quotes.QuoteCategory'),
        ),
        migrations.AddField(
            model_name='categoryunlockpurchase',
            name='google_play_purchase',
            field=models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='api.GooglePlayIAPPurchase'),
        ),
        migrations.AddField(
            model_name='categoryunlockpurchase',
            name='profile',
            field=models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='quotes.Profile'),
        ),
        migrations.CreateModel(
            name='BalanceRechargeProduct',
            fields=[
                ('id', models.UUIDField(default=uuid.uuid4, editable=False, primary_key=True, serialize=False)),
                ('admin_title', models.CharField(max_length=256, verbose_name='Название')),
                ('balance_recharge', models.IntegerField(default=1, verbose_name='Сумма пополнения баланса')),
                ('google_play_product', models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='api.GooglePlayProduct')),
            ],
            options={
                'verbose_name': 'IAP продукт',
                'verbose_name_plural': 'продукты',
            },
        ),
        migrations.CreateModel(
            name='AchievementReceiving',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('received_at', models.DateTimeField(auto_now_add=True)),
                ('achievement', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='quotes.Achievement')),
                ('profile', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='quotes.Profile')),
            ],
            options={
                'verbose_name': 'достижения vs юзеры',
                'verbose_name_plural': 'достижения vs юзеры',
            },
        ),
        migrations.AddField(
            model_name='achievement',
            name='opened_by_users',
            field=models.ManyToManyField(blank=True, through='quotes.AchievementReceiving', to='quotes.Profile', verbose_name='Профили пользователей которые открыли ачивку'),
        ),
    ]
