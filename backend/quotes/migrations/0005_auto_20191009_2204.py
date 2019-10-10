# Generated by Django 2.2.5 on 2019-10-09 22:04

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('quotes', '0004_auto_20191009_2204'),
    ]

    operations = [
        migrations.AddField(
            model_name='quote',
            name='bonus_reward',
            field=models.BigIntegerField(default=0, verbose_name='Бонус монет за прохождение'),
        ),
        migrations.AddField(
            model_name='quote',
            name='complete_by_users',
            field=models.ManyToManyField(blank=True, related_name='quotes_quote_complete_by_users', related_query_name='quotes_quote_complete_by_users_objs', to='quotes.Profile', verbose_name='Юзеры которые прошли и должны получить вознаграждение'),
        ),
        migrations.AddField(
            model_name='quote',
            name='on_complete_achievement',
            field=models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='quotes.Achievement'),
        ),
        migrations.AddField(
            model_name='section',
            name='bonus_reward',
            field=models.BigIntegerField(default=0, verbose_name='Бонус монет за прохождение'),
        ),
        migrations.AddField(
            model_name='section',
            name='complete_by_users',
            field=models.ManyToManyField(blank=True, related_name='quotes_section_complete_by_users', related_query_name='quotes_section_complete_by_users_objs', to='quotes.Profile', verbose_name='Юзеры которые прошли и должны получить вознаграждение'),
        ),
        migrations.AddField(
            model_name='section',
            name='on_complete_achievement',
            field=models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='quotes.Achievement'),
        ),
        migrations.AddField(
            model_name='topic',
            name='bonus_reward',
            field=models.BigIntegerField(default=0, verbose_name='Бонус монет за прохождение'),
        ),
        migrations.AddField(
            model_name='topic',
            name='complete_by_users',
            field=models.ManyToManyField(blank=True, related_name='quotes_topic_complete_by_users', related_query_name='quotes_topic_complete_by_users_objs', to='quotes.Profile', verbose_name='Юзеры которые прошли и должны получить вознаграждение'),
        ),
        migrations.AddField(
            model_name='topic',
            name='on_complete_achievement',
            field=models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='quotes.Achievement'),
        ),
    ]
