// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_source_info_licence.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpSourceInfoLicenceCWProxy {
  EdpSourceInfoLicence id(String? id);

  EdpSourceInfoLicence url(String? url);

  EdpSourceInfoLicence attribution(String? attribution);

  EdpSourceInfoLicence redistribution(
    EdpSourceInfoLicenceRedistributionEnum? redistribution,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSourceInfoLicence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSourceInfoLicence(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpSourceInfoLicence call({
    String? id,
    String? url,
    String? attribution,
    EdpSourceInfoLicenceRedistributionEnum? redistribution,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpSourceInfoLicence.copyWith(...)` or call `instanceOfEdpSourceInfoLicence.copyWith.fieldName(value)` for a single field.
class _$EdpSourceInfoLicenceCWProxyImpl
    implements _$EdpSourceInfoLicenceCWProxy {
  const _$EdpSourceInfoLicenceCWProxyImpl(this._value);

  final EdpSourceInfoLicence _value;

  @override
  EdpSourceInfoLicence id(String? id) => call(id: id);

  @override
  EdpSourceInfoLicence url(String? url) => call(url: url);

  @override
  EdpSourceInfoLicence attribution(String? attribution) =>
      call(attribution: attribution);

  @override
  EdpSourceInfoLicence redistribution(
    EdpSourceInfoLicenceRedistributionEnum? redistribution,
  ) => call(redistribution: redistribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSourceInfoLicence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSourceInfoLicence(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpSourceInfoLicence call({
    Object? id = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? redistribution = const $CopyWithPlaceholder(),
  }) {
    return EdpSourceInfoLicence(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String?,
      redistribution: redistribution == const $CopyWithPlaceholder()
          ? _value.redistribution
          // ignore: cast_nullable_to_non_nullable
          : redistribution as EdpSourceInfoLicenceRedistributionEnum?,
    );
  }
}

extension $EdpSourceInfoLicenceCopyWith on EdpSourceInfoLicence {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpSourceInfoLicence.copyWith(...)` or `instanceOfEdpSourceInfoLicence.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpSourceInfoLicenceCWProxy get copyWith =>
      _$EdpSourceInfoLicenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpSourceInfoLicence _$EdpSourceInfoLicenceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EdpSourceInfoLicence', json, ($checkedConvert) {
  final val = EdpSourceInfoLicence(
    id: $checkedConvert('id', (v) => v as String?),
    url: $checkedConvert('url', (v) => v as String?),
    attribution: $checkedConvert('attribution', (v) => v as String?),
    redistribution: $checkedConvert(
      'redistribution',
      (v) => $enumDecodeNullable(
        _$EdpSourceInfoLicenceRedistributionEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$EdpSourceInfoLicenceToJson(
  EdpSourceInfoLicence instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'url': ?instance.url,
  'attribution': ?instance.attribution,
  'redistribution':
      ?_$EdpSourceInfoLicenceRedistributionEnumEnumMap[instance.redistribution],
};

const _$EdpSourceInfoLicenceRedistributionEnumEnumMap = {
  EdpSourceInfoLicenceRedistributionEnum.allowed: 'allowed',
  EdpSourceInfoLicenceRedistributionEnum.localOnly: 'local-only',
  EdpSourceInfoLicenceRedistributionEnum.unknown: 'unknown',
};
