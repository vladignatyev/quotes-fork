# Generated by Django 2.2.5 on 2019-10-05 09:02

from django.db import migrations, models
import django.utils.timezone


class Migration(migrations.Migration):

    dependencies = [
        ('quotes', '0008_auto_20191005_0857'),
    ]

    operations = [
        migrations.AddField(
            model_name='categoryunlockpurchase',
            name='date_created',
            field=models.DateTimeField(auto_now_add=True, default=django.utils.timezone.now),
            preserve_default=False,
        ),
        migrations.AlterField(
            model_name='quotecategory',
            name='available_to_users',
            field=models.ManyToManyField(blank=True, through='quotes.CategoryUnlockPurchase', to='quotes.Profile', verbose_name='Профили пользователей которым доступна категория'),
        ),
    ]
