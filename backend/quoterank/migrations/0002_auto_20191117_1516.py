# Generated by Django 2.2.5 on 2019-11-17 15:16

from django.db import migrations, models
import django.utils.timezone


class Migration(migrations.Migration):

    dependencies = [
        ('quoterank', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='profilerank',
            name='joined_at',
            field=models.DateTimeField(auto_now_add=True, default=django.utils.timezone.now),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='profilerank',
            name='last_update',
            field=models.DateTimeField(auto_now=True),
        ),
        migrations.AddField(
            model_name='profilerank',
            name='position_change_since_last_update',
            field=models.BigIntegerField(default=0),
        ),
        migrations.AddField(
            model_name='profilerank',
            name='rank_cached',
            field=models.PositiveIntegerField(default=0),
        ),
        migrations.AddField(
            model_name='profilerank',
            name='rank_change_since_last_update',
            field=models.BigIntegerField(default=0),
        ),
        migrations.AddField(
            model_name='profilerank',
            name='total_category_achievements_received',
            field=models.PositiveIntegerField(default=0),
        ),
        migrations.AddField(
            model_name='profilerank',
            name='total_category_reward',
            field=models.PositiveIntegerField(default=0),
        ),
        migrations.AddField(
            model_name='profilerank',
            name='total_quote_achievements_received',
            field=models.PositiveIntegerField(default=0),
        ),
        migrations.AddField(
            model_name='profilerank',
            name='total_quote_reward',
            field=models.PositiveIntegerField(default=0),
        ),
        migrations.AddField(
            model_name='profilerank',
            name='total_section_achievements_received',
            field=models.PositiveIntegerField(default=0),
        ),
        migrations.AddField(
            model_name='profilerank',
            name='total_section_reward',
            field=models.PositiveIntegerField(default=0),
        ),
        migrations.AddField(
            model_name='profilerank',
            name='total_topic_achievements_received',
            field=models.PositiveIntegerField(default=0),
        ),
        migrations.AddField(
            model_name='profilerank',
            name='total_topic_reward',
            field=models.PositiveIntegerField(default=0),
        ),
    ]
