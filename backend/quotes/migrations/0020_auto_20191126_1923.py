# Generated by Django 2.2.5 on 2019-11-26 19:23

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('quotes', '0019_doubleupproduct'),
    ]

    operations = [
        migrations.AddField(
            model_name='balancerechargeproduct',
            name='scope_tags',
            field=models.ManyToManyField(blank=True, to='quotes.Tag'),
        ),
        migrations.AddField(
            model_name='doubleupproduct',
            name='admin_title',
            field=models.CharField(default='Not set!!!', max_length=256, verbose_name='Название продукта для юзера'),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='doubleupproduct',
            name='is_featured',
            field=models.BooleanField(blank=True, default=False, verbose_name='Показывать как самый выгодный?'),
        ),
        migrations.AddField(
            model_name='doubleupproduct',
            name='item_image',
            field=models.FileField(blank=True, null=True, upload_to='rechargeproducts', verbose_name='Картинка 512х512'),
        ),
        migrations.AddField(
            model_name='doubleupproduct',
            name='item_image_preview',
            field=models.FileField(blank=True, null=True, upload_to='rechargeproducts-preview', verbose_name='Превью картинки 256х256'),
        ),
        migrations.AddField(
            model_name='doubleupproduct',
            name='scope_tags',
            field=models.ManyToManyField(blank=True, to='quotes.Tag'),
        ),
    ]
