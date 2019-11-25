from api.google import make_credentials_from_file, create_request, process_response, PurchaseValidationError


if __name__ == '__main__':
# credentials = make_credentials_from_file(settings.GOOGLE_APPLICATION_CREDENTIALS)
# credentials = make_credentials_from_file('/Users/ignatev/Downloads/quotepuzzle-904ce-firebase-adminsdk-o4mjg-57c0e11da7.json')
    credentials = make_credentials_from_file('/Users/ignatev/Downloads/Quotes Game-3abe0b360f9f.json')

# request = create_request(credentials, 'com.quote.mosaic', 'get_20_coins', 'mlpnjomdpnhpbffhanohcknh.AO-J1OzLAejHvpx838DvYJ0qBKzI-a1vDM638cYyvTgMacMr0c6oElhEaBkwAONS3-EMeAC8BhJuZVH1XwwpEmArwuo16iV_wqWd3ef63YDtIzSscQQzOY8')
# request = create_request(credentials, 'com.quote.mosaic', 'get_20_coins', 'meghhmmlfkbodooebjplfmbg.AO-J1Oz-GNC6CC2SLJYGHbYon18ckSF-l8PFEl6ljIawWCTx5eE2u12HjnCzqbx5_bVbsnq8a1PVzU3rbp8TSsULdowK2W-ORhM6Y1ZaZgXyRXMVUntjNJw')
# request = create_request(credentials, 'com.quote.mosaic', 'get_1000_coins', 'ppbcgdoncgefhpfjcbajfbma.AO-J1OxdctdC5Aq-DMLR0KWpPP9Q8DbwXJ8rJt7lCUoYQU-r0GmtD0qC3g9F-2YY2U-mkjxPzMmyP0lSztaQAjon-vmVSD0CRDy5dBKK2FwxaIib3yEWyIw-P4YOphwsBTK11uT5h1h_')
# request = create_request(credentials, 'com.quote.mosaic', 'get_1000_coins', 'ppbcgdoncgefhpfjcbajfbma.AO-J1OxdctdC5Aq-DMLR0KWpPP9Q8DbwXJ8rJt7lCUoYQU-r0GmtD0qC3g9F-2YY2U-mkjxPzMmyP0lSztaQAjon-vmVSD0CRDy5dBKK2FwxaIib3yEWyIw-P4YOphwsBTK11uT5h1h_')
# for i in range(0,10):

    # request = create_request(credentials, 'com.quote.mosaic', 'get_1000_coins', 'adcgfmkmdgmigdjeehnjocen.AO-J1Oxh8U3FKP-DBVQulRen9eRaK0qurrCUREgNQdmvRi-NsPhwTrHOsSv4aZMQLpkqSCJWx6n-eRGX7UHx_4oXAepn0YOyWv_FHptqVBXMkUFs9gu5gBLHeEv2DElExKJZeMcwdhcK')
    request = create_request(credentials, 'com.quote.mosaic', 'android.test.reward', 'inapp:com.quote.mosaic:android.test.reward')

    try:
        status = process_response(request)
        print(status)
        print(status.is_purchased())
        print(status.is_cancelled())
        print(status.is_ready())
    # except HttpError as e:
    except PurchaseValidationError as e:
        raise PurchaseValidationError(e)
