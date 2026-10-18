// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'licence_terms.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LicenceTermsCWProxy {
  LicenceTerms id(String id);

  LicenceTerms name(String name);

  LicenceTerms commercialUse(LicenceTermsCommercialUseEnum commercialUse);

  LicenceTerms shareAlike(bool shareAlike);

  LicenceTerms attributionRequired(bool attributionRequired);

  LicenceTerms redistribution(LicenceTermsRedistributionEnum redistribution);

  LicenceTerms attributionText(String? attributionText);

  LicenceTerms termsUrl(String? termsUrl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LicenceTerms(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LicenceTerms(...).copyWith(id: 12, name: "My name")
  /// ```
  LicenceTerms call({
    String id,
    String name,
    LicenceTermsCommercialUseEnum commercialUse,
    bool shareAlike,
    bool attributionRequired,
    LicenceTermsRedistributionEnum redistribution,
    String? attributionText,
    String? termsUrl,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLicenceTerms.copyWith(...)` or call `instanceOfLicenceTerms.copyWith.fieldName(value)` for a single field.
class _$LicenceTermsCWProxyImpl implements _$LicenceTermsCWProxy {
  const _$LicenceTermsCWProxyImpl(this._value);

  final LicenceTerms _value;

  @override
  LicenceTerms id(String id) => call(id: id);

  @override
  LicenceTerms name(String name) => call(name: name);

  @override
  LicenceTerms commercialUse(LicenceTermsCommercialUseEnum commercialUse) =>
      call(commercialUse: commercialUse);

  @override
  LicenceTerms shareAlike(bool shareAlike) => call(shareAlike: shareAlike);

  @override
  LicenceTerms attributionRequired(bool attributionRequired) =>
      call(attributionRequired: attributionRequired);

  @override
  LicenceTerms redistribution(LicenceTermsRedistributionEnum redistribution) =>
      call(redistribution: redistribution);

  @override
  LicenceTerms attributionText(String? attributionText) =>
      call(attributionText: attributionText);

  @override
  LicenceTerms termsUrl(String? termsUrl) => call(termsUrl: termsUrl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LicenceTerms(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LicenceTerms(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LicenceTerms call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? commercialUse = const $CopyWithPlaceholder(),
    Object? shareAlike = const $CopyWithPlaceholder(),
    Object? attributionRequired = const $CopyWithPlaceholder(),
    Object? redistribution = const $CopyWithPlaceholder(),
    Object? attributionText = const $CopyWithPlaceholder(),
    Object? termsUrl = const $CopyWithPlaceholder(),
  }) {
    return LicenceTerms(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      commercialUse:
          commercialUse == const $CopyWithPlaceholder() || commercialUse == null
          ? _value.commercialUse
          // ignore: cast_nullable_to_non_nullable
          : commercialUse as LicenceTermsCommercialUseEnum,
      shareAlike:
          shareAlike == const $CopyWithPlaceholder() || shareAlike == null
          ? _value.shareAlike
          // ignore: cast_nullable_to_non_nullable
          : shareAlike as bool,
      attributionRequired:
          attributionRequired == const $CopyWithPlaceholder() ||
              attributionRequired == null
          ? _value.attributionRequired
          // ignore: cast_nullable_to_non_nullable
          : attributionRequired as bool,
      redistribution:
          redistribution == const $CopyWithPlaceholder() ||
              redistribution == null
          ? _value.redistribution
          // ignore: cast_nullable_to_non_nullable
          : redistribution as LicenceTermsRedistributionEnum,
      attributionText: attributionText == const $CopyWithPlaceholder()
          ? _value.attributionText
          // ignore: cast_nullable_to_non_nullable
          : attributionText as String?,
      termsUrl: termsUrl == const $CopyWithPlaceholder()
          ? _value.termsUrl
          // ignore: cast_nullable_to_non_nullable
          : termsUrl as String?,
    );
  }
}

extension $LicenceTermsCopyWith on LicenceTerms {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLicenceTerms.copyWith(...)` or `instanceOfLicenceTerms.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LicenceTermsCWProxy get copyWith => _$LicenceTermsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LicenceTerms _$LicenceTermsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LicenceTerms', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'name',
          'commercialUse',
          'shareAlike',
          'attributionRequired',
          'redistribution',
        ],
      );
      final val = LicenceTerms(
        id: $checkedConvert('id', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
        commercialUse: $checkedConvert(
          'commercialUse',
          (v) => $enumDecode(_$LicenceTermsCommercialUseEnumEnumMap, v),
        ),
        shareAlike: $checkedConvert('shareAlike', (v) => v as bool),
        attributionRequired: $checkedConvert(
          'attributionRequired',
          (v) => v as bool,
        ),
        redistribution: $checkedConvert(
          'redistribution',
          (v) => $enumDecode(_$LicenceTermsRedistributionEnumEnumMap, v),
        ),
        attributionText: $checkedConvert(
          'attributionText',
          (v) => v as String?,
        ),
        termsUrl: $checkedConvert('termsUrl', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$LicenceTermsToJson(LicenceTerms instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'commercialUse':
          _$LicenceTermsCommercialUseEnumEnumMap[instance.commercialUse]!,
      'shareAlike': instance.shareAlike,
      'attributionRequired': instance.attributionRequired,
      'redistribution':
          _$LicenceTermsRedistributionEnumEnumMap[instance.redistribution]!,
      'attributionText': ?instance.attributionText,
      'termsUrl': ?instance.termsUrl,
    };

const _$LicenceTermsCommercialUseEnumEnumMap = {
  LicenceTermsCommercialUseEnum.ALLOWED: 'ALLOWED',
  LicenceTermsCommercialUseEnum.FORBIDDEN: 'FORBIDDEN',
  LicenceTermsCommercialUseEnum.UNKNOWN: 'UNKNOWN',
};

const _$LicenceTermsRedistributionEnumEnumMap = {
  LicenceTermsRedistributionEnum.ALLOWED: 'ALLOWED',
  LicenceTermsRedistributionEnum.LOCAL_ONLY: 'LOCAL_ONLY',
  LicenceTermsRedistributionEnum.FORBIDDEN: 'FORBIDDEN',
  LicenceTermsRedistributionEnum.UNKNOWN: 'UNKNOWN',
};
