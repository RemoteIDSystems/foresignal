// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'observation_link_link_reason.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObservationLinkLinkReasonCWProxy {
  ObservationLinkLinkReason rule(String rule);

  ObservationLinkLinkReason dtSeconds(num? dtSeconds);

  ObservationLinkLinkReason distanceKm(num? distanceKm);

  ObservationLinkLinkReason attributeDeltas(
    Map<String, Object>? attributeDeltas,
  );

  ObservationLinkLinkReason upstreamIdMatch(bool? upstreamIdMatch);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObservationLinkLinkReason(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObservationLinkLinkReason(...).copyWith(id: 12, name: "My name")
  /// ```
  ObservationLinkLinkReason call({
    String rule,
    num? dtSeconds,
    num? distanceKm,
    Map<String, Object>? attributeDeltas,
    bool? upstreamIdMatch,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObservationLinkLinkReason.copyWith(...)` or call `instanceOfObservationLinkLinkReason.copyWith.fieldName(value)` for a single field.
class _$ObservationLinkLinkReasonCWProxyImpl
    implements _$ObservationLinkLinkReasonCWProxy {
  const _$ObservationLinkLinkReasonCWProxyImpl(this._value);

  final ObservationLinkLinkReason _value;

  @override
  ObservationLinkLinkReason rule(String rule) => call(rule: rule);

  @override
  ObservationLinkLinkReason dtSeconds(num? dtSeconds) =>
      call(dtSeconds: dtSeconds);

  @override
  ObservationLinkLinkReason distanceKm(num? distanceKm) =>
      call(distanceKm: distanceKm);

  @override
  ObservationLinkLinkReason attributeDeltas(
    Map<String, Object>? attributeDeltas,
  ) => call(attributeDeltas: attributeDeltas);

  @override
  ObservationLinkLinkReason upstreamIdMatch(bool? upstreamIdMatch) =>
      call(upstreamIdMatch: upstreamIdMatch);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObservationLinkLinkReason(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObservationLinkLinkReason(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObservationLinkLinkReason call({
    Object? rule = const $CopyWithPlaceholder(),
    Object? dtSeconds = const $CopyWithPlaceholder(),
    Object? distanceKm = const $CopyWithPlaceholder(),
    Object? attributeDeltas = const $CopyWithPlaceholder(),
    Object? upstreamIdMatch = const $CopyWithPlaceholder(),
  }) {
    return ObservationLinkLinkReason(
      rule: rule == const $CopyWithPlaceholder() || rule == null
          ? _value.rule
          // ignore: cast_nullable_to_non_nullable
          : rule as String,
      dtSeconds: dtSeconds == const $CopyWithPlaceholder()
          ? _value.dtSeconds
          // ignore: cast_nullable_to_non_nullable
          : dtSeconds as num?,
      distanceKm: distanceKm == const $CopyWithPlaceholder()
          ? _value.distanceKm
          // ignore: cast_nullable_to_non_nullable
          : distanceKm as num?,
      attributeDeltas: attributeDeltas == const $CopyWithPlaceholder()
          ? _value.attributeDeltas
          // ignore: cast_nullable_to_non_nullable
          : attributeDeltas as Map<String, Object>?,
      upstreamIdMatch: upstreamIdMatch == const $CopyWithPlaceholder()
          ? _value.upstreamIdMatch
          // ignore: cast_nullable_to_non_nullable
          : upstreamIdMatch as bool?,
    );
  }
}

extension $ObservationLinkLinkReasonCopyWith on ObservationLinkLinkReason {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObservationLinkLinkReason.copyWith(...)` or `instanceOfObservationLinkLinkReason.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObservationLinkLinkReasonCWProxy get copyWith =>
      _$ObservationLinkLinkReasonCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObservationLinkLinkReason _$ObservationLinkLinkReasonFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ObservationLinkLinkReason', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['rule']);
  final val = ObservationLinkLinkReason(
    rule: $checkedConvert('rule', (v) => v as String),
    dtSeconds: $checkedConvert('dtSeconds', (v) => v as num?),
    distanceKm: $checkedConvert('distanceKm', (v) => v as num?),
    attributeDeltas: $checkedConvert(
      'attributeDeltas',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    upstreamIdMatch: $checkedConvert('upstreamIdMatch', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$ObservationLinkLinkReasonToJson(
  ObservationLinkLinkReason instance,
) => <String, dynamic>{
  'rule': instance.rule,
  'dtSeconds': ?instance.dtSeconds,
  'distanceKm': ?instance.distanceKm,
  'attributeDeltas': ?instance.attributeDeltas,
  'upstreamIdMatch': ?instance.upstreamIdMatch,
};
