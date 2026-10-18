// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'near_one_of2.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NearOneOf2CWProxy {
  NearOneOf2 anchor(NearOneOf2Anchor anchor);

  NearOneOf2 radiusKm(num radiusKm);

  NearOneOf2 maxAnchorAge(String? maxAnchorAge);

  NearOneOf2 minMoveMeters(num? minMoveMeters);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NearOneOf2(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NearOneOf2(...).copyWith(id: 12, name: "My name")
  /// ```
  NearOneOf2 call({
    NearOneOf2Anchor anchor,
    num radiusKm,
    String? maxAnchorAge,
    num? minMoveMeters,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNearOneOf2.copyWith(...)` or call `instanceOfNearOneOf2.copyWith.fieldName(value)` for a single field.
class _$NearOneOf2CWProxyImpl implements _$NearOneOf2CWProxy {
  const _$NearOneOf2CWProxyImpl(this._value);

  final NearOneOf2 _value;

  @override
  NearOneOf2 anchor(NearOneOf2Anchor anchor) => call(anchor: anchor);

  @override
  NearOneOf2 radiusKm(num radiusKm) => call(radiusKm: radiusKm);

  @override
  NearOneOf2 maxAnchorAge(String? maxAnchorAge) =>
      call(maxAnchorAge: maxAnchorAge);

  @override
  NearOneOf2 minMoveMeters(num? minMoveMeters) =>
      call(minMoveMeters: minMoveMeters);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NearOneOf2(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NearOneOf2(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NearOneOf2 call({
    Object? anchor = const $CopyWithPlaceholder(),
    Object? radiusKm = const $CopyWithPlaceholder(),
    Object? maxAnchorAge = const $CopyWithPlaceholder(),
    Object? minMoveMeters = const $CopyWithPlaceholder(),
  }) {
    return NearOneOf2(
      anchor: anchor == const $CopyWithPlaceholder() || anchor == null
          ? _value.anchor
          // ignore: cast_nullable_to_non_nullable
          : anchor as NearOneOf2Anchor,
      radiusKm: radiusKm == const $CopyWithPlaceholder() || radiusKm == null
          ? _value.radiusKm
          // ignore: cast_nullable_to_non_nullable
          : radiusKm as num,
      maxAnchorAge: maxAnchorAge == const $CopyWithPlaceholder()
          ? _value.maxAnchorAge
          // ignore: cast_nullable_to_non_nullable
          : maxAnchorAge as String?,
      minMoveMeters: minMoveMeters == const $CopyWithPlaceholder()
          ? _value.minMoveMeters
          // ignore: cast_nullable_to_non_nullable
          : minMoveMeters as num?,
    );
  }
}

extension $NearOneOf2CopyWith on NearOneOf2 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNearOneOf2.copyWith(...)` or `instanceOfNearOneOf2.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NearOneOf2CWProxy get copyWith => _$NearOneOf2CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NearOneOf2 _$NearOneOf2FromJson(Map<String, dynamic> json) => $checkedCreate(
  'NearOneOf2',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['anchor', 'radiusKm']);
    final val = NearOneOf2(
      anchor: $checkedConvert(
        'anchor',
        (v) => NearOneOf2Anchor.fromJson(v as Map<String, dynamic>),
      ),
      radiusKm: $checkedConvert('radiusKm', (v) => v as num),
      maxAnchorAge: $checkedConvert('maxAnchorAge', (v) => v as String?),
      minMoveMeters: $checkedConvert('minMoveMeters', (v) => v as num? ?? 200),
    );
    return val;
  },
);

Map<String, dynamic> _$NearOneOf2ToJson(NearOneOf2 instance) =>
    <String, dynamic>{
      'anchor': instance.anchor.toJson(),
      'radiusKm': instance.radiusKm,
      'maxAnchorAge': ?instance.maxAnchorAge,
      'minMoveMeters': ?instance.minMoveMeters,
    };
