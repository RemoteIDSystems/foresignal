// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'near_one_of.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NearOneOfCWProxy {
  NearOneOf placeIds(List<String>? placeIds);

  NearOneOf placeId(String? placeId);

  NearOneOf radiusKm(num? radiusKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NearOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NearOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  NearOneOf call({List<String>? placeIds, String? placeId, num? radiusKm});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNearOneOf.copyWith(...)` or call `instanceOfNearOneOf.copyWith.fieldName(value)` for a single field.
class _$NearOneOfCWProxyImpl implements _$NearOneOfCWProxy {
  const _$NearOneOfCWProxyImpl(this._value);

  final NearOneOf _value;

  @override
  NearOneOf placeIds(List<String>? placeIds) => call(placeIds: placeIds);

  @override
  NearOneOf placeId(String? placeId) => call(placeId: placeId);

  @override
  NearOneOf radiusKm(num? radiusKm) => call(radiusKm: radiusKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NearOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NearOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NearOneOf call({
    Object? placeIds = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? radiusKm = const $CopyWithPlaceholder(),
  }) {
    return NearOneOf(
      placeIds: placeIds == const $CopyWithPlaceholder()
          ? _value.placeIds
          // ignore: cast_nullable_to_non_nullable
          : placeIds as List<String>?,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      radiusKm: radiusKm == const $CopyWithPlaceholder()
          ? _value.radiusKm
          // ignore: cast_nullable_to_non_nullable
          : radiusKm as num?,
    );
  }
}

extension $NearOneOfCopyWith on NearOneOf {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNearOneOf.copyWith(...)` or `instanceOfNearOneOf.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NearOneOfCWProxy get copyWith => _$NearOneOfCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NearOneOf _$NearOneOfFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NearOneOf', json, ($checkedConvert) {
      final val = NearOneOf(
        placeIds: $checkedConvert(
          'placeIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        placeId: $checkedConvert('placeId', (v) => v as String?),
        radiusKm: $checkedConvert('radiusKm', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$NearOneOfToJson(NearOneOf instance) => <String, dynamic>{
  'placeIds': ?instance.placeIds,
  'placeId': ?instance.placeId,
  'radiusKm': ?instance.radiusKm,
};
