// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'get_intel_calibration200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GetIntelCalibration200ResponseCWProxy {
  GetIntelCalibration200Response scopes(List<IntelCalibration> scopes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GetIntelCalibration200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GetIntelCalibration200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  GetIntelCalibration200Response call({List<IntelCalibration> scopes});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGetIntelCalibration200Response.copyWith(...)` or call `instanceOfGetIntelCalibration200Response.copyWith.fieldName(value)` for a single field.
class _$GetIntelCalibration200ResponseCWProxyImpl
    implements _$GetIntelCalibration200ResponseCWProxy {
  const _$GetIntelCalibration200ResponseCWProxyImpl(this._value);

  final GetIntelCalibration200Response _value;

  @override
  GetIntelCalibration200Response scopes(List<IntelCalibration> scopes) =>
      call(scopes: scopes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GetIntelCalibration200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GetIntelCalibration200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GetIntelCalibration200Response call({
    Object? scopes = const $CopyWithPlaceholder(),
  }) {
    return GetIntelCalibration200Response(
      scopes: scopes == const $CopyWithPlaceholder() || scopes == null
          ? _value.scopes
          // ignore: cast_nullable_to_non_nullable
          : scopes as List<IntelCalibration>,
    );
  }
}

extension $GetIntelCalibration200ResponseCopyWith
    on GetIntelCalibration200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGetIntelCalibration200Response.copyWith(...)` or `instanceOfGetIntelCalibration200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GetIntelCalibration200ResponseCWProxy get copyWith =>
      _$GetIntelCalibration200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GetIntelCalibration200Response _$GetIntelCalibration200ResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('GetIntelCalibration200Response', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['scopes']);
  final val = GetIntelCalibration200Response(
    scopes: $checkedConvert(
      'scopes',
      (v) => (v as List<dynamic>)
          .map((e) => IntelCalibration.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$GetIntelCalibration200ResponseToJson(
  GetIntelCalibration200Response instance,
) => <String, dynamic>{
  'scopes': instance.scopes.map((e) => e.toJson()).toList(),
};
