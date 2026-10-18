// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'field_encodings_velocity_json.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FieldEncodingsVelocityJsonCWProxy {
  FieldEncodingsVelocityJson url(String? url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldEncodingsVelocityJson(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldEncodingsVelocityJson(...).copyWith(id: 12, name: "My name")
  /// ```
  FieldEncodingsVelocityJson call({String? url});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFieldEncodingsVelocityJson.copyWith(...)` or call `instanceOfFieldEncodingsVelocityJson.copyWith.fieldName(value)` for a single field.
class _$FieldEncodingsVelocityJsonCWProxyImpl
    implements _$FieldEncodingsVelocityJsonCWProxy {
  const _$FieldEncodingsVelocityJsonCWProxyImpl(this._value);

  final FieldEncodingsVelocityJson _value;

  @override
  FieldEncodingsVelocityJson url(String? url) => call(url: url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldEncodingsVelocityJson(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldEncodingsVelocityJson(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FieldEncodingsVelocityJson call({
    Object? url = const $CopyWithPlaceholder(),
  }) {
    return FieldEncodingsVelocityJson(
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
    );
  }
}

extension $FieldEncodingsVelocityJsonCopyWith on FieldEncodingsVelocityJson {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFieldEncodingsVelocityJson.copyWith(...)` or `instanceOfFieldEncodingsVelocityJson.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FieldEncodingsVelocityJsonCWProxy get copyWith =>
      _$FieldEncodingsVelocityJsonCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FieldEncodingsVelocityJson _$FieldEncodingsVelocityJsonFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FieldEncodingsVelocityJson', json, ($checkedConvert) {
  final val = FieldEncodingsVelocityJson(
    url: $checkedConvert('url', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$FieldEncodingsVelocityJsonToJson(
  FieldEncodingsVelocityJson instance,
) => <String, dynamic>{'url': ?instance.url};
